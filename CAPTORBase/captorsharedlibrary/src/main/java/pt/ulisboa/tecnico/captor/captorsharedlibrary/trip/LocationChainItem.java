package pt.ulisboa.tecnico.captor.captorsharedlibrary.trip;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name="location_chain_items")
public class LocationChainItem {
	
	@Id
	@GeneratedValue
	@JsonIgnore
	private Long id;
	
	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name="previousLocationSignature", column=@Column(name="point_previousLocationSignature", columnDefinition="TEXT")),
		@AttributeOverride(name="number", column=@Column(name="point_number")),
		@AttributeOverride(name="tripId", column=@Column(name="point_tripId"))
	})
	@JsonProperty("location")
	private LocationPoint location;
	@Column(columnDefinition="TEXT")
	@JsonProperty("transportSignature")
	@NotNull
	private String transportSignature;
	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name="previousLocationSignature", column=@Column(name="proof_previousLocationSignature", columnDefinition="TEXT")),
		@AttributeOverride(name="number", column=@Column(name="proof_number")),
		@AttributeOverride(name="tripId", column=@Column(name="proof_tripId"))
	})
	@JsonProperty("locationProof")
	private LocationProof locationProof;
	@JsonProperty("isProof")
	private boolean isProof;
	
	public LocationChainItem() {
		locationProof = new LocationProof();
		location = new LocationPoint();
	} 
	
	@JsonProperty("location")
	public LocationPoint getLocation() {
		return location;
	}
	public void setLocation(LocationPoint location) {
		this.location = location;
	}
	
	@JsonProperty("transportSignature")
	@NotNull
	public String getTransportSignature() {
		return transportSignature;
	}
	public void setTransportSignature(String transportSignature) {
		this.transportSignature = transportSignature;
	}

	@JsonProperty("locationProof")
	public LocationProof getLocationProof() {
		return locationProof;
	}
	public void setLocationProof(LocationProof locationProof) {
		this.locationProof = locationProof;
	}

	@JsonProperty("isProof")
	public boolean isProof() {
		return isProof;
	}
	public void setProof(boolean isProof) {
		this.isProof = isProof;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
