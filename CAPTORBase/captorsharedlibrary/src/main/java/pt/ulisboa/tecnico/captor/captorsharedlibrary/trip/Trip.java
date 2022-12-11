package pt.ulisboa.tecnico.captor.captorsharedlibrary.trip;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import pt.ulisboa.tecnico.captor.captorsharedlibrary.auth.User;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.location.Coordinates;

@Entity
@Table(name="trips")
public class Trip {
	
	public enum StatusEnum {
	    SCHEDULED("scheduled"),
	    
	    ON_ROUTE("on-route"),
	    
	    ARRIVED("arrived");
	
	    private String value;
	
	    StatusEnum(String value) {
	      this.value = value;
	    }
	
	    @Override
	    @JsonValue
	    public String toString() {
	      return String.valueOf(value);
	    }
	
	    @JsonCreator
	    public static StatusEnum fromValue(String text) {
	      for (StatusEnum b : StatusEnum.values()) {
	        if (String.valueOf(b.value).equals(text)) {
	          return b;
	        }
	      }
	      return null;
	    }
	  }
	@Enumerated(EnumType.STRING)
	@JsonProperty("status")
	private StatusEnum status = null;
	
    @Id
    @GeneratedValue
	@JsonProperty("id")
    private Long id;

    @Embedded
    @AttributeOverrides(value = {
            @AttributeOverride(name = "latitude", column = @Column(name = "initial_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "initial_longitude"))
        })
    @JsonProperty("initialCoordinates")
    @NotNull
    private Coordinates initialCoordinates =  new Coordinates(1.1, 1.1);
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="locationchain_id")
    @JsonProperty("locationChain")
    private LocationChain locationChain;
    
    @ManyToOne
	@JoinColumn(name="TRANSPORT_ID")
    @JsonProperty("transport")
    @NotNull
    private User transport;

    public Trip() {};

    public Trip(Long id) {
        setId(id);
    }

    public Trip(Coordinates initialcoordinates) {
        //setInitialCoordinates(initialCoordinates);
    	this.initialCoordinates = initialcoordinates;
    }

    public Trip(Long id, Coordinates initialCoordinates) {
        setId(id);
        setInitialCoordinates(initialCoordinates);
    }

    @JsonProperty("id")
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    @JsonProperty("initialCoordinates")
    @NotNull
    public Coordinates getInitialCoordinates() {
        return initialCoordinates;
    }
    public void setInitialCoordinates(Coordinates initialCoordinates) {
        this.initialCoordinates = initialCoordinates;
    }

    @JsonProperty("locationChain")
	public LocationChain getLocationChain() {
		return locationChain;
	}
	public void setLocationChain(LocationChain locationChain) {
		this.locationChain = locationChain;
	}
	
	@JsonProperty("transport")
	@NotNull
	public User getTransport() {
		return transport;
	}
	public void setTransport(User transport) {
		this.transport = transport;
	}
	
	public void addLocationChainItem(LocationChainItem item) {
		if (locationChain == null) {
			locationChain = new LocationChain();
		}
		locationChain.addItem(item);
	}
	
	public LocationChainItem getLocationChainItem(int number) {
		if (locationChain == null) {
			return null;
		}
		return locationChain.getItem(number);
	}

	public int getNextLPNumber() {
		if (locationChain == null) {
			return 1;
		}
		return locationChain.getNextLPNumber();
	}

	public String getLastHash() {
		if (locationChain == null) {
			return null;
		}
		LocationChainItem item = locationChain.getLastItem();
		if (item == null) {
			return null;
		}
		return item.getTransportSignature();
	}
	
	public Coordinates getLastLocation() {
		if (locationChain == null) {
			return null;
		}
		LocationChainItem item = locationChain.getLastItem();
		if (item == null) {
			return null;
		}
		if (item.isProof()) {		// not pretty...
			return item.getLocationProof().getLocation();
		}
		return item.getLocation().getLocation();
	}
	
	@JsonIgnore
	public LocationChainItem getLastItem() {
		if (locationChain == null) {
			return null;
		}
		return locationChain.getLastItem();
	}

	public StatusEnum getStatus() {
		return status;
	}

	public void setStatus(StatusEnum status) {
		this.status = status;
	}
}
