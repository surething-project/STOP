package pt.ulisboa.tecnico.captor.captorsharedlibrary.inspection;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import pt.ulisboa.tecnico.captor.captorsharedlibrary.location.Coordinates;

/**
 * Inspector Proof that contains all the parameters
 */
@Embeddable
public class InspectorLocationProofMessage implements Serializable {

	@JsonProperty("transport")
    private String transport;
	@JsonProperty("inspect")
    private String inspect;
	@JsonProperty("inspectionId")
    private Long inspectionId;
	@JsonProperty("tripId")
    private Long tripId;
	@JsonProperty("nonce")
    private int nonce;
	@Column(name = "proof_timestamp")
	@JsonProperty("timestamp")
    private String timestamp;
	@Embedded
	@AttributeOverrides(value = {
            @AttributeOverride(name = "latitude", column = @Column(name = "proof_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "proof_longitude"))
        })
	@JsonProperty("coordinates")
    private Coordinates coordinates;
	@Column(name = "proof_accuracy")
    @JsonProperty("accuracy")
    private float accuracy;     // To evaluate location accuracy

    public InspectorLocationProofMessage() {
    	coordinates = new Coordinates();
    }
    
    public InspectorLocationProofMessage(String transport, String inspect, Long inspectionId, Long tripId, int nonce, Coordinates coordinates) {
        this.transport = transport;
        this.inspect = inspect;
        this.inspectionId = inspectionId;
        this.tripId = tripId;
        this.nonce = nonce;
        this.coordinates = coordinates;
        this.timestamp = new SimpleDateFormat("yyyyMMddHH:mm:ss").format(new Date());
    }

    @JsonProperty("transport")
    public String getTransport() {
        return transport;
    }
    public void setTransport(String transport) {
		this.transport = transport;
	}
    
    @JsonProperty("inspect")
    public String getInspect() {
        return inspect;
    }
    public void setInspect(String inspect) {
		this.inspect = inspect;
	}
    
    @JsonProperty("inspectionId")
    public Long getInspectionId() {
        return inspectionId;
    }
    public void setInspectionId(Long inspectionId) {
		this.inspectionId = inspectionId;
	}
    
    @JsonProperty("tripId")
    public Long getTripId() {
        return tripId;
    }
    public void setTripId(Long tripId) {
		this.tripId = tripId;
	}

    @JsonProperty("nonce")
    public int getNonce() {
        return nonce;
    }
    public void setNonce(int nonce) {
		this.nonce = nonce;
	}

    @JsonProperty("timestamp")
    public String getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

    @JsonProperty("coordinates")
    public Coordinates getCoordinates() {
        return coordinates;
    }
    public void setCoordinates(Coordinates coordinates) {
		this.coordinates = coordinates;
	}

	@JsonProperty("accuracy")
    public float getAccuracy() {
        return accuracy;
    }
    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }
}
