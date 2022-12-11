package pt.ulisboa.tecnico.captor.captorsharedlibrary.trip;

import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name="location_chains")
public class LocationChain {
	
	@Id
	@GeneratedValue
	@JsonIgnore
	private Long id;
	
	@ElementCollection
	@MapKeyColumn(name="item_no")
	@JsonProperty("items")
	private Map<Integer, LocationChainItem> items;
	
	public LocationChain() {}
	
	public void addItem(LocationChainItem item) {
		if (items == null) {
			items = new HashMap<Integer,LocationChainItem>();
		}
		int number;
		if (item.isProof()) {
			number = item.getLocationProof().getNumber();
		} else {
			number = item.getLocation().getNumber();
		}
		items.put(number, item);
	}
	
	public LocationChainItem getItem(int number) {
		return items.get(number);
	}

	public int getNextLPNumber() {
		if (items == null) {
			return 1;
		}
		return items.size() + 1;
	}

	public LocationChainItem getLastItem() {
		if (items == null) {
			return null;
		}
		return items.get(items.size());
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

}
