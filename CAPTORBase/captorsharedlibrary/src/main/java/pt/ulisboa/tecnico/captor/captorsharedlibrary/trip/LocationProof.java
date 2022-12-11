package pt.ulisboa.tecnico.captor.captorsharedlibrary.trip;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

import pt.ulisboa.tecnico.captor.captorsharedlibrary.inspection.InspectorLocationProof;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.location.Coordinates;

@Embeddable
public class LocationProof extends LP implements Serializable {
	
	@Embedded
	@JsonProperty("inspectorLocationProof")
	private InspectorLocationProof inspectorLocationProof;
	
	public LocationProof() {
		inspectorLocationProof = new InspectorLocationProof();
	}; 

	public LocationProof(Long tripId, InspectorLocationProof inspectorLocationProof) {
		super();
		this.inspectorLocationProof = inspectorLocationProof;
		setTripId(tripId);
		
	}

	@Override
	public Coordinates getLocation() {
		return inspectorLocationProof.getProofMessage().getCoordinates();
	}

	@Override
	public String getTimestamp() {
		return inspectorLocationProof.getProofMessage().getTimestamp();
	}

	@JsonProperty("inspectorLocationProof")
	public InspectorLocationProof getInspectorLocationProof() {
		return inspectorLocationProof;
	}
	public void setInspectorLocationProof(InspectorLocationProof inspectorLocationProof) {
		this.inspectorLocationProof = inspectorLocationProof;
	}
}
