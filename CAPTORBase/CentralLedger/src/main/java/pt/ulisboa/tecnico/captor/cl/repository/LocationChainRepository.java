package pt.ulisboa.tecnico.captor.cl.repository;

import pt.ulisboa.tecnico.captor.captorsharedlibrary.trip.LocationChain;

public interface LocationChainRepository {
	
	LocationChain addLocationChain(LocationChain l);
	
	LocationChain getLocationChainById(Long id);
	
	void deleteLocationChain(LocationChain l);

}
