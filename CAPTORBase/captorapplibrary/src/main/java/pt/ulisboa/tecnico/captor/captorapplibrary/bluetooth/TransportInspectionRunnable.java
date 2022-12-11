package pt.ulisboa.tecnico.captor.captorapplibrary.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import pt.ulisboa.tecnico.captor.captorsharedlibrary.cryptography.CryptographyUtil;
import pt.ulisboa.tecnico.captor.captorapplibrary.holders.TransportDataHolder;
import pt.ulisboa.tecnico.captor.captorapplibrary.proofs.ProofRequest;
import pt.ulisboa.tecnico.captor.captorapplibrary.proofs.SignedProofRequest;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.location.Coordinates;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.inspection.InspectorLocationProof;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.trip.LocationChainItem;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.trip.LocationProof;

/**
 * TransportInspectionRunnable:
 *      Runnable executed by the Transport device during an inspection
 *
 */
public class TransportInspectionRunnable implements Runnable {

    /**
     * Tag used for logs
     */
    private String TAG = "TransportInspectionRunnable";

    /**
     * Variables for bluetooth communication
     */
    private BluetoothSocket bluetoothSocket;
    private BluetoothDevice bluetoothDevice;
    private boolean isConnected;
    private Context context;

    public TransportInspectionRunnable(Context context, BluetoothDevice device) {
        this.isConnected = false;
        this.bluetoothDevice = device;
        this.context = context;
    }

    @Override
    public void run() {
        connectToDevice();
    }

    /**
     * Connects to bluetooth device
     */
    private void connectToDevice() {
        try {
            bluetoothSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(BluetoothController.APP_BT_UUID);
        } catch (IOException e) {
            Log.e(TAG, "Socket create with device failed");
        }
        try {
            bluetoothSocket.connect();
            isConnected = true;
        } catch (IOException e) {
            Log.e(TAG, "Connection attempt with device failed");
            // close socket ?
        }
        finally {
            if (isConnected) {
                Intent intent = new Intent(TransportBluetoothController.CONNECTED_TO_INSPECTOR);
                context.sendBroadcast(intent);      // Inform UI of connection
                // Send proof request
                Thread getProof = new Thread(new GetProofFromInspection());
                getProof.start();
            } else {
                // Broadcast problem to UI
                Intent intent = new Intent(TransportBluetoothController.CONNECTION_PROBLEM);
                context.sendBroadcast(intent);      // Inform UI of connection problem
            }
        }
    }

    /**
     * Sends proof request, receives proof and adds to location Chain
     */
    public class GetProofFromInspection implements Runnable {

        private InputStream inStream;
        private OutputStream outStream;

        public GetProofFromInspection() {
            setupStreams();
        }

        @Override
        public void run() {
            // create/get proof request / We assume TransportLocationHandler is running
            Location l = TransportDataHolder.getInstance().getLastLocation();
            ProofRequest pr = new ProofRequest(TransportDataHolder.getInstance().getCurrentInspection().getTransportP(),
                    TransportDataHolder.getInstance().getCurrentInspection().getInspectionId(),
                    TransportDataHolder.getInstance().getActiveTrip().getId(),
                    TransportDataHolder.getInstance().getCurrentInspection().getNonce(),
                    new Coordinates(l.getLatitude(), l.getLongitude()));
            // send request
            sendProofRequest(pr);

            // wait and receive request
            receiveProof();
        }

        public void setupStreams() {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = bluetoothSocket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            inStream = tmpIn;
            outStream = tmpOut;
        }

        private void sendProofRequest(ProofRequest pr) {
            /**
             * Sign proofRequest
             */
            SignedProofRequest signed = new SignedProofRequest(
                    CryptographyUtil.signObject(pr, TransportDataHolder.getInstance().getUser().getKeyPair().getPrivate()),
                    pr);
            /**
             * Encrypt object with public key of inspector
             */
            Object obj = CryptographyUtil.encryptObject(TransportDataHolder.getInstance().getCurrentInspectPublicKey(), signed);
            try {
                /**
                 * Send encrypted object
                 */
                ObjectOutputStream oos = new ObjectOutputStream(outStream);
                oos.writeObject(obj);
                Intent intent = new Intent(TransportBluetoothController.SENT_REQUEST);
                context.sendBroadcast(intent);      // Inform UI of connection
            } catch (IOException e) {
                Log.e(TAG, "Could not send proof request", e);
            }
        }

        private void receiveProof() {
            /**
             * Receive from object input stream
             */
            ObjectInputStream ois = null;
            Object o = null;
            try {
                ois = new ObjectInputStream(inStream);
                o = ois.readObject();

            } catch (IOException | ClassNotFoundException e) {
                Log.e(TAG, "Could not read from inputStream", e);
                try {
                    bluetoothSocket.close();
                } catch (Exception ex) {
                    // do nothing
                }
                // Broadcast problem to UI
                Intent intent = new Intent(TransportBluetoothController.CONNECTION_PROBLEM);
                context.sendBroadcast(intent);      // Inform UI of connection problem
                return;
            }

            /**
             * Decrypt object
             */
            o = CryptographyUtil.decryptObject(TransportDataHolder.getInstance().getUser().getKeyPair().getPrivate(), o);
            /**
             * Handle error as in bad response?
             */

            /**
             * Close socket, validate and add proof to Location chain
             */
            InspectorLocationProof inspectorLocationProof = (InspectorLocationProof) o;
            Log.d(TAG, inspectorLocationProof.toString());
            TransportDataHolder.getInstance().setSessionId(inspectorLocationProof.getSessionId());
            TransportDataHolder.getInstance().setProverId(inspectorLocationProof.getProverId());
            TransportDataHolder.getInstance().setCCid(inspectorLocationProof.getCcId());
            TransportDataHolder.getInstance().setCitizenCardSignature(inspectorLocationProof.getCitizenCardSignature());
            TransportDataHolder.getInstance().setPublicKey(inspectorLocationProof.getPublicKey());
            addProofToLocationChain(inspectorLocationProof);
        }

        private void addProofToLocationChain(InspectorLocationProof p) {
            /**
             * Close socket after proof was received
             */
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            /**
             * Validate signature of inspector proof
             */
            if (!CryptographyUtil.verifySignature(p.getProofMessage(),
                    TransportDataHolder.getInstance().getCurrentInspectPublicKey(),
                    p.getSignature())) {
                Log.d(TAG, "Signature not valid");
                // broadcast error
                return;
            }

            /**
             * Create own proof
             */
            LocationProof proof = new LocationProof();
            proof.setInspectorLocationProof(p);
            proof.setNumber(TransportDataHolder.getInstance().getActiveTrip().getNextLPNumber());
            /**
             * Set up hash
             */
            proof.setPreviousLocationSignature(TransportDataHolder.getInstance().getActiveTrip().getLastHash());
            proof.setTripId(TransportDataHolder.getInstance().getActiveTrip().getId());
            /**
             * Add to location chain and to last proof
             */
            LocationChainItem item = new LocationChainItem();
            item.setLocationProof(proof);
            item.setProof(true);
            item.setTransportSignature(CryptographyUtil.signObject(proof,
                    TransportDataHolder.getInstance().getUser().getKeyPair().getPrivate()));
            TransportDataHolder.getInstance().getActiveTrip().addLocationChainItem(item);
            TransportDataHolder.getInstance().addProof(item);
            /**
             * Broadcast proof saved. It will be sent to Central Ledger
             */
            Intent intent = new Intent(TransportBluetoothController.RECEIVED_PROOF);
            context.sendBroadcast(intent);      // Inform UI of connection
        }
    }
}
