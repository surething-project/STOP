package pt.ulisboa.tecnico.captor.captorapplibrary.clclients;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import java.io.IOException;

import okhttp3.ResponseBody;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.cryptography.CryptographyUtil;
import pt.ulisboa.tecnico.captor.captorapplibrary.holders.InspectDataHolder;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.inspection.Checkpoint;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.location.Coordinates;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.inspection.Inspection;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.inspection.InspectorLocationProof;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public class InspectAPIClient extends CLClient {

    public static final String CHECKPOINT_CREATED = "Checkpoint created";
    public static final String CHECKPOINT_NOT_CREATED = "Checkpoint not created";
    public static final String INSPECTION_CREATED = "Inspection created";
    public static final String INSPECTION_NOT_CREATED = "Inspection not created";
    public static final String INSPECTION_ENDED = "Inspection ended";
    public static final String INSPECTION_NOT_ENDED = "Inspection not ended";
    public static final String CURRENT_INSPECTION_UPDATED = "Current inspection updated";
    public static final String CURRENT_INSPECTION_NOT_UPDATED = "Current inspection not updated";

    private static InspectAPIClient clientInstance = null;
    private InspectAPI inspectAPI;

    public InspectAPIClient() {
        retrofit = new Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        inspectAPI = retrofit.create(InspectAPI.class);
    }

    public static InspectAPIClient getInstance() {
        if (clientInstance == null) {
            clientInstance = new InspectAPIClient();
        }
        return clientInstance;
    }

    public interface InspectAPI {
        @Headers({"Content-Type: application/json", "Accept: application/json"})
        @POST("/CentralLedger/v1/inspect/")
        Call<ResponseBody> addCheckpoint(@Body Checkpoint checkpoint);

        @POST("/CentralLedger/v1/inspect/{checkpointId}/select")
        Call<Inspection> selectInspection(@Path("checkpointId") Long checkpointId);

        @GET("/CentralLedger/v1/inspect/{checkpointId}")
        Call<Checkpoint> showCheckpoint(@Path("checkpointId") Long checkpointId);

        @GET("/CentralLedger/v1/inspect/{checkpointId}/update/{inspectionId}")
        Call<Inspection> showInspection(@Path("checkpointId") Long checkpointId, @Path("inspectionId") Long inspectionId);

        @POST("/CentralLedger/v1/inspect/{checkpointId}/update/{inspectionId}")
        Call<Inspection> updateInspection(@Path("checkpointId") Long checkpointId, @Path("inspectionId") Long inspectionId,
                                          @Body InspectorLocationProof inspectorLocationProof);

    }

    public void addCheckpoint(final Activity activity) {
        Log.d("Checkpoint", "Creating Checkpoint");
        Location location = InspectDataHolder.getInstance().getLastLocation();
        Checkpoint checkpoint = new Checkpoint();
        checkpoint.setCoordinates(new Coordinates(location.getLatitude(), location.getLongitude()));
        checkpoint.setInspector(InspectDataHolder.getInstance().getUser());

        InspectDataHolder.getInstance().setActiveCheckpoint(checkpoint);

        Log.d("Checkpoint", checkpoint.toString());
        Call<ResponseBody> call = inspectAPI.addCheckpoint(checkpoint);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.code() == 200) {
                        Checkpoint checkpoint = InspectDataHolder.getInstance().getActiveCheckpoint();
                        checkpoint.setCheckpointId(Long.valueOf(response.body().string()));
                        InspectDataHolder.getInstance().setActiveCheckpoint(checkpoint);

                        Intent intent = new Intent(CHECKPOINT_CREATED);
                        activity.sendBroadcast(intent);
                    } else {
                        Log.d("Checkpoint", response.code() + " " + response.errorBody().string());
                        Intent intent = new Intent(CHECKPOINT_NOT_CREATED);
                        activity.sendBroadcast(intent);
                    }
                } catch (IOException e) {
                    Intent intent = new Intent(CHECKPOINT_NOT_CREATED);
                    activity.sendBroadcast(intent);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Intent intent = new Intent(CHECKPOINT_NOT_CREATED);
                activity.sendBroadcast(intent);
            }
        });
    }

    public void addInspection(final Activity activity) {
        Call<Inspection> call = inspectAPI.selectInspection(InspectDataHolder.getInstance()
                .getActiveCheckpoint().getCheckpointId());
        call.enqueue(new Callback<Inspection>() {
            @Override
            public void onResponse(Call<Inspection> call, Response<Inspection> response) {
                if (response.code() == 200) {
                    Inspection inspection = response.body();
                    Checkpoint checkpoint = InspectDataHolder.getInstance().getActiveCheckpoint();
                    checkpoint.addInspection(inspection);
                    InspectDataHolder.getInstance().setActiveInspection(inspection);
                    InspectDataHolder.getInstance().setCurrentTransportPublicKey(
                            CryptographyUtil.publicKeyFromString(
                                    inspection.getTrip().getTransport().getPublicKey()
                            )
                    );
                    Intent intent = new Intent(INSPECTION_CREATED);
                    activity.sendBroadcast(intent);
                } else {
                    Intent intent = new Intent(INSPECTION_NOT_CREATED);
                    activity.sendBroadcast(intent);
                }
            }

            @Override
            public void onFailure(Call<Inspection> call, Throwable t) {
                Intent intent = new Intent(INSPECTION_NOT_CREATED);
                activity.sendBroadcast(intent);
            }
        });
    }

    public void updateInspection(final Activity activity) {
        Inspection inspection = InspectDataHolder.getInstance().getActiveInspection();
        Call<Inspection> call = inspectAPI.updateInspection(inspection.getCheckpoint().getCheckpointId(), inspection.getInspectionId(), inspection.getProof());
        call.enqueue(new Callback<Inspection>() {
            @Override
            public void onResponse(Call<Inspection> call, Response<Inspection> response) {
                if (response.code() == 200) {
                    Intent intent = new Intent(INSPECTION_ENDED);
                    activity.sendBroadcast(intent);
                } else {
                    Intent intent = new Intent(INSPECTION_ENDED);
                    activity.sendBroadcast(intent);
                }
            }

            @Override
            public void onFailure(Call<Inspection> call, Throwable t) {
                Intent intent = new Intent(INSPECTION_NOT_ENDED);
                activity.sendBroadcast(intent);
            }
        });
    }

     public void showInspection(final Activity activity) {
        Inspection inspection = InspectDataHolder.getInstance().getActiveInspection();
        Call<Inspection> call = inspectAPI.showInspection(inspection.getCheckpoint().getCheckpointId(), inspection.getInspectionId());
        call.enqueue(new Callback<Inspection>() {
            @Override
            public void onResponse(Call<Inspection> call, Response<Inspection> response) {
                if (response.code() == 200) {
                    Inspection inspection = response.body();
                    InspectDataHolder.getInstance().setActiveInspection(inspection);
                    Intent intent = new Intent(CURRENT_INSPECTION_UPDATED);
                    activity.sendBroadcast(intent);
                } else {
                    Intent intent = new Intent(CURRENT_INSPECTION_NOT_UPDATED);
                    activity.sendBroadcast(intent);
                }
            }

            @Override
            public void onFailure(Call<Inspection> call, Throwable t) {
                Intent intent = new Intent(CURRENT_INSPECTION_NOT_UPDATED);
                activity.sendBroadcast(intent);
            }
        });
     }
}
