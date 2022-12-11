package pt.ulisboa.tecnico.captor.transport;

import static pt.ulisboa.tecnico.captor.captorsharedlibrary.cryptography.CryptographyUtil.loadPrivateKey;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import pt.ulisboa.tecnico.captor.captorapplibrary.bluetooth.TransportBluetoothController;
import pt.ulisboa.tecnico.captor.captorapplibrary.clclients.QscdAPIClient;
import pt.ulisboa.tecnico.captor.captorapplibrary.clclients.TransportAPIClient;
import pt.ulisboa.tecnico.captor.captorapplibrary.evaluation.EvaluationRecorder;
import pt.ulisboa.tecnico.captor.captorapplibrary.holders.TransportDataHolder;
import pt.ulisboa.tecnico.captor.captorapplibrary.ui.UIHandler;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.evaluation.EvaluationDataHolder;

public class InspectionActivity extends AppCompatActivity {
    private static final String TAG = "InspectionActivity";

    public static final int REQUEST_INSPECTION = 123;

    private TransportBluetoothController transportBluetoothController;

    private TextView currentStatus;
    private TextView currentInstruction;
    private Button resume;
    private ImageView sign;
    private TextView info;
    private Button retry;
    private TextView inspId;
    private Button cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * Prepare window
         */
        setContentView(R.layout.activity_inspection);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        /**
         * Prepare variables
         */
        currentStatus = findViewById(R.id.current_status);
        currentInstruction = findViewById(R.id.current_instruction);
        resume = findViewById(R.id.resume_trip);
        sign = findViewById(R.id.sign);
        info = findViewById(R.id.info);
        retry = findViewById(R.id.retry);
        inspId = findViewById(R.id.inspectionID);
        cancel = findViewById(R.id.cancel);
        /**
         * Initialize Bluetooth Controller
         */
        transportBluetoothController = new TransportBluetoothController(getApplicationContext());


