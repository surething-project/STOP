package pt.ulisboa.tecnico.captor.captorapplibrary.breceivers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import pt.ulisboa.tecnico.captor.captorapplibrary.bluetooth.InspectBluetoothController;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.cryptography.CryptographyUtil;
import pt.ulisboa.tecnico.captor.captorapplibrary.holders.InspectDataHolder;
import pt.ulisboa.tecnico.captor.captorapplibrary.location.InspectLocationHandler;
import pt.ulisboa.tecnico.captor.captorapplibrary.location.LocationHandler;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.location.Coordinates;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.inspection.Inspection;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.inspection.InspectorLocationProof;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.inspection.InspectorLocationProofMessage;

/**
 * This receiver is executed when the inspector approves the inspection.
 * It sends the proof to the transporter device through the bluetooth socket
 * Notifies activity when proof is sent via Intent
 */
public class InspectionApprovalReceiver extends BroadcastReceiver {

    public static final String PROOF_SENT_TO_TRANSPORT = "Proof sent to transport";
    public static final String PROOF_NOT_SENT_TO_TRANSPORT = "Proof not sent to transport";

    private static final String TAG = "InspectionApproval";

    private Context context;
    private OutputStream outStream;
    private Activity activity;

    public InspectionApprovalReceiver(Context context, Activity activity, OutputStream outStream) {
        this.context = context;
        this.outStream = outStream;
        this.activity = activity;
    }

    @Override
    public void onReceive(Context contextReceived, Intent intentReceived) {
        String action = intentReceived.getAction();

        if (InspectBluetoothController.INSPECTION_APPROVED.equals(action)) {
            /**
             * Retrieve current location
             */
            IntentFilter filter = new IntentFilter(InspectLocationHandler.LOCATION_ADDED);
            context.registerReceiver(new locationReceiver(activity), filter);
            LocationHandler locationHandler = new InspectLocationHandler(activity);
            locationHandler.start();
            context.unregisterReceiver(this);
        }
    }

    private class locationReceiver extends BroadcastReceiver {
        private Activity activity;

        public locationReceiver(Activity activity) {
            super();
            this.activity = activity;
        }

        @Override
        public void onReceive(Context contextReceived, Intent intentReceived) {
            String action = intentReceived.getAction();

            if (InspectLocationHandler.LOCATION_ADDED.equals(action)) {
                /**
                 * Get current location
                 */
                Location l = InspectDataHolder.getInstance().getLastLocation();
                /**
                 * Create proof
                 */
                Inspection inspection = InspectDataHolder.getInstance().getActiveInspection();
                InspectorLocationProofMessage pm = new InspectorLocationProofMessage(inspection.getTransportP(),
                        inspection.getInspectP(),
                        inspection.getInspectionId(),
                        inspection.getTrip().getId(),
                        inspection.getNonce(),
                        new Coordinates(l.getLatitude(), l.getLongitude()));
                pm.setAccuracy(l.getAccuracy());        // To evaluate location accuracy
                /**
                    * Sign proof
                */
                String signature = CryptographyUtil.signObject(pm, InspectDataHolder.getInstance().getUser().getKeyPair().getPrivate());
                InspectorLocationProof proof = new InspectorLocationProof(
                        pm, signature, InspectDataHolder.getInstance().getSessionId(), InspectDataHolder.getInstance().getProverId(),
                        InspectDataHolder.getInstance().getCCid(), InspectDataHolder.getInstance().getCitizenCardSignature(),
                        InspectDataHolder.getInstance().getPublicKey()
                );
                /**
                 * Update local inspection object
                 */
                inspection.setProof(proof);
                InspectDataHolder.getInstance().setActiveInspection(inspection);
                /**
                 * Encrypt proof before sending
                 */
                Object obj = CryptographyUtil.encryptObject(InspectDataHolder.getInstance().getCurrentTransportPublicKey(), proof);
                /**
                 * Send proof
                 */
                try {
                    ObjectOutputStream oos = new ObjectOutputStream(outStream);
                    oos.writeObject(obj);
                    /**
                     * Broadcast Proof sent to transport device
                     */
                    Intent intent = new Intent(PROOF_SENT_TO_TRANSPORT);
                    activity.sendBroadcast(intent);
                } catch (IOException e) {
                    Log.e(TAG, "Error occurred when sending data", e);
                    // broadcast error
                    Intent intent = new Intent(PROOF_NOT_SENT_TO_TRANSPORT);
                    activity.sendBroadcast(intent);
                }
                context.unregisterReceiver(this);
            }
        }
    }
}
