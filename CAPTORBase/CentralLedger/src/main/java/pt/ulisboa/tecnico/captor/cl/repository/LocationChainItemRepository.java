package pt.ulisboa.tecnico.captor.cl.repository;

import pt.ulisboa.tecnico.captor.captorsharedlibrary.trip.LocationChainItem;

public interface LocationChainItemRepository {
	
	LocationChainItem addLocationChainItem(LocationChainItem l);
	
	LocationChainItem getLocationChainItemById(Long id);
	
	void deleteLocationChainItem(LocationChainItem l);

}
