package pt.ulisboa.tecnico.captor.captorapplibrary.holders;

import android.location.Location;

import java.security.PublicKey;
import java.util.ArrayList;

import pt.ulisboa.tecnico.captor.captorapplibrary.proofs.ProofRequest;
import pt.ulisboa.tecnico.captor.captorapplibrary.user.TransportUser;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.inspection.Inspection;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.trip.LocationChainItem;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.trip.Trip;

/**
 * DataHolder for the Transport app
 */
public class TransportDataHolder {
    private static final String TAG = "TransportDataHolder";

    /**
     * TransportDataHolder instance
     */
    private static TransportDataHolder dataHolder = null;

    /**
     * List of LocationProof requests generated
     */
    private ArrayList<ProofRequest> proofRequests = new ArrayList<ProofRequest>();

    /**
     * List of proofs received
     */
    private ArrayList<LocationChainItem> proofs = new ArrayList<LocationChainItem>();

    /**
     * Last location collected, updated by TransportLocationHandler
     */
    private Location lastLocation;

    /**
     * Current user using the app
     */
    private TransportUser user;

    /**
     * Active trip on app
     */
    private Trip activeTrip;

    /**
     * Active inspection selection
     */
    private Inspection currentInspection;

    /**
     * Public key of inspector of active inspection
     */
    private PublicKey currentInspectPublicKey;

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

    protected TransportDataHolder() {}

    public static TransportDataHolder getInstance() {
        if (dataHolder == null) {
            dataHolder = new TransportDataHolder();
        }
        return dataHolder;
    }

    public TransportUser getUser() {
        return user;
    }

    public void setUser(TransportUser user) {
        this.user = user;
    }

    public Trip getActiveTrip() {
        return activeTrip;
    }

    public void setActiveTrip(Trip activeTrip) {
        this.activeTrip = activeTrip;
    }

    public Inspection getCurrentInspection() {
        return currentInspection;
    }

    public void setCurrentInspection(Inspection currentInspection) {
        this.currentInspection = currentInspection;
    }

    public void addProofRequest(ProofRequest pr) {
        proofRequests.add(pr);
    }

    public void addProof(LocationChainItem p) {
        proofs.add(p);
    }

    public LocationChainItem getLastProof() {
        if (proofs.isEmpty()) {
            return null;
        }
        return proofs.get(proofs.size()-1);
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(Location lastLocation) {
        this.lastLocation = lastLocation;
    }

    public PublicKey getCurrentInspectPublicKey() {
        return currentInspectPublicKey;
    }

    public void setCurrentInspectPublicKey(PublicKey currentInspectPublicKey) {
        this.currentInspectPublicKey = currentInspectPublicKey;
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
