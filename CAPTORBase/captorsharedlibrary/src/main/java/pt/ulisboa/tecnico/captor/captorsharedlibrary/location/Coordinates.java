package pt.ulisboa.tecnico.captor.captorsharedlibrary.location;

import java.io.Serializable;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Embeddable;
import javax.validation.constraints.*;
@Embeddable
public class Coordinates implements Serializable  {
	  @JsonProperty("latitude")
	  private double latitude = 0;

	  @JsonProperty("longitude")
	  private double longitude = 0;
	  
	  public Coordinates() {};
	  
	  public Coordinates(double latitude, double longitude) {
		  setLatitude(latitude);
		  setLongitude(longitude);
	  }

	  public Coordinates latitude(double latitude) {
	    this.latitude = latitude;
	    return this;
	  }

	  /**
	   * Get latitude
	   * @return latitude
	   **/
	  @JsonProperty("latitude")
	  @NotNull
	  public double getLatitude() {
	    return latitude;
	  }

	  public void setLatitude(double latitude) {
	    this.latitude = latitude;
	  }

	  public Coordinates longitude(double longitude) {
	    this.longitude = longitude;
	    return this;
	  }

	  /**
	   * Get longitude
	   * @return longitude
	   **/
	  @JsonProperty("longitude")
	  @NotNull
	  public double getLongitude() {
	    return longitude;
	  }

	  public void setLongitude(double longitude) {
	    this.longitude = longitude;
	  }


	  @Override
	  public boolean equals(java.lang.Object o) {
	    if (this == o) {
	      return true;
	    }
	    if (o == null || getClass() != o.getClass()) {
	      return false;
	    }
	    Coordinates coordinates = (Coordinates) o;
	    return Objects.equals(this.latitude, coordinates.latitude) &&
	        Objects.equals(this.longitude, coordinates.longitude);
	  }

	  @Override
	  public int hashCode() {
	    return Objects.hash(latitude, longitude);
	  }


	  @Override
	  public String toString() {
	    StringBuilder sb = new StringBuilder();
	    sb.append("class Coordinates {\n");
	    
	    sb.append("    latitude: ").append(toIndentedString(latitude)).append("\n");
	    sb.append("    longitude: ").append(toIndentedString(longitude)).append("\n");
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

