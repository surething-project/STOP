package pt.ulisboa.tecnico.captor.captorapplibrary.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;

import java.util.UUID;

/**
 * BluetoothController:
 *  Abstract class that controls bluetooth communication
 *  and contains global variables for Bluetooth communication
 *  Currently used by InspectBluetoothController and TransportBluetoothController of same package
 */
public abstract class BluetoothController {

    public static final UUID APP_BT_UUID = UUID.fromString("123486bd-8548-4c1b-ba95-d4ad64ecaa45");
    public static final String BT_SERVICE_NAME = "CAPTOR_BT";
    public static final int DISCOVERABLE_TIME = 3600; // Maximum time Android allows
    public static final int REQUEST_ENABLE_BT = 2;

    protected BluetoothAdapter bluetoothAdapter;
    protected Context context;

    public BluetoothController (Context context) {
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.context = context;
    }

    /**
     * Sets name of bluetooth on device
     * @param name name desired
     */
    public void setBluetoothName(String name) {
        this.bluetoothAdapter.setName(name);
    }
}
