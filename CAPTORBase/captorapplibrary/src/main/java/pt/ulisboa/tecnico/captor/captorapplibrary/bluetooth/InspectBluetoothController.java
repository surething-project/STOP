package pt.ulisboa.tecnico.captor.captorapplibrary.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;

/**
 * Handles discoverability of device and starts InspectRunnable to receive Bluetooth connections
 * Currently it is used by Inspect MainActivity
 */
public class InspectBluetoothController extends BluetoothController {

    public static final String LISTENING = "Listening";
    public static final String TRANSPORT_FOUND = "Transport Found";
    public static final String PROOF_REQUEST_RECEIVED = "Proof Request Received";
    public static final String INSPECTION_APPROVED = "Inspection Approved";

    private Activity activity;
    private Thread inspectListen;

    public InspectBluetoothController (Context context, Activity activity) {
        super(context);
        this.activity = activity;
    }

    public void startInspection() {
        //ensureDiscoverable();   // Enable bluetooth discovery

        // Start Inspect runnable
        inspectListen = new Thread(new InspectRunnable(context, activity));
        inspectListen.start();
    }

    public void ensureDiscoverable() {                  // Not used, this is implemented in the activity
        if (bluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, BluetoothController.DISCOVERABLE_TIME);
            context.startActivity(discoverableIntent);
        }
        // what if not approved?
    }
}
