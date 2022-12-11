package pt.ulisboa.tecnico.captor.captorsharedlibrary.inspection;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.*;

import pt.ulisboa.tecnico.captor.captorsharedlibrary.auth.User;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.location.Coordinates;

@Entity
@Table(name="checkpoints")
public class Checkpoint   {
	  @Embedded
	  @JsonProperty("coordinates")
	  @NotNull
	  private Coordinates coordinates;

	  @Id
	  @GeneratedValue
	  @JsonProperty("checkpointId")
	  private Long checkpointId;

	  @ManyToOne
	  @JoinColumn(name="inspect_id")
	  @JsonProperty("inspector")
	  @NotNull
	  private User inspector = null;
	  
	  @ElementCollection
	  @JsonIgnore
	  private List<Inspection> inspections;

	  public Checkpoint() {
		  inspections = new ArrayList<Inspection>();
	  } 

	  /**
	   * Get coordinates
	   * @return coordinates
	   **/
	  @JsonProperty("coordinates")
	  @NotNull
	  public Coordinates getCoordinates() {
	    return coordinates;
	  }

	  public void setCoordinates(Coordinates coordinates) {
	    this.coordinates = coordinates;
	  }

	  /**
	   * Get checkpointId
	   * @return checkpointId
	   **/
	  @JsonProperty("checkpointId")
	  public Long getCheckpointId() {
	    return checkpointId;
	  }

	  public void setCheckpointId(Long checkpointId) {
	    this.checkpointId = checkpointId;
	  }

	  /**
	   * Get inspector
	   * @return inspector
	   **/
	  @JsonProperty("inspector")
	  @NotNull
	  public User getInspector() {
	    return inspector;
	  }

	  public void setInspector(User inspector) {
	    this.inspector = inspector;
	  }
	  
	  public void setInspections(ArrayList<Inspection> inspections) {
		  this.inspections = inspections;
	  }
	  public List<Inspection> getInspections() {
		  return this.inspections;
	  }
	  
	  public Long addInspection(Inspection inspection) {
		  if (inspections == null) {
			  inspections = new ArrayList<Inspection>();
		  }
		  //inspection.setInspectionId(inspections.size() + 1);
		  inspections.add(inspection);
		  //return inspection.getInspectionId();
		  return null;
	  }
	  
	  public Inspection getInspection(Long inspectionId) {
		  if (inspections == null) {
			  return null;
		  }
		  try {
			  //return inspections.get(inspectionId - 1);
			  return null;
		  } catch (IndexOutOfBoundsException e) {
			  return null;
		  }
	  }
	  
	  /*public boolean modifyInspection(Long inspectionId, Inspection inspection) {
			try {
				//inspections.set(inspectionId - 1, inspection);
				return true;
			} catch (IndexOutOfBoundsException e) {
				return false;
			}
		}*/

	  @Override
	  public boolean equals(java.lang.Object o) {
	    if (this == o) {
	      return true;
	    }
	    if (o == null || getClass() != o.getClass()) {
	      return false;
	    }
	    Checkpoint checkpoint = (Checkpoint) o;
	    return Objects.equals(this.coordinates, checkpoint.coordinates) &&
	        Objects.equals(this.checkpointId, checkpoint.checkpointId) &&
	        Objects.equals(this.inspector, checkpoint.inspector);
	  }

	  @Override
	  public int hashCode() {
	    return Objects.hash(coordinates, checkpointId, inspector);
	  }


	  @Override
	  public String toString() {
	    StringBuilder sb = new StringBuilder();
	    sb.append("class Checkpoint {\n");
	    
	    sb.append("    coordinates: ").append(toIndentedString(coordinates)).append("\n");
	    sb.append("    checkpointId: ").append(toIndentedString(checkpointId)).append("\n");
	    sb.append("    inspectorKey: ").append(toIndentedString(inspector.toString())).append("\n");
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
	}
