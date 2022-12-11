package pt.ulisboa.tecnico.captor.captorapplibrary.location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import pt.ulisboa.tecnico.captor.captorapplibrary.ui.UIHandler;

/**
 *
 */
public class LocationHandler {
    private static final String TAG = "LocationHandler";

    /**
     * Values used for Location Request. These can be modified by subclasses
     */
    protected int UPDATE_INTERVAL_IN_MILLISECONDS = 1000;
    protected long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    protected int PRIORITY = LocationRequest.PRIORITY_HIGH_ACCURACY;

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 25;
    private static final int REQUEST_CHECK_SETTINGS = 78;

    /**
     * Varibales used for location collection
     */
    protected Activity activity;
    protected FusedLocationProviderClient fusedLocationClient;
    protected LocationCallback locationCallback;
    protected LocationRequest locationRequest;
    protected Location currentLocation;
    protected LocationSettingsRequest locationSettingsRequest;
    protected SettingsClient settingsClient;
    protected boolean requestingLocationUpdates;
    protected boolean ready;

    /**
     *
     * @param activity The UI activity
     */
    public LocationHandler(Activity activity) {
        this.activity = activity;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
        settingsClient = LocationServices.getSettingsClient(activity);
        ready = false;
        requestingLocationUpdates = false;
    }

    /**
     * Starts location collection
     */
    public void start() {
        if (!ready) {
            setup();
        }
        requestingLocationUpdates = true;
        startLocationUpdates();
    }

    /**
     * Stops location collection
     */
    public void stop() {
        stopLocationUpdates();
    }

    /**
     * Method to be overridden by subclasses to handle the location point collected
     */
    protected void saveLocation() {}

    /**
     * Checks permissions and setups variables needed to start location collection
     */
    protected void setup() {
        if (!checkPermissions()) {
            requestPermissions();
        }
        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();
        ready = true;
    }

    /**
     *
     * @return boolean value regarding the permissions granted for location collection
     */
    protected boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Prompts the user to approved permissions
     */
    protected void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(activity,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");

        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Setups the clients to start location collection
     */
    protected void startLocationUpdates() {
        settingsClient.checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener(activity, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location setting satisfied");

                        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                    }
                })
                .addOnFailureListener(activity, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                UIHandler.showToast(activity, activity, errorMessage);
                                requestingLocationUpdates = false;
                        }
                    }
                });
    }

    /**
     * Builds location settings request
     */
    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        locationSettingsRequest = builder.build();
    }

    /**
     * Removes location updates
     */
    protected void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    /**
     * Creates location request
     */
    protected void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setPriority(PRIORITY);
    }

    /**
     * Setup what to do in location callback
     */
    protected void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                currentLocation = locationResult.getLastLocation();
                saveLocation();
            }
        };
    }
}
