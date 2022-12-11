package pt.ulisboa.tecnico.captor.captorsharedlibrary.trip;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

import com.fasterxml.jackson.annotation.JsonProperty;

import pt.ulisboa.tecnico.captor.captorsharedlibrary.location.Coordinates;

/**
 * Location Point that contains GPS coordinates retrieved by device
 */
@Embeddable
public class LocationPoint extends LP implements Serializable {
	
	@Embedded
	@JsonProperty("coordinates")
	private Coordinates coordinates;
	@JsonProperty("timestamp")
	private String timestamp;
	@JsonProperty("accuracy")
	private float accuracy;		// To evaluate location accuracy
	
	public LocationPoint() {
		coordinates = new Coordinates();
	} 
	
	public LocationPoint(Coordinates coordinates) {
		super();
		this.coordinates = coordinates;
		this.timestamp = new SimpleDateFormat("yyyyMMddHH:mm:ss").format(new Date());
	}

	@Override
	public Coordinates getLocation() {
		return this.coordinates;
	}
	
	@JsonProperty("coordinates")
	public Coordinates getCoordinates() {
		return coordinates;
	}
	public void setCoordinates(Coordinates coordinates) {
		this.coordinates = coordinates;
	}
	
	@JsonProperty("timestamp")
	@Override
	public String getTimestamp() {
		return this.timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	@JsonProperty("accuracy")
	public float getAccuracy() {
		return accuracy;
	}
	public void setAccuracy(float accuracy) {
		this.accuracy = accuracy;
	}

}
