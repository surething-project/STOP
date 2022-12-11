package pt.ulisboa.tecnico.captor.captorsharedlibrary.trip;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.MappedSuperclass;

import pt.ulisboa.tecnico.captor.captorsharedlibrary.location.Coordinates;

@Embeddable
@MappedSuperclass
public class LP implements Serializable {
	@JsonProperty("previousLocationSignature")
	@Column(columnDefinition="TEXT")
	protected String previousLocationSignature;
	@JsonProperty("number")
	protected int number;
	@JsonProperty("tripId")
	protected Long tripId;
	
	public LP() {}

	@JsonProperty("previousLocationSignature")
	public String getPreviousLocationSignature() {
		return previousLocationSignature;
	}
	public void setPreviousLocationSignature(String previousLocationSignature) {
		this.previousLocationSignature = previousLocationSignature;
	}

	@JsonProperty("number")
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}

	@JsonProperty("tripId")
	public Long getTripId() {
		return tripId;
	}
	public void setTripId(Long tripId) {
		this.tripId = tripId;
	}
	
	@JsonIgnore
	public Coordinates getLocation() {return null;}
	
	public String getTimestamp() {return null;}

}
