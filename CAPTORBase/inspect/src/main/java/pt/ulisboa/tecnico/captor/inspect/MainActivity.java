package pt.ulisboa.tecnico.captor.inspect;

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
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.nio.charset.StandardCharsets;

import pt.ulisboa.tecnico.captor.captorapplibrary.bluetooth.BluetoothController;
import pt.ulisboa.tecnico.captor.captorapplibrary.bluetooth.InspectBluetoothController;
import pt.ulisboa.tecnico.captor.captorapplibrary.breceivers.InspectionApprovalReceiver;
import pt.ulisboa.tecnico.captor.captorapplibrary.clclients.InspectAPIClient;
import pt.ulisboa.tecnico.captor.captorapplibrary.clclients.NotaryAPIClient;
import pt.ulisboa.tecnico.captor.captorapplibrary.clclients.QscdAPIClient;
import pt.ulisboa.tecnico.captor.captorapplibrary.evaluation.EvaluationRecorder;
import pt.ulisboa.tecnico.captor.captorapplibrary.holders.InspectDataHolder;
import pt.ulisboa.tecnico.captor.captorapplibrary.holders.TransportDataHolder;
import pt.ulisboa.tecnico.captor.captorapplibrary.location.InspectLocationHandler;
import pt.ulisboa.tecnico.captor.captorapplibrary.ui.UIHandler;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.evaluation.EvaluationDataHolder;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.location.Coordinates;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    public static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 31;
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 51;

    private InspectBluetoothController btController;
    private InspectLocationHandler locationHandler;
    private Button startButton;
    private Button scanQrCodeButton;
    private Button approveButton;
    private Button createButton;
    private Button cancelButton;
    private TextView currentStatusUI;
    private boolean checkpointCreated;
    private boolean activeInspection;
    private boolean transportFound;
    private boolean retrying;
    private MapView map;
    private IMapController mapController;
    private Marker checkpointMarker;
    private Marker vehicleMarker;
    private Marker currentPosition;
    private TextView progress;
    private TextView selected;
    private TextView username;
    private TextView checkpoint;
    private TextView inspection;
    private Activity mainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = this;

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
         * Set window in portrait and always on
         */
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        /**
         * Set up Osmodroid
         */
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        /**
         * Prepare variables
         */
        IntentFilter filter;
        final Activity activity = this;
        currentStatusUI = findViewById(R.id.current_status);
        startButton = findViewById(R.id.create_inspection_button);
        scanQrCodeButton = findViewById(R.id.scanQrCode);
        btController = new InspectBluetoothController(getApplicationContext(), this);
        checkpointCreated = false;
        activeInspection = false;
        transportFound = false;
        retrying = false;
        progress = findViewById(R.id.progress);
        username = findViewById(R.id.info);
        selected = findViewById(R.id.selected);
        checkpoint = findViewById(R.id.checkpointID);
        inspection = findViewById(R.id.inspectionID);
        /**
         * Create checkpoint and set associated filter
         */
        createButton = findViewById(R.id.createCheckpoint);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkpointCreated = true;
                createButton.setVisibility(View.INVISIBLE);
                progress.setVisibility(View.VISIBLE);
                InspectAPIClient.getInstance().addCheckpoint(activity);
            }
        });
        filter = new IntentFilter(InspectAPIClient.CHECKPOINT_CREATED);
        this.registerReceiver(receiver, filter);
        filter = new IntentFilter(InspectAPIClient.CHECKPOINT_NOT_CREATED);
        this.registerReceiver(receiver, filter);
        /**
         * Create inspection and prepare associated filter
         */
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startButton.setVisibility(View.INVISIBLE);
                progress.setVisibility(View.VISIBLE);
                EvaluationDataHolder.getInstance().selectionInspect.run();

                InspectAPIClient.getInstance().addInspection(activity);
            }
        });
        filter = new IntentFilter(InspectAPIClient.INSPECTION_CREATED);
        this.registerReceiver(receiver, filter);
        filter = new IntentFilter(InspectAPIClient.INSPECTION_NOT_CREATED);
        this.registerReceiver(receiver, filter);
        filter = new IntentFilter(InspectAPIClient.CURRENT_INSPECTION_UPDATED);
        this.registerReceiver(receiver, filter);
        filter = new IntentFilter(InspectAPIClient.CURRENT_INSPECTION_NOT_UPDATED);
        this.registerReceiver(receiver, filter);
        /**
         * Scan QRCode and prepare associated filter
         */
        scanQrCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanQrCodeButton.setVisibility(View.INVISIBLE);
                IntentIntegrator intentIntegrator = new IntentIntegrator(mainActivity);
                intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
                intentIntegrator.setPrompt("QR Code");
                intentIntegrator.setOrientationLocked(false);
                intentIntegrator.setCameraId(0);
                intentIntegrator.initiateScan();
            }
        });
        /**
         * Set filters for finding transport device
         */
        filter = new IntentFilter(InspectBluetoothController.LISTENING);
        this.registerReceiver(receiver, filter);
        filter = new IntentFilter(InspectBluetoothController.TRANSPORT_FOUND);
        this.registerReceiver(receiver, filter);
        filter = new IntentFilter(InspectBluetoothController.PROOF_REQUEST_RECEIVED);
        this.registerReceiver(receiver, filter);
        /**
         * Approve inspection
         */
        approveButton = (Button) findViewById(R.id.approve_inspection_button);
        approveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EvaluationDataHolder.getInstance().approval.stop();
                //Log: InspectionApproved,CheckpointId,InspectionId,Time
                EvaluationRecorder.getInstance().writeLine("InspectionApproved," +
                         InspectDataHolder.getInstance().getActiveCheckpoint().getCheckpointId() +
                        "," + InspectDataHolder.getInstance().getActiveInspection().getInspectionId() +
                        "," + EvaluationDataHolder.getInstance().approval.getDifference() + "ms");
                EvaluationDataHolder.getInstance().sendProofToTransport.run();
                approveButton.setVisibility(View.INVISIBLE);
                Intent intent = new Intent(InspectBluetoothController.INSPECTION_APPROVED);
                MainActivity.this.sendBroadcast(intent);
                currentStatusUI.setText(R.string.approved);
            }
        });
        filter = new IntentFilter(InspectionApprovalReceiver.PROOF_SENT_TO_TRANSPORT);
        this.registerReceiver(receiver, filter);
        filter = new IntentFilter(InspectionApprovalReceiver.PROOF_NOT_SENT_TO_TRANSPORT);
        this.registerReceiver(receiver, filter);
        filter = new IntentFilter(InspectAPIClient.INSPECTION_ENDED);
        this.registerReceiver(receiver, filter);
        filter = new IntentFilter(InspectAPIClient.INSPECTION_NOT_ENDED);
        this.registerReceiver(receiver, filter);
        filter = new IntentFilter(QscdAPIClient.SESSION_FINISHED);
        this.registerReceiver(receiver, filter);
        /**
         * Cancel inspection in case something goes wrong
         */
        cancelButton = findViewById(R.id.force);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelButton.setVisibility(View.INVISIBLE);
                //Log: InspectionCanceled,CheckpointId,InspectionId
                EvaluationRecorder.getInstance().writeLine("InspectionCanceled," +
                        InspectDataHolder.getInstance().getActiveCheckpoint().getCheckpointId() +
                        "," + InspectDataHolder.getInstance().getActiveInspection().getInspectionId());
                InspectDataHolder.getInstance().setActiveInspection(null);
                inspection.setVisibility(View.INVISIBLE);
                startButton.setVisibility(View.VISIBLE);
                scanQrCodeButton.setVisibility(View.INVISIBLE);
                approveButton.setVisibility(View.INVISIBLE);
                currentStatusUI.setVisibility(View.INVISIBLE);
                selected.setVisibility(View.INVISIBLE);
                findViewById(R.id.status).setVisibility(View.INVISIBLE);
                stopDiscoverable();         // To stop bluetooth discoverable
                UIHandler.showToast(activity, getApplicationContext(), "Inspection canceled");
                locationHandler.start();
                activeInspection = false;
                transportFound = false;

                /**
                 * Warning: this does not change the state of the inspection to closed in the system.
                 *  There is a endpoint operation for that in the CentralLedger
                 */

            }
        });

        /**
         * Set location
         */
        filter = new IntentFilter(InspectLocationHandler.LOCATION_ADDED);
        this.registerReceiver(receiver, filter);
        locationHandler = new InspectLocationHandler(this);
        locationHandler.start();

        /**
         * Set up map
         */
        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        mapController = map.getController();
        mapController.setZoom(20);
        checkpointMarker = new Marker(map);
        checkpointMarker.setTitle("Checkpoint location");
        checkpointMarker.setIcon(getResources().getDrawable(R.drawable.destination));
        currentPosition = new Marker(map);
        currentPosition.setTitle("Current location");
        currentPosition.setIcon(getResources().getDrawable(R.drawable.inspector));
        vehicleMarker = new Marker(map);
        vehicleMarker.setTitle("Selected vehicle");
        vehicleMarker.setIcon(getResources().getDrawable(R.drawable.truck));
    }

    public void ensureDiscoverable() {
        if (BluetoothAdapter.getDefaultAdapter().getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, BluetoothController.DISCOVERABLE_TIME);
            this.startActivity(discoverableIntent);
        }
        // what if not approved?
    }

    public void stopDiscoverable() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,1);    // only known way of stopping discoverable...
        startActivity(discoverableIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                UIHandler.showToast(this, this, "Scanning cancelled!");
                scanQrCodeButton.setVisibility(View.VISIBLE);

            } else {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);

                String resultString = new String(result.getContents().getBytes(), StandardCharsets.UTF_8);
                JsonObject jsonObject = new Gson().fromJson(resultString, JsonObject.class);

                String ccID = jsonObject.get("ccID").toString().replace("\"", "");
                String sessionId = jsonObject.get("sessionId").toString().replace("\"", "");
                String proverId = jsonObject.get("proverId").toString().replace("\"", "");
                String token = jsonObject.get("token").toString().replace("\"", "");
                String citizenCardSignature = jsonObject.get("citizenCardSignature").toString().replace("\"", "");

                InspectDataHolder.getInstance().setCCid(ccID);
                InspectDataHolder.getInstance().setSessionId(sessionId);
                InspectDataHolder.getInstance().setProverId(proverId);
                InspectDataHolder.getInstance().setToken(token);
                InspectDataHolder.getInstance().setCitizenCardSignature(citizenCardSignature);

                QscdAPIClient.getInstance().setToken(token);

                Log.d(TAG, "Citizen Card Signature: " + citizenCardSignature);
                UIHandler.showToast(this, this, "Citizen Card Scanned");
            }
        }

        switch(requestCode) {
            case AuthActivity.REQUEST_AUTH:
                if (resultCode == Activity.RESULT_OK) {
                    /**
                     * Prepare for checkpoint creation
                     */
                    userAuthenticated();
                } else {
                    // not really possible as AuthActivity only finishes when authenticated.
                    UIHandler.showToast(this, this, "Activity error. Exiting.");
                    this.finish();
                }
                break;
            case BluetoothController.REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    //startButton.setVisibility(View.VISIBLE);
                    //scanQrCodeButton.setVisibility(View.VISIBLE);
                    authUser();
                } else {
                    UIHandler.showToast(this, this, "Bluetooth enable request not granted. Exiting.");
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
     * Prepare for checkpoint creation
     */
    private void userAuthenticated() {
        String id = InspectDataHolder.getInstance().getUser().getId().toString();

        username.setText(getResources().getString(R.string.username) + " " + id);
        username.setVisibility(View.VISIBLE);

        //Log: InspectUser,UserId
        EvaluationRecorder.getInstance().writeLine("InspectUserCreated," +
                        id);
        //locationHandler.start();
        //createButton.setVisibility(View.VISIBLE); This is done in location added
    }

    private final BroadcastReceiver receiver = new MyReceiver(this);
    private class MyReceiver extends BroadcastReceiver {
        private Activity activity;

        public MyReceiver(Activity activity) {
            super();
            this.activity = activity;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            /**
             * Handle location retrieved
             */
            if (InspectLocationHandler.LOCATION_ADDED.equals(action)) {
                if (!checkpointCreated) {
                    createButton.setVisibility(View.VISIBLE);
                    progress.setVisibility(View.INVISIBLE);
                }
                /**
                 * Focus map on current location
                 */
                Location location = InspectDataHolder.getInstance().getLastLocation();
                GeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());
                mapController.setCenter(point);
                currentPosition.setPosition(point);
                map.getOverlays().clear();
                map.getOverlays().add(currentPosition);
                if (checkpointCreated) {
                    map.getOverlays().add(checkpointMarker);
                }
                if (activeInspection && !transportFound) {
                    map.getOverlays().add(vehicleMarker);                           // Add old location anyway
                    InspectAPIClient.getInstance().showInspection(activity);        // Get location of selected vehicle
                }
                map.invalidate();       // refresh map
            }
            /**
             * Checkpoint created
             */
            else if (InspectAPIClient.CHECKPOINT_CREATED.equals(action)) {

                //Log: CheckpointCreated,CheckpointId
                EvaluationRecorder.getInstance().writeLine("CheckpointCreated," +
                                InspectDataHolder.getInstance().getActiveCheckpoint().getCheckpointId());

                //locationHandler.stop();
                Coordinates coordinates = InspectDataHolder.getInstance().getActiveCheckpoint().getCoordinates();
                GeoPoint checkpointPoint = new GeoPoint(coordinates.getLatitude(),coordinates.getLongitude());
                checkpointMarker.setPosition(checkpointPoint);
                checkpointMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);

                map.getOverlays().add(checkpointMarker);
                mapController.setCenter(checkpointPoint);
                progress.setVisibility(View.INVISIBLE);
                startButton.setVisibility(View.VISIBLE);
                scanQrCodeButton.setVisibility(View.INVISIBLE);

                checkpoint.setText(getResources().getString(R.string.checkpoint) + " " + InspectDataHolder.getInstance().getActiveCheckpoint().getCheckpointId());
                checkpoint.setVisibility(View.VISIBLE);
            } else if (InspectAPIClient.CHECKPOINT_NOT_CREATED.equals(action)) {
                checkpointCreated = false;
                progress.setVisibility(View.INVISIBLE);
                createButton.setVisibility(View.VISIBLE);
                UIHandler.showToast(MainActivity.this, getApplicationContext(), "Checkpoint could not be created. Please try again");
            }
            /**
             * Inspection created
             */
            else if (InspectAPIClient.INSPECTION_CREATED.equals(action)) {
                EvaluationDataHolder.getInstance().selectionInspect.stop();
                EvaluationDataHolder.getInstance().timeToCheckpoint.run();
                //Log: TransportSelected,CheckpointId,InspectionId,Time
                EvaluationRecorder.getInstance().writeLine("TransportSelected," +
                         InspectDataHolder.getInstance().getActiveCheckpoint().getCheckpointId() +
                        "," + InspectDataHolder.getInstance().getActiveInspection().getInspectionId() +
                        "," + EvaluationDataHolder.getInstance().selectionInspect.getDifference() + "ms");
                btController.setBluetoothName(InspectDataHolder.getInstance().getActiveInspection().getInspectP());
                ensureDiscoverable();
                progress.setVisibility(View.INVISIBLE);
                currentStatusUI.setVisibility(View.VISIBLE);
                findViewById(R.id.status).setVisibility(View.VISIBLE);
                btController.startInspection();
                scanQrCodeButton.setVisibility(View.VISIBLE);
                selected.setText(getResources().getString(R.string.selected) + " " +
                        InspectDataHolder.getInstance().getActiveInspection().getTransportP());
                selected.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.VISIBLE);
                activeInspection = true;

                inspection.setText(getResources().getString(R.string.inspection) + " " + InspectDataHolder.getInstance().getActiveInspection().getInspectionId());
                inspection.setVisibility(View.VISIBLE);


            } else if (InspectAPIClient.INSPECTION_NOT_CREATED.equals(action)) {
                EvaluationDataHolder.getInstance().selectionInspect.stop();
                progress.setVisibility(View.INVISIBLE);
                startButton.setVisibility(View.VISIBLE);
                scanQrCodeButton.setVisibility(View.INVISIBLE);
                UIHandler.showToast(MainActivity.this, getApplicationContext(), "Inspection could not be created. No vehicles available. Please try again");
            } else if (InspectAPIClient.CURRENT_INSPECTION_UPDATED.equals(action)) {
                Coordinates coordinates = InspectDataHolder.getInstance().getActiveInspection().getTrip().getLastLocation();
                GeoPoint point = new GeoPoint(coordinates.getLatitude(), coordinates.getLongitude());
                vehicleMarker.setPosition(point);
                vehicleMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                map.getOverlays().clear();
                map.getOverlays().add(currentPosition);
                map.getOverlays().add(checkpointMarker);
                map.getOverlays().add(vehicleMarker);
                map.invalidate();
            } else if (InspectAPIClient.CURRENT_INSPECTION_NOT_UPDATED.equals(action)) {
                // ignore for now
            }
            /**
             * Bluetooth communication
             */
            else if (InspectBluetoothController.LISTENING.equals(action)) {
                currentStatusUI.setText(R.string.waiting_transport);
            } else if (InspectBluetoothController.TRANSPORT_FOUND.equals(action)) {
                EvaluationDataHolder.getInstance().timeToCheckpoint.stop();
                EvaluationDataHolder.getInstance().receiveProofRequest.run();
                //Log: TimeToCheckpoint,CheckpointId,InspectionId,Time
                EvaluationRecorder.getInstance().writeLine("TimeToCheckpoint," +
                        InspectDataHolder.getInstance().getActiveCheckpoint().getCheckpointId() +
                        "," + InspectDataHolder.getInstance().getActiveInspection().getInspectionId() +
                        "," + EvaluationDataHolder.getInstance().timeToCheckpoint.getDifference() + "ms");
                transportFound = true;
                locationHandler.stop();
                currentStatusUI.setText(R.string.transport_found); // inform transport found
            } else if (InspectBluetoothController.PROOF_REQUEST_RECEIVED.equals(action)) {
                EvaluationDataHolder.getInstance().receiveProofRequest.stop();
                //Log: ProofRequestReceived,CheckpointId,InspectionId,Time
                EvaluationRecorder.getInstance().writeLine("ProofRequestReceived," +
                         InspectDataHolder.getInstance().getActiveCheckpoint().getCheckpointId() +
                        "," + InspectDataHolder.getInstance().getActiveInspection().getInspectionId() +
                        "," + EvaluationDataHolder.getInstance().receiveProofRequest.getDifference() + "ms");
                EvaluationDataHolder.getInstance().approval.run();
                currentStatusUI.setText(R.string.request_received); // inform received proof
                /**
                 * Put button visible for generating proof
                 */
                approveButton.setVisibility(View.VISIBLE);
            } else if (InspectionApprovalReceiver.PROOF_SENT_TO_TRANSPORT.equals(action)) {
                UIHandler.showToast(activity, getApplicationContext(), "Proof Sent to Transport");

                EvaluationDataHolder.getInstance().sendProofToTransport.stop();
                EvaluationDataHolder.getInstance().sendInspectorProofToCL.run();
                //Log: ProofSentToTransport,CheckpointId,InspectionId,Time
                EvaluationRecorder.getInstance().writeLine("ProofSentToTransport," +
                        InspectDataHolder.getInstance().getActiveCheckpoint().getCheckpointId() +
                        "," + InspectDataHolder.getInstance().getActiveInspection().getInspectionId() +
                        "," + EvaluationDataHolder.getInstance().sendProofToTransport.getDifference() + "ms");
                /**
                 * Send proof to ledger
                 */
                InspectAPIClient.getInstance().updateInspection(activity);

                // Timer
            } else if (InspectionApprovalReceiver.PROOF_NOT_SENT_TO_TRANSPORT.equals(action)) {
                EvaluationDataHolder.getInstance().sendProofToTransport.stop();
                //Log: ProofSentToTransport,CheckpointId,InspectionId,Time
                EvaluationRecorder.getInstance().writeLine("ProofNotSentToTransport," +
                        InspectDataHolder.getInstance().getActiveCheckpoint().getCheckpointId() +
                        "," + InspectDataHolder.getInstance().getActiveInspection().getInspectionId() +
                        "," + EvaluationDataHolder.getInstance().sendProofToTransport.getDifference() + "ms");
                currentStatusUI.setText("Proof not sent to Transport.");
                /**
                 * Resetting inspection to be implemented
                  */
                //InspectAPIClient.getInstance().updateInspection(activity);
            }
            /**
             * Proof sent to ledger
             */
            else if (InspectAPIClient.INSPECTION_ENDED.equals(action)) {
                EvaluationDataHolder.getInstance().sendInspectorProofToCL.stop();
                //Log: InspectorProofSentToCL,CheckpointId,InspectionId,Time
                EvaluationRecorder.getInstance().writeLine("InspectorProofSentToCL," +
                        InspectDataHolder.getInstance().getActiveCheckpoint().getCheckpointId() +
                        "," + InspectDataHolder.getInstance().getActiveInspection().getInspectionId() +
                        "," + EvaluationDataHolder.getInstance().sendInspectorProofToCL.getDifference() + "ms");
                InspectDataHolder.getInstance().setActiveInspection(null);
                inspection.setVisibility(View.INVISIBLE);
                startButton.setVisibility(View.VISIBLE);
                scanQrCodeButton.setVisibility(View.INVISIBLE);
                approveButton.setVisibility(View.INVISIBLE);
                cancelButton.setVisibility(View.INVISIBLE);
                currentStatusUI.setVisibility(View.INVISIBLE);
                selected.setVisibility(View.INVISIBLE);
                findViewById(R.id.status).setVisibility(View.INVISIBLE);
                stopDiscoverable();         // To stop bluetooth discoverable
                UIHandler.showToast(activity, getApplicationContext(), "Inspection ended successfully");
                locationHandler.start();
                activeInspection = false;
                transportFound = false;

                // Finish Session
                QscdAPIClient.getInstance().finishSession(activity);
                InspectDataHolder.getInstance().setCCid(null);

            } else if (QscdAPIClient.SESSION_FINISHED.equals(action)) {
                // Create Proof
                String token = QscdAPIClient.getInstance().getToken();
                NotaryAPIClient.getInstance().setToken(token);
                NotaryAPIClient.getInstance().createProof(activity);
                UIHandler.showToast(activity, getApplicationContext(), "Proof created successfully");

            } else if (InspectAPIClient.INSPECTION_NOT_ENDED.equals(action)) {
                if (retrying) {
                    EvaluationDataHolder.getInstance().sendInspectorProofToCL.stop();
                    //Log: InspectorProofNotSentToCL,CheckpointId,InspectionId,Time
                    EvaluationRecorder.getInstance().writeLine("InspectorProofNotSentToCL," +
                            InspectDataHolder.getInstance().getActiveCheckpoint().getCheckpointId() +
                            "," + InspectDataHolder.getInstance().getActiveInspection().getInspectionId() +
                            "," + EvaluationDataHolder.getInstance().sendInspectorProofToCL.getDifference() + "ms");
                    /**
                     * Exiting inspection anyway, despite error
                     */
                    InspectDataHolder.getInstance().setActiveInspection(null);
                    inspection.setVisibility(View.INVISIBLE);
                    startButton.setVisibility(View.VISIBLE);
                    scanQrCodeButton.setVisibility(View.INVISIBLE);
                    approveButton.setVisibility(View.INVISIBLE);
                    cancelButton.setVisibility(View.INVISIBLE);
                    currentStatusUI.setVisibility(View.INVISIBLE);
                    selected.setVisibility(View.INVISIBLE);
                    findViewById(R.id.status).setVisibility(View.INVISIBLE);
                    stopDiscoverable();         // To stop bluetooth discoverable
                    UIHandler.showToast(activity, getApplicationContext(), "Inspection ended with error");
                    locationHandler.start();
                    activeInspection = false;
                    transportFound = false;
                } else {
                    retrying = true;
                    currentStatusUI.setText("Could not send proof to Central Ledger. Retrying");
                    InspectAPIClient.getInstance().updateInspection(activity);
                }
            }
        }
    };
}