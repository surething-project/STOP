package pt.ulisboa.tecnico.captor.captorapplibrary.breceivers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import pt.ulisboa.tecnico.captor.captorapplibrary.bluetooth.TransportBluetoothController;
import pt.ulisboa.tecnico.captor.captorapplibrary.bluetooth.TransportInspectionRunnable;

/**
 * BluetoothSearchReceiver:
 *      Finds device and starts communication with it
 */
public class BluetoothSearchReceiver extends BroadcastReceiver {

    private String TAG = "BluetoothSearchReceiver";

    private String deviceName;
    private BluetoothAdapter bluetoothAdapter;
    private boolean foundDevice;
    private Context context;


    public BluetoothSearchReceiver(Context context, BluetoothAdapter bluetoothAdapter, String deviceName) {
        this.deviceName = deviceName;
        this.bluetoothAdapter = bluetoothAdapter;
        this.foundDevice = false;
        this.context = context;
    }

    @Override
    public void onReceive(Context contextReceived, Intent intentReceived) {
        String action = intentReceived.getAction();

        if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
            Log.d(TAG, "Started INSPECTOR discovery");
        } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            BluetoothDevice device = intentReceived.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Log.d(TAG, "Device found: " + device.getAddress());
            if (device.getName() != null) {
                Log.d(TAG, "Device found: " + device.getName());
            }
            //If it is Inspector
            if (!foundDevice && device.getName() != null && device.getName().equals(deviceName)) {
                foundDevice = true;
                bluetoothAdapter.cancelDiscovery();
                // Broadcast found Inspector
                Intent intent = new Intent(TransportBluetoothController.INSPECTOR_FOUND);
                context.sendBroadcast(intent);      // Inform UI of Inspector found
                Thread inspectorCommunication = new Thread(new TransportInspectionRunnable(context, device));
                inspectorCommunication.start();     // Start communication with inspect
                context.unregisterReceiver(this);
            }
        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
            Log.d(TAG, "Unregister Peer Discovery Receiver");
            if (!foundDevice) {       // Did not find inspector
                Intent intent = new Intent(TransportBluetoothController.INSPECTOR_NOT_FOUND);
                context.sendBroadcast(intent);      // Inform UI of Inspector not found
            }
            context.unregisterReceiver(this);
        }
    }
}
