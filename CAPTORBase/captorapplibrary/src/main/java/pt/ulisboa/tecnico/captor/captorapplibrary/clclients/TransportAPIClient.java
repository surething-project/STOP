package pt.ulisboa.tecnico.captor.captorapplibrary.clclients;


import android.app.Activity;
import android.content.Intent;
import android.location.Location;

import java.io.IOException;
import java.util.LinkedList;

import okhttp3.ResponseBody;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.cryptography.CryptographyUtil;
import pt.ulisboa.tecnico.captor.captorapplibrary.holders.TransportDataHolder;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.location.Coordinates;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.inspection.Inspection;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.trip.LocationChainItem;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.trip.LocationPoint;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.trip.Trip;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * This client is used by the Transport app to communicate with the Central Ledger
 * It is also responsable for creating Tips and Location Points
 */
public class TransportAPIClient extends CLClient {

    public static final String TRIP_CREATED = "Trip created";
    public static final String TRIP_NOT_CREATED = "Trip not created";
    public static final String START_TRIP = "Start trip";
    public static final String TRIP_NOT_STARTED = "Trip not started";
    public static final String SENT_LOCATION = "Sent location";
    public static final String LOCATION_NOT_SENT = "Location not sent";
    public static final String ENDED_TRIP = "Ended trip";
    public static final String TRIP_NOT_ENDED = "Trip not ended";
    public static final String SELECTED = "Selected";
    public static final String NOT_SELECTED = "Not selected";
    public static final String SENT_PROOF = "Sent proof";
    public static final String PROOF_NOT_SENT = "Proof not sent";

    private static TransportAPIClient clientInstance = null;
    private TripAPI tripAPI;
    private LinkedList<LocationChainItem> offlineQueue;

    public TransportAPIClient() {

        retrofit = new Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        tripAPI = retrofit.create(TripAPI.class);
        offlineQueue = new LinkedList<LocationChainItem>();
    }

    public static TransportAPIClient getInstance() {
        if (clientInstance == null) {
            clientInstance = new TransportAPIClient();
        }
        return clientInstance;
    }

    public interface TripAPI {
        @POST("/CentralLedger/v1/trip/")
        Call<ResponseBody> addTrip(@Body Trip trip);

        @GET("/CentralLedger/v1/trip/{tripId}/inspect")
        Call<Inspection> checkTripInspection(@Path("tripId") Long tripId);

        @POST("/CentralLedger/v1/trip/{tripId}/end")
        Call<Void> endTrip(@Path("tripId") Long tripId, @Body LocationChainItem item);

        @GET("/CentralLedger/v1/trip/{tripId}")
        Call<Trip> getTripById(@Path("tripId") Long tripId);

        @GET("/CentralLedger/v1/trip/{tripId}/locate")
        Call<LocationPoint> getTripLocation(@Path("tripId") Long tripId);

        @POST("/CentralLedger/v1/trip/{tripId}/begin")
        Call<Void> initializeTrip(@Path("tripId") Long tripId, @Body LocationChainItem item);

        @POST("/CentralLedger/v1/trip/{tripId}/locate")
        Call<Void> locateTrip(@Path("tripId") Long tripId, @Body LocationChainItem item);

        @PUT("/CentralLedger/v1/trip/{tripId}")
        Call<Void> updateTripById(@Path("tripId") Long tripId, @Body Trip trip);

        @POST("/CentralLedger/v1/trip/{tripId}/proof")
        Call<Void> addProof(@Path("tripId") Long tripId, @Body LocationChainItem item);
    }

