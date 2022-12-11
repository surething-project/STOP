package pt.ulisboa.tecnico.captor.captorsharedlibrary.inspection;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import javax.validation.constraints.*;

import pt.ulisboa.tecnico.captor.captorsharedlibrary.trip.Trip;

@Entity
@Table(name = "inspections")
public class Inspection {
	@ManyToOne
	@JoinColumn(name="checkpoint_id")
	@JsonProperty("checkpoint")
	private Checkpoint checkpoint = null;

	@Id
	@GeneratedValue
	@Column(name = "id")
	@JsonProperty("inspectionId")
	private Long inspectionId;

	@ManyToOne
	@JoinColumn(name="trip_id")
	@JsonProperty("trip")
	private Trip trip;

	@JsonProperty("nonce")
	@Column(name="generated_nonce")
	private int nonce = 0;
	
	@JsonProperty("proof")
	private InspectorLocationProof proof;
	
	@JsonProperty("inspectP")
	private String inspectP;
	
	@JsonProperty("transportP")
	private String transportP;
	
	
	public Inspection() {
		proof = new InspectorLocationProof();
	}

	/**
	 * Get checkpoint
	 * 
	 * @return checkpoint
	 **/
	@JsonProperty("checkpoint")
	@NotNull
	public Checkpoint getCheckpoint() {
		return checkpoint;
	}

	public void setCheckpoint(Checkpoint checkpoint) {
		this.checkpoint = checkpoint;
	}

	/**
	 * Get inspectionId
	 * 
	 * @return inspectionId
	 **/
	@JsonProperty("inspectionId")
	@NotNull
	public Long getInspectionId() {
		return inspectionId;
	}

	public void setInspectionId(Long inspectionId) {
		this.inspectionId = inspectionId;
	}

	/**
	 * Get nonce
	 * 
	 * @return nonce
	 **/
	@JsonProperty("nonce")
	@NotNull
	public int getNonce() {
		return nonce;
	}

	public void setNonce(int nonce) {
		this.nonce = nonce;
	}

	@JsonProperty("trip")
	public Trip getTrip() {
		return trip;
	}

	public void setTrip(Trip trip) {
		this.trip = trip;
	}
	
	@JsonProperty("proof")
	public InspectorLocationProof getProof() {
		return proof;
	}
	public void setProof(InspectorLocationProof proof) {
		this.proof = proof;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Inspection inspection = (Inspection) o;
		return Objects.equals(this.checkpoint, inspection.checkpoint)
				&& Objects.equals(this.inspectionId, inspection.inspectionId)
				&& Objects.equals(this.nonce, inspection.nonce);
	}

	@Override
	public int hashCode() {
		return Objects.hash(checkpoint, inspectionId, nonce);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class Inspection {\n");

		sb.append("    checkpoint: ").append(toIndentedString(checkpoint)).append("\n");
		sb.append("    inspectionId: ").append(toIndentedString(inspectionId)).append("\n");
		sb.append("    nonce: ").append(toIndentedString(nonce)).append("\n");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Convert the given object to string with each line indented by 4 spaces
	 * (except the first line).
	 */
	private String toIndentedString(java.lang.Object o) {
		if (o == null) {
			return "null";
		}
		return o.toString().replace("\n", "\n    ");
	}

	@JsonProperty("inspectP")
	public String getInspectP() {
		return inspectP;
	}
	public void setInspectP(String inspectP) {
		this.inspectP = inspectP;
	}

	@JsonProperty("transportP")
	public String getTransportP() {
		return transportP;
	}
	public void setTransportP(String transportP) {
		this.transportP = transportP;
	}
}
