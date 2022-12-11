package pt.ulisboa.tecnico.captor.captorapplibrary.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import pt.ulisboa.tecnico.captor.captorapplibrary.breceivers.InspectionApprovalReceiver;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.cryptography.CryptographyUtil;
import pt.ulisboa.tecnico.captor.captorapplibrary.holders.InspectDataHolder;
import pt.ulisboa.tecnico.captor.captorapplibrary.proofs.ProofRequest;
import pt.ulisboa.tecnico.captor.captorapplibrary.proofs.SignedProofRequest;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.inspection.InspectorLocationProof;

/**
 * Creates a bluetooth socket to receive bluetooth connections and receives and validates proof requests.
 * Notifies activty that proof request was received via Intent
 * and registers InspectionApprovalReceiver to send proof to transport when inspector approves inspection
 *
 * Currently it is created by InspectBluetoothController
 */
public class InspectRunnable implements Runnable {

    private String TAG = "InspectRunnable";

    private BluetoothServerSocket serverSocket;
    private BluetoothAdapter bluetoothAdapter;
    private boolean listening;
    private Context context;
    private Activity activity;

    public InspectRunnable(Context context, Activity activity) {
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.listening = false;
        this.context = context;
        this.activity = activity;
    }

    @Override
    public void run() {
        acceptConnections();        // Start accept thread
    }

    public void quit() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void acceptConnections() {
        BluetoothServerSocket temp = null;
        try {
            temp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(BluetoothController.BT_SERVICE_NAME, BluetoothController.APP_BT_UUID);
            listening = true;
        } catch (IOException e) {
            Log.e(TAG, "listen method failed", e);
        }
        serverSocket = temp;

        Intent intent = new Intent(InspectBluetoothController.LISTENING);
        context.sendBroadcast(intent);

        while (listening) {
            BluetoothSocket socket = null;
            try {
                socket = serverSocket.accept();
                // Check if correct device connected
                if (socket.getRemoteDevice().getName() != null &&
                        socket.getRemoteDevice().getName().equals(InspectDataHolder.getInstance().getActiveInspection().getTransportP())) {
                    // Broadcast to UI
                    intent = new Intent(InspectBluetoothController.TRANSPORT_FOUND);
                    context.sendBroadcast(intent);
                    // Start new thread responsible for handling connection
                    Thread receiveRequest = new Thread(new ReceiveRequest(socket));
                    receiveRequest.start();
                    quit();
                }
            } catch (IOException e) {
                Log.e(TAG, "accept failed", e);
                break;
            }
        }
    }

    private class ReceiveRequest implements Runnable {

        private BluetoothSocket socket;
        private InputStream inStream;
        private OutputStream outStream;

        public ReceiveRequest(BluetoothSocket socket) {
            this.socket = socket;
        }

        public void run() {
            // receive request object
            setupStreams();
            receiveProofRequest();
        }

        public void setupStreams() {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            inStream = tmpIn;
            outStream = tmpOut;
        }

        public void receiveProofRequest() {
            ObjectInputStream ois = null;
            Object o = null;
            try {
                ois = new ObjectInputStream(inStream);
                o = ois.readObject();

            } catch (IOException | ClassNotFoundException e) {
                Log.e(TAG, "Could not read from inputStream", e);
                //close socket ?
            }
            if (o != null) {
                /**
                 * Decrypt received object
                 */
                o = CryptographyUtil.decryptObject(InspectDataHolder.getInstance().getUser().getKeyPair().getPrivate(),
                        o);
                /**
                 * Validate signature
                 */
                SignedProofRequest signedProofRequest = (SignedProofRequest) o;
                if (!CryptographyUtil.verifySignature(signedProofRequest.getProofRequest(),
                        InspectDataHolder.getInstance().getCurrentTransportPublicKey(),
                        signedProofRequest.getSignature())) {
                    rejectRequest();
                    return;
                }
                ProofRequest pr = signedProofRequest.getProofRequest();
                /**
                 * Validate nonce
                 */
                // validate nonce
                // if nonce repeated, reject
                /**
                 * validate pseudonyms and ids
                 */
                // validate inspect pseudonyms
                // validate transport pseudonyms
                // validate transport id
                // validate inspect id
                if (InspectDataHolder.getInstance().addProofRequest(pr)) {    //logic should not be here
                    /**
                     * broadcast to UI proof request received
                     */
                    Intent intent = new Intent(InspectBluetoothController.PROOF_REQUEST_RECEIVED);
                    context.sendBroadcast(intent);
                    /**
                     * Register broadcast receiver of inspection approved
                     */
                    IntentFilter filter = new IntentFilter(InspectBluetoothController.INSPECTION_APPROVED);
                    context.registerReceiver(new InspectionApprovalReceiver(context, activity, outStream), filter);
                    /**
                     * Proof is sent by this receiver
                     */
                } else { // not possible to add proof request
                    return;
                }
            }
        }
        private void rejectRequest() {
            Log.i(TAG, "Request received not valid");
            try {
                ObjectOutputStream oos = new ObjectOutputStream(outStream);
                oos.writeObject(new InspectorLocationProof(null, null, null, null, null, null, null));
            } catch (IOException e) {
                Log.e(TAG, "Could not send error", e);
            }
            try {
                socket.close();
                Log.i(TAG, "Closed socket with bad proof request");
            } catch (IOException e) {
                Log.e(TAG, "Could not close unwanted socket", e);
            }
            // broadcast error ?
        }
    }



    /**
     * Proof is sent in pt.ulisboa.tecnico.captor.captorapplibrary.breceivers.InspectionApprovalReceiver
     */
}