        IntentFilter filter = new IntentFilter(TransportBluetoothController.INSPECTOR_FOUND);    // Inspect Bluetooth device was found
        this.registerReceiver(receiver, filter);
        filter = new IntentFilter(TransportBluetoothController.INSPECTOR_NOT_FOUND);    // Inspect Bluetooth device was not found
        this.registerReceiver(receiver, filter);
        filter = new IntentFilter(TransportBluetoothController.CONNECTION_PROBLEM);     // Not possible to connect via Bluetooth
        this.registerReceiver(receiver, filter);
        filter = new IntentFilter(TransportBluetoothController.CONNECTED_TO_INSPECTOR);     // Bluetooth connection to Inspect device
        this.registerReceiver(receiver, filter);
        filter = new IntentFilter(TransportBluetoothController.SENT_REQUEST);       // Sent Proof request to Inspector
        this.registerReceiver(receiver, filter);
        filter = new IntentFilter(TransportBluetoothController.RECEIVED_PROOF);     // Received Proof from Inspector
        this.registerReceiver(receiver, filter);
        filter = new IntentFilter(TransportAPIClient.SENT_PROOF);       // Sent Proof to Central Ledger
        this.registerReceiver(receiver, filter);
        filter = new IntentFilter(TransportAPIClient.PROOF_NOT_SENT);       // Proof not sent to Central Ledger
        this.registerReceiver(receiver, filter);

        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retry.setVisibility(View.INVISIBLE);
                currentStatus.setText("Retrying...");
                transportBluetoothController.findInspector(TransportDataHolder.getInstance()
                        .getCurrentInspection().getInspectP());
            }
        });

        resume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIHandler.showToast(InspectionActivity.this, getApplicationContext(), "Inspection done. Resume trip.");
                setResult(RESULT_OK);
                finish();
            }
        });
        /**
         * For evaluation purposes
         */
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel.setVisibility(View.INVISIBLE);
                resume.setVisibility(View.VISIBLE);
            }
        });

        /**
         * Start searching for bluetooth inspector device
         */
        EvaluationDataHolder.getInstance().findInspectDevice.run();
        transportBluetoothController.findInspector(TransportDataHolder.getInstance()
                .getCurrentInspection().getInspectP());
        currentStatus.setText(R.string.scanning);
        info.setText("Inspector " + TransportDataHolder.getInstance().getCurrentInspection().getInspectP()
        + " will conduct the inspection.");
        currentInstruction.setText("stop the vehicle");
        inspId.setText("Inspection ID: " + TransportDataHolder.getInstance().getCurrentInspection().getInspectionId());
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TransportBluetoothController.INSPECTOR_FOUND.equals(action)) {
                EvaluationDataHolder.getInstance().findInspectDevice.stop();
                //Log: InspectDeviceFound,TripId,InspectionId,Time
                EvaluationRecorder.getInstance().writeLine("InspectDeviceFound," +
                        TransportDataHolder.getInstance().getActiveTrip().getId() +
                        ","  + TransportDataHolder.getInstance().getCurrentInspection().getInspectionId() +
                        "," + EvaluationDataHolder.getInstance().findInspectDevice.getDifference() + "ms");
                EvaluationDataHolder.getInstance().sendRequest.run();
                currentStatus.setText(R.string.found_inspector);        // Inform inspector was found
            } else if (TransportBluetoothController.INSPECTOR_NOT_FOUND.equals(action)) {
                EvaluationDataHolder.getInstance().findInspectDevice.stop();
                currentStatus.setText(R.string.no_inspector_found);                     // Inform inspector not found
                /**
                 * Show retry button
                 */
                currentInstruction.setText("retry the search of inspect device");
                retry.setVisibility(View.VISIBLE);
            } else if (TransportBluetoothController.CONNECTION_PROBLEM.equals(action)) {
                currentStatus.setText(R.string.connection_issue);                   // Inform connection problem
                /**
                 * Show retry button
                 */
                currentInstruction.setText("retry the search of inspect device");
                retry.setVisibility(View.VISIBLE);
            } else if (TransportBluetoothController.CONNECTED_TO_INSPECTOR.equals(action)) {
                currentStatus.setText(R.string.status);                                     // Show status text
            } else if (TransportBluetoothController.SENT_REQUEST.equals(action)) {
                EvaluationDataHolder.getInstance().sendRequest.stop();
                //Log: ProofRequestSent,TripId,InspectionId,Time
                EvaluationRecorder.getInstance().writeLine("ProofRequestSent," +
                         TransportDataHolder.getInstance().getActiveTrip().getId() +
                        ","  + TransportDataHolder.getInstance().getCurrentInspection().getInspectionId() +
                        "," + EvaluationDataHolder.getInstance().sendRequest.getDifference() + "ms");
                EvaluationDataHolder.getInstance().receiveProof.run();
                currentStatus.setText(R.string.sent_request);           // Inform connection problem
            } else if (TransportBluetoothController.RECEIVED_PROOF.equals(action)) {
                UIHandler.showToast(InspectionActivity.this, getApplicationContext(), "Proof Received from Inspection");

                EvaluationDataHolder.getInstance().receiveProof.stop();
                EvaluationDataHolder.getInstance().sendProofToCL.run();
                //Log: ProofReceived,TripId,InspectionId,Time
                EvaluationRecorder.getInstance().writeLine("ProofReceived," +
                        TransportDataHolder.getInstance().getActiveTrip().getId() +
                        ","  + TransportDataHolder.getInstance().getCurrentInspection().getInspectionId() +
                        "," + EvaluationDataHolder.getInstance().receiveProof.getDifference() + "ms");
                currentStatus.setText(R.string.proof_received);         // Inform proof received from inspect

                // Save necessary values for the request
                QscdAPIClient.getInstance().setSessionId(TransportDataHolder.getInstance().getSessionId());
                QscdAPIClient.getInstance().setProverId(TransportDataHolder.getInstance().getProverId());
                QscdAPIClient.getInstance().setCcId(TransportDataHolder.getInstance().getCCid());
                QscdAPIClient.getInstance().setCitizenCardSignature(TransportDataHolder.getInstance().getCitizenCardSignature());

                // Join Session and Register Citizen Card Signed
                QscdAPIClient.getInstance().joinSessionAndRegisterCitizenCardSigned(
                        InspectionActivity.this,
                        loadPrivateKey("transport_private_key.pem")
                );

                TransportAPIClient.getInstance().addProof(InspectionActivity.this);        // Save proof received

            } else if (TransportAPIClient.SENT_PROOF.equals(action)) {
                EvaluationDataHolder.getInstance().sendProofToCL.stop();
                //Log: ProofSentToCL,TripId,InspectionId,Time
                EvaluationRecorder.getInstance().writeLine("ProofSentToCL," +
                        TransportDataHolder.getInstance().getActiveTrip().getId() +
                        ","  + TransportDataHolder.getInstance().getCurrentInspection().getInspectionId() +
                        "," + EvaluationDataHolder.getInstance().sendProofToCL.getDifference() + "ms");
                // Inform user to continue trip
                /*endTrip.setVisibility(View.VISIBLE);            // Allow user to end trip
                arrivedButton.setVisibility(View.INVISIBLE);
                inspectionMarker = null;
                map.getOverlays().clear();
                map.getOverlays().add(currentMarker);
                map.setVisibility(View.VISIBLE);
                sendingLocation = false;                    // Unblock sending location
                */
                currentStatus.setText("Proof sent to Ledger!");
                currentInstruction.setText("prepare and resume trip");
                sign.setImageDrawable(getResources().getDrawable(R.drawable.go));
                resume.setVisibility(View.VISIBLE);
            } else if (TransportAPIClient.PROOF_NOT_SENT.equals(action)) {
                EvaluationDataHolder.getInstance().sendProofToCL.stop();
                //Log: ProofNotSentToCL,TripId,InspectionId,Time
                EvaluationRecorder.getInstance().writeLine("ProofNotSentToCL," +
                        TransportDataHolder.getInstance().getActiveTrip().getId() +
                        ","  + TransportDataHolder.getInstance().getCurrentInspection().getInspectionId() +
                        "," + EvaluationDataHolder.getInstance().sendProofToCL.getDifference() + "ms");
                UIHandler.showToast(InspectionActivity.this, getApplicationContext(), "Proof not sent to Ledger. Error");
                resume.setVisibility(View.VISIBLE);
            }
        }
    };
}
