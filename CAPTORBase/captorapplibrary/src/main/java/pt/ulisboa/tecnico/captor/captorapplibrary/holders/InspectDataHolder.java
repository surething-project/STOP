package pt.ulisboa.tecnico.captor.captorapplibrary.holders;

import android.location.Location;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;

import pt.ulisboa.tecnico.captor.captorapplibrary.proofs.ProofRequest;
import pt.ulisboa.tecnico.captor.captorapplibrary.user.InspectUser;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.inspection.Checkpoint;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.inspection.Inspection;

/**
 * DataHolder for the Inspect app
 */
public class InspectDataHolder {

    /**
     * InspectDataHolder instance
     */
    private static InspectDataHolder inspectDataHolderInstance = null;

    /**
     * Key pair of the Inspect device to be used for encryption
     */
    private KeyPair keyPair = null;

    /**
     * List of proof request received, updated by InspectRunnable
     */
    private ArrayList<ProofRequest> proofRequests = new ArrayList<ProofRequest>();

    /**
     * Last location collected, updated by InspectLocationHandler
     */
    private Location lastLocation;

    /**
     * Current user on app
     */
    private InspectUser user;

    /**
     * Active checkpoint
     */
    private Checkpoint activeCheckpoint;

    /**
     * Active inspection
     */
    private Inspection activeInspection;

    /**
     * Public Key of the transport to be inspected
     */
    private PublicKey currentTransportPublicKey;

    /**
     * CCid of the current session
     */
    private String ccid;

    /**
     * Id of the current session
     */
    private String sessionId;

    /**
     * Prover Id of the current session
     */
    private String proverId;

    /**
     * Token of the current session
     */
    private String token;

    /**
     * Citizen Card Signature of the current session
     */
    private String citizenCardSignature;

    /**
     * Citizen Card Public Key of the current session
     */
    private String publicKey;

    protected InspectDataHolder() {}

    public static InspectDataHolder getInstance() {
        if (inspectDataHolderInstance == null) {
            inspectDataHolderInstance = new InspectDataHolder();
        }
        return inspectDataHolderInstance;
    }

    public InspectUser getUser() {
        return user;
    }

    public void setUser(InspectUser user) {
        this.user = user;
    }

    public ProofRequest getLastProofRequest() {
        if (proofRequests.isEmpty()) {
            return null;
        }
        return proofRequests.get(proofRequests.size()-1);
    }

    public boolean addProofRequest(ProofRequest pr) {
        // validate proof request
        proofRequests.add(pr);
        return true;
    }

    public Checkpoint getActiveCheckpoint() {
        return activeCheckpoint;
    }

    public void setActiveCheckpoint(Checkpoint activeCheckpoint) {
        this.activeCheckpoint = activeCheckpoint;
    }

    public Inspection getActiveInspection() {
        return activeInspection;
    }

    public void setActiveInspection(Inspection activeInspection) {
        this.activeInspection = activeInspection;
    }

    public PublicKey getCurrentTransportPublicKey() {
        return currentTransportPublicKey;
    }

    public void setCurrentTransportPublicKey(PublicKey currentTransportPublicKey) {
        this.currentTransportPublicKey = currentTransportPublicKey;
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(Location lastLocation) {
        this.lastLocation = lastLocation;
    }

    public String getCCid() { return ccid; }

    public void setCCid(String ccid) { this.ccid = ccid; }

    public String getSessionId() { return sessionId; }

    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getProverId() { return proverId; }

    public void setProverId(String proverId) { this.proverId = proverId; }

    public String getToken() { return token; }

    public void setToken(String token) { this.token = token; }

    public String getCitizenCardSignature() { return citizenCardSignature; }

    public void setCitizenCardSignature(String citizenCardSignature) { this.citizenCardSignature = citizenCardSignature; }

    public String getPublicKey() { return publicKey; }

    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }
}
