package pt.ulisboa.tecnico.captor.captorapplibrary.proofs;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import pt.ulisboa.tecnico.captor.captorsharedlibrary.location.Coordinates;

public class ProofRequest implements Serializable {

    private String transport;
    private Long inspectionID;
    private Long tripID;
    private int nonce;
    private String timestamp;
    private Coordinates coordinates;

    public ProofRequest(String transport, Long inspectionID, Long tripID, int nonce, Coordinates coordinates) {
        this.transport = transport;
        this.inspectionID = inspectionID;
        this.tripID = tripID;
        this.nonce = nonce;
        this.coordinates = coordinates;
        this.timestamp = new SimpleDateFormat("yyyyMMddHH:mm:ss").format(new Date());
    }

    public String getTransport() {
        return transport;
    }

    public Long getInspectionID() {
        return inspectionID;
    }

    public Long getTripID() {
        return tripID;
    }

    public int getNonce() {
        return nonce;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }
}
