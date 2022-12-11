package pt.ulisboa.tecnico.captor.captorapplibrary.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.IntentFilter;

import pt.ulisboa.tecnico.captor.captorapplibrary.breceivers.BluetoothSearchReceiver;

public class TransportBluetoothController extends BluetoothController {

    public static final String INSPECTOR_NOT_FOUND = "Inspector Not Found";
    public static final String INSPECTOR_FOUND = "Inspector Found";
    public static final String CONNECTION_PROBLEM = "Connection Problem";
    public static final String CONNECTED_TO_INSPECTOR = "Connected to Inspector";
    public static final String SENT_REQUEST = "Sent Request";
    public static final String RECEIVED_PROOF = "Received Proof";

    public TransportBluetoothController (Context context) {
        super(context);
    }

    public void findInspector(String inspectorName) {

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        context.registerReceiver(new BluetoothSearchReceiver(context, bluetoothAdapter, inspectorName), intentFilter);

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();      // Start Bluetooth scan
    }
}