    public void addTrip(final Activity activity) {
        Location location = TransportDataHolder.getInstance().getLastLocation();
        Trip trip = new Trip(new Coordinates(location.getLatitude(), location.getLongitude()));
        trip.setTransport(TransportDataHolder.getInstance().getUser());

        TransportDataHolder.getInstance().setActiveTrip(trip);

        Call<ResponseBody> call = tripAPI.addTrip(trip);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Trip trip = TransportDataHolder.getInstance().getActiveTrip();
                try {
                    if (response.code() == 200) {
                        trip.setId(Long.valueOf(response.body().string()));
                        TransportDataHolder.getInstance().setActiveTrip(trip);

                        Intent intent = new Intent(TRIP_CREATED);
                        activity.sendBroadcast(intent);
                    } else {
                        Intent intent = new Intent(TRIP_NOT_CREATED);
                        activity.sendBroadcast(intent);

                    }
                } catch (IOException e) {
                    Intent intent = new Intent(TRIP_NOT_CREATED);
                    activity.sendBroadcast(intent);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Intent intent = new Intent(TRIP_NOT_CREATED);
                activity.sendBroadcast(intent);
            }
        });
    }

    public void initializeTrip(final Activity activity) {
        Trip trip = TransportDataHolder.getInstance().getActiveTrip();
        LocationChainItem item = createLocationChainItem();

        Call<Void> call = tripAPI.initializeTrip(trip.getId(), item);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.code() == 200) {
                    Intent intent = new Intent(START_TRIP);
                    activity.sendBroadcast(intent);
                } else {
                    Intent intent = new Intent(TRIP_NOT_STARTED);
                    activity.sendBroadcast(intent);
                    // remove LP
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Intent intent = new Intent(TRIP_NOT_STARTED);
                activity.sendBroadcast(intent);
                // remove LP
            }
        });
    }

    public void locateTrip(final Activity activity) {
        LocationChainItem item = createLocationChainItem();
        offlineQueue.addLast(item);
        clearOfflineLocateTrip(activity);

    }

    private void clearOfflineLocateTrip(final Activity activity) {
        if (offlineQueue.isEmpty()) {
            return;     // no more items to send
        }
        Trip trip = TransportDataHolder.getInstance().getActiveTrip();
        LocationChainItem item = offlineQueue.getFirst();     // send first location item not sent
        Call<Void> call = tripAPI.locateTrip(trip.getId(), item);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.code() == 200) {
                    Intent intent = new Intent(SENT_LOCATION);
                    activity.sendBroadcast(intent);
                    if (! offlineQueue.isEmpty()) {
                        offlineQueue.removeFirst();     // remove sent item
                    }
                    clearOfflineLocateTrip(activity);   // send unsent items
                } else {
                    Intent intent = new Intent(LOCATION_NOT_SENT);
                    activity.sendBroadcast(intent);
                    // how to handle this case?
                    // remove LP?
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {   // unable to contact server
                // let's continue and try again later
                Intent intent = new Intent(LOCATION_NOT_SENT);
                activity.sendBroadcast(intent);
                // remove LP?
            }
        });
    }


    public void endTrip(final Activity activity) {
        clearOfflineLocateTrip(activity);       // send all unsent location items
        if (! offlineQueue.isEmpty()) {     // could not send items, then trip cannot be ended
            Intent intent = new Intent(TRIP_NOT_ENDED);     // try again later
            activity.sendBroadcast(intent);
            return;
        }

        Trip trip = TransportDataHolder.getInstance().getActiveTrip();
        LocationChainItem item = createLocationChainItem();

        Call<Void> call = tripAPI.endTrip(trip.getId(), item);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.code() == 200) {
                    Intent intent = new Intent(ENDED_TRIP);
                    activity.sendBroadcast(intent);
                } else {
                    Intent intent = new Intent(TRIP_NOT_ENDED);
                    activity.sendBroadcast(intent);
                    // remove LP
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Intent intent = new Intent(TRIP_NOT_ENDED);
                activity.sendBroadcast(intent);
                // remove LP
            }
        });
    }

    public void checkTripInspection(final Activity activity) {
        Call<Inspection> call = tripAPI.checkTripInspection(TransportDataHolder.getInstance().getActiveTrip().getId());
        call.enqueue(new Callback<Inspection>() {
            @Override
            public void onResponse(Call<Inspection> call, Response<Inspection> response) {
                if (response.code() == 200) {
                    TransportDataHolder.getInstance().setCurrentInspection(response.body());
                    TransportDataHolder.getInstance().setCurrentInspectPublicKey(
                            CryptographyUtil.publicKeyFromString(
                                    response.body().getCheckpoint().getInspector().getPublicKey()
                            )
                    );
                    Intent intent = new Intent(SELECTED);
                    activity.sendBroadcast(intent);
                } if (response.code() == 404) {
                    Intent intent = new Intent(NOT_SELECTED);
                    activity.sendBroadcast(intent);
                }
            }

            @Override
            public void onFailure(Call<Inspection> call, Throwable t) {
                // broadcast specific error?
            }
        });
    }

    public void addProof(final Activity activity) {
        Call<Void> call = tripAPI.addProof(TransportDataHolder.getInstance().getActiveTrip().getId(),
                TransportDataHolder.getInstance().getLastProof());
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.code() == 200) {
                    Intent intent = new Intent(SENT_PROOF);
                    activity.sendBroadcast(intent);
                } else {
                    Intent intent = new Intent(PROOF_NOT_SENT);
                    activity.sendBroadcast(intent);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Intent intent = new Intent(PROOF_NOT_SENT);
                activity.sendBroadcast(intent);
            }
        });
    }

    public LocationChainItem createLocationChainItem() {
        Location location = TransportDataHolder.getInstance().getLastLocation();
        Trip trip = TransportDataHolder.getInstance().getActiveTrip();
        /**
         * Create location point
         */
        LocationPoint locationPoint = new LocationPoint(new Coordinates(location.getLatitude(), location.getLongitude()));
        locationPoint.setAccuracy(location.getAccuracy());      // To evaluate location accuracy
        locationPoint.setNumber(trip.getNextLPNumber());
        locationPoint.setTripId(trip.getId());
        /**
         * Get previous hash
         */
        locationPoint.setPreviousLocationSignature(trip.getLastHash());
        /**
         * Create locationchain item
         */
        LocationChainItem item = new LocationChainItem();
        item.setLocation(locationPoint);
        item.setProof(false);
        /**
         * Create signature of item
         */
        item.setTransportSignature(CryptographyUtil.signObject(locationPoint,
                TransportDataHolder.getInstance().getUser().getKeyPair().getPrivate()));
        trip.addLocationChainItem(item);
        TransportDataHolder.getInstance().setActiveTrip(trip);
        return item;
    }
}
