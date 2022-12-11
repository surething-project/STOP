package pt.ulisboa.tecnico.captor.transport;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import pt.ulisboa.tecnico.captor.captorapplibrary.bluetooth.BluetoothController;
import pt.ulisboa.tecnico.captor.captorapplibrary.bluetooth.TransportBluetoothController;
import pt.ulisboa.tecnico.captor.captorapplibrary.clclients.TransportAPIClient;
import pt.ulisboa.tecnico.captor.captorapplibrary.evaluation.EvaluationRecorder;
import pt.ulisboa.tecnico.captor.captorapplibrary.holders.TransportDataHolder;
import pt.ulisboa.tecnico.captor.captorapplibrary.location.TransportLocationHandler;
import pt.ulisboa.tecnico.captor.captorapplibrary.ui.UIHandler;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.evaluation.EvaluationDataHolder;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.location.Coordinates;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    public static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 31;
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 51;

    private Button arrivedButton;
    private Button createTrip;
    private Button startTrip;
    private Button endTrip;
    private TransportBluetoothController transportBluetoothController;
    private TransportLocationHandler locationHandler;
    private boolean onTrip;
    private boolean sendingLocation;
    private MapView map;
    private IMapController mapController;
    private Marker currentMarker;
    private Marker inspectionMarker;
    private TextView selected;
    private Button logout;
    private TextView user;
    private TextView currentTrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * Grant location access. Android needs this at runtime
         */
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // Show explanation
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        } else {        // Permission already granted
            enableBT();
        }
        /**
         * Grant file access for evaluation recording
         */
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show explanation
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        }
        /**
         * Set up Osmodroid
         */
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        /**
         * Set window in portrait and always on
         */
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        /**
         * Initialize variables used in activity
         */
        final Activity activity = this;
        IntentFilter filter;
        arrivedButton = findViewById(R.id.arrived);
        createTrip = findViewById(R.id.createTrip);
        startTrip = findViewById(R.id.startTrip);
        endTrip = findViewById(R.id.endTrip);
        selected = findViewById(R.id.selected);
        logout = findViewById(R.id.logout);
        user = findViewById(R.id.user);
        currentTrip = findViewById(R.id.current_trip);
        /**
         * Initialize Bluetooth Controller
         */
        transportBluetoothController = new TransportBluetoothController(getApplicationContext());
        /**
         * Set button to create trip and set associated filters
         */
        createTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TransportAPIClient.getInstance().addTrip(activity);
                createTrip.setVisibility(View.INVISIBLE);
                // create create activity
                createTrip();
            }
        });
        /**
         * Set button to start trip and set associated filters
         */
        startTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransportAPIClient.getInstance().initializeTrip(activity);
            }
        });
        filter = new IntentFilter(TransportAPIClient.START_TRIP);       // Central Ledger allows trip to start
        this.registerReceiver(receiver, filter);
        filter = new IntentFilter(TransportAPIClient.SENT_LOCATION);    // Current location sent to Central Ledger
        this.registerReceiver(receiver, filter);
        filter = new IntentFilter(TransportAPIClient.LOCATION_NOT_SENT);    // Current location sent to Central Ledger
        this.registerReceiver(receiver, filter);
        filter = new IntentFilter(TransportAPIClient.SELECTED);         // Central Ledger selected trip for inspection
        this.registerReceiver(receiver, filter);
        filter = new IntentFilter(TransportAPIClient.NOT_SELECTED);     // Central Ledger did not select trip for inspection
        this.registerReceiver(receiver, filter);
        /**
         * Set button for arrived button and set associated filters - TEMP
         */
        arrivedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startInspection();
            }
        });
        /**
         * Set button to end trip and set associated filers
         */
        endTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransportAPIClient.getInstance().endTrip(activity);
            }
        });
        filter = new IntentFilter(TransportAPIClient.ENDED_TRIP);
        this.registerReceiver(receiver, filter);
        /**
         * Set button to logout user
         */
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout.setVisibility(View.INVISIBLE);
                logoutUser();
            }
        });


        /**
         * Set location retrieval
         */
        filter = new IntentFilter(TransportLocationHandler.LOCATION_ADDED);     // Current Location retrived
        this.registerReceiver(receiver, filter);
        onTrip = false;
        sendingLocation = false;
        locationHandler = new TransportLocationHandler(this);
        locationHandler.start();
        /**
         * Set up map
         */
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        mapController = map.getController();
        mapController.setZoom(18);
        currentMarker = new Marker(map);
        currentMarker.setIcon(getResources().getDrawable(R.drawable.truck));
        currentMarker.setTitle("Current location");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            /**
             * Handles GPS permissions
             */
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) { // double-check permission
                        enableBT();
                    } else {
                        UIHandler.showToast(this, this, "Location permission not confirmed. Exiting.");
                        this.finish();
                    }
                } else {
                    UIHandler.showToast(this, this, "Location permission not granted. Exiting.");
                    this.finish();
                }
                break;
        }
    }

    /**
     * Enables bluetooth to start authentication
     */
    private void enableBT() {
        /**
         * Turn on bluetooth
         */
        if (BluetoothAdapter.getDefaultAdapter() == null) {
            UIHandler.showToast(this, this, "Bluetooth is not available. Exiting.");
            this.finish();
        }
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, BluetoothController.REQUEST_ENABLE_BT);
        } else {
            authUser();
        }
    }

    /**
     * Create activity for authentication
     */
    private void authUser() {
        Intent authUser = new Intent(this, AuthActivity.class);
        startActivityForResult(authUser, AuthActivity.REQUEST_AUTH);
    }

    /**
     * Prepare for trip creation
     */
    private void userAuthenticated() {
        /**
         * Setup Bluetooth controller with given username
         */

        user.setText(getResources().getString(R.string.user) + " " +
                TransportDataHolder.getInstance().getUser().getId().toString());
        user.setVisibility(View.VISIBLE);
        //Log: UserCreated,UserId
        EvaluationRecorder.getInstance().writeLine("TransportUserCreated," +
                TransportDataHolder.getInstance().getUser().getId().toString());

        /**
         * show create trip button
         */
        createTrip.setVisibility(View.VISIBLE);
    }

    /**
     * Create trip
     */
    private void createTrip() {
        locationHandler.stop();
        Intent create = new Intent(this, CreateTripActivity.class);
        startActivityForResult(create, CreateTripActivity.REQUEST_TRIP_CREATION);
    }

    private void tripCreated(boolean startNow) {
        logout.setVisibility(View.INVISIBLE);
        locationHandler.start();
        // show your trip id is....
        if (startNow) {
            startTrip();
        }
    }

    private void getTrip() {    // used when scheduled
        //sets trip as active or api client does that?
        startTrip();
    }

    private void startTrip() {
        TransportAPIClient.getInstance().initializeTrip(this);
        currentTrip.setText(getResources().getString(R.string.current_trip) + " " +
                TransportDataHolder.getInstance().getActiveTrip().getId());
        currentTrip.setVisibility(View.VISIBLE);
    }

    private void startInspection() {
        sendingLocation = true;     // hold sending location
        Intent doInspection = new Intent(this, InspectionActivity.class);
        startActivityForResult(doInspection, InspectionActivity.REQUEST_INSPECTION);
    }

    private void resumeTrip() {
        TransportDataHolder.getInstance().setCurrentInspection(null);
        selected.setVisibility(View.INVISIBLE);
        endTrip.setVisibility(View.VISIBLE);            // Allow user to end trip
        arrivedButton.setVisibility(View.INVISIBLE);
        inspectionMarker = null;
        map.getOverlays().clear();
        map.getOverlays().add(currentMarker);
        map.setVisibility(View.VISIBLE);
        sendingLocation = false;                    // Unblock sending location
    }

    private void logoutUser() {
        user.setText(getResources().getString(R.string.user));
        user.setVisibility(View.INVISIBLE);
        createTrip.setVisibility(View.INVISIBLE);
        TransportDataHolder.getInstance().setUser(null);
        TransportDataHolder.getInstance().setActiveTrip(null);
        TransportDataHolder.getInstance().setCurrentInspectPublicKey(null);
        TransportDataHolder.getInstance().setCurrentInspection(null);
        authUser();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            /**
             * Do inspection
             */
            case InspectionActivity.REQUEST_INSPECTION:
                if (resultCode == Activity.RESULT_OK) {
                    resumeTrip();
                } else {
                    // handle bad inspection
                }
                break;
            /**
             * Create trip request
             */
            case CreateTripActivity.REQUEST_TRIP_CREATION:
                //get trip id of trip to start
                if (resultCode == Activity.RESULT_OK) {
                    tripCreated(data.getLongExtra(CreateTripActivity.RESULT_TRIP_ID, 0)
                            != 0);
                } else {
                    // not really possible as CreateTrip only finishes when trip is created.
                    createTrip.setVisibility(View.VISIBLE); // user can retry
                }
                break;
            /**
             * Auth user request
             */
            case AuthActivity.REQUEST_AUTH:
                if (resultCode == Activity.RESULT_OK) {
                    /**
                     * Prepare for trip creation
                     */
                    userAuthenticated();
                } else {
                    // not really possible as AuthActivity only finishes when authenticated.
                    UIHandler.showToast(this, this, "Activity error. Exiting.");
                    this.finish();
                }
                break;
            /**
             * Enable Bluetooth request
             */
            case BluetoothController.REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    authUser();
                } else {
                    UIHandler.showToast(this, this, "Bluetooth enable request not granted. Exiting.");
                    this.finish();
                }
                break;
        }
    }

    public void ensureDiscoverable() {                                  //  Not used
        if (BluetoothAdapter.getDefaultAdapter().getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, BluetoothController.DISCOVERABLE_TIME);
            this.startActivity(discoverableIntent);
        }
        // what if not approved?
    }

    private final BroadcastReceiver receiver = new TransportReceiver(this);
    private class TransportReceiver extends BroadcastReceiver {
        private Activity activity;
        public TransportReceiver(Activity activity) {
            super();
            this.activity = activity;
        }
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            /**
             * Trip initialization and continuation
             */
            if (TransportAPIClient.START_TRIP.equals(action)) {
                startTrip.setVisibility(View.INVISIBLE);    // Hide start trip button
                endTrip.setVisibility(View.VISIBLE);        // Allow user to end trip
                onTrip = true;                              // Set on Trip mode
                //Log: TripStarted,TripId
                EvaluationRecorder.getInstance().writeLine("TripStarted," +
                                TransportDataHolder.getInstance().getActiveTrip().getId());
                TransportAPIClient.getInstance().locateTrip(activity);      // Send first location point to Central Ledger and save in chain
            } else if (TransportLocationHandler.LOCATION_ADDED.equals(action)) {
                if (!onTrip) {
                    map.setVisibility(View.VISIBLE);
                } else if (onTrip && !sendingLocation) {       // If on trip mode and not sending location
                    EvaluationDataHolder.getInstance().sendPointToCL.run();
                    TransportAPIClient.getInstance().locateTrip(activity);      // Send location point to Central Ledger
                    //TransportAPIClient.getInstance().checkTripInspection(activity);
                    sendingLocation = true;             // Hold sending location
                }
                /**
                 * Update Map
                 */
                Location location = TransportDataHolder.getInstance().getLastLocation();
                GeoPoint startPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                currentMarker.setPosition(startPoint);
                currentMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                map.getOverlays().clear();
                map.getOverlays().add(currentMarker);
                if (inspectionMarker != null) {
                    map.getOverlays().add(inspectionMarker);
                }
                mapController.setCenter(startPoint);
            } else if (TransportAPIClient.SENT_LOCATION.equals(action)) {
                EvaluationDataHolder.getInstance().sendPointToCL.stop();
                //Log: PointSentToCL,TripId
                EvaluationRecorder.getInstance().writeLine("PointSentToCL," +
                        TransportDataHolder.getInstance().getActiveTrip().getId() +
                        "," + EvaluationDataHolder.getInstance().sendPointToCL.getDifference());
                sendingLocation = false;        // Unblock sending location
                EvaluationDataHolder.getInstance().selectionTransport.run();
                TransportAPIClient.getInstance().checkTripInspection(activity);     // Check Trip Inspection
            } else if (TransportAPIClient.LOCATION_NOT_SENT.equals(action)) {
                sendingLocation = false;        // Unblock sending location, despite device offline
            } else if (TransportAPIClient.NOT_SELECTED.equals(action)) {
                EvaluationDataHolder.getInstance().selectionTransport.stop();
                arrivedButton.setVisibility(View.INVISIBLE);        // Ensure arrived Button is hidden (Temp)
            }
            /**
             * Trip selection and inspection
             */
            else if (TransportAPIClient.SELECTED.equals(action)) {
                EvaluationDataHolder.getInstance().selectionTransport.stop();
                //Log: SelectForInspection,TripId,InspectionId,Time
                EvaluationRecorder.getInstance().writeLine("SelectedForInspection," +
                         TransportDataHolder.getInstance().getActiveTrip().getId() +
                        ","  + TransportDataHolder.getInstance().getCurrentInspection().getInspectionId() +
                        "," + EvaluationDataHolder.getInstance().selectionTransport.getDifference() + "ms");
                arrivedButton.setVisibility(View.VISIBLE);          // Allow user to report on location TEMP
                endTrip.setVisibility(View.INVISIBLE);              // Disallow user to end trip
                //sendingLocation = true;         // Hold sending location
                /**
                 * Put market of inspection location
                 */
                Coordinates inspection = TransportDataHolder.getInstance().getCurrentInspection().getCheckpoint().getCoordinates();
                GeoPoint inspectionPoint = new GeoPoint(inspection.getLatitude(), inspection.getLongitude());
                inspectionMarker = new Marker(map);
                inspectionMarker.setPosition(inspectionPoint);
                inspectionMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                inspectionMarker.setTitle("Inspection location");
                inspectionMarker.setIcon(getResources().getDrawable(R.drawable.inspector));
                map.getOverlays().add(inspectionMarker);
                map.invalidate();           // refresh map
                /**
                 * Inform user
                 */
                selected.setVisibility(View.VISIBLE);
                selected.setText(getResources().getString(R.string.selected)
                        + " by Inspector " +
                        TransportDataHolder.getInstance().getCurrentInspection().getInspectP());
                transportBluetoothController.setBluetoothName(TransportDataHolder.getInstance().getCurrentInspection().getTransportP());
            }
            /**
             * End of trip
             */
            else if (TransportAPIClient.ENDED_TRIP.equals(action)) {
                currentTrip.setText(getResources().getString(R.string.current_trip));
                currentTrip.setVisibility(View.INVISIBLE);
                endTrip.setVisibility(View.INVISIBLE);
                sendingLocation = false;
                createTrip.setVisibility(View.VISIBLE);     // Allow user to create new trip
                onTrip = false;
                logout.setVisibility(View.VISIBLE);
                //Log: TripEnded,TripId
                EvaluationRecorder.getInstance().writeLine("TripEnded," +
                        TransportDataHolder.getInstance().getActiveTrip().getId());
                TransportDataHolder.getInstance().setActiveTrip(null);
            }
        }
    }
}
