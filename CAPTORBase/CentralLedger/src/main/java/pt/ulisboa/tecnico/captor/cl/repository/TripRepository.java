package pt.ulisboa.tecnico.captor.cl.repository;

import pt.ulisboa.tecnico.captor.captorsharedlibrary.trip.Trip;

public interface TripRepository {
	
	Trip addTrip(Trip t);
	
	Trip getTripById(Long id);
	
	void deleteTrip(Trip t);

}
