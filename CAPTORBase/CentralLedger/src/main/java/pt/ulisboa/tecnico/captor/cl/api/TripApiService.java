package pt.ulisboa.tecnico.captor.cl.api;

import pt.ulisboa.tecnico.captor.captorsharedlibrary.trip.LocationChainItem;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.trip.Trip;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

public abstract class TripApiService {
    public abstract Response addTrip(Trip body, SecurityContext securityContext) throws NotFoundException;
    public abstract Response checkTripInspection(Long tripId, SecurityContext securityContext) throws NotFoundException;
    public abstract Response endTrip(LocationChainItem body,Long tripId,SecurityContext securityContext) throws NotFoundException;
    public abstract Response getTripById(Long tripId,SecurityContext securityContext) throws NotFoundException;
    public abstract Response getTripLocation(Long tripId,SecurityContext securityContext) throws NotFoundException;
    public abstract Response initializeTrip(LocationChainItem body,Long tripId,SecurityContext securityContext) throws NotFoundException;
    public abstract Response locateTrip(LocationChainItem body,Long tripId,SecurityContext securityContext) throws NotFoundException;
    public abstract Response updateTripById(Trip body,Long tripId,SecurityContext securityContext) throws NotFoundException;
    public abstract Response addProof(LocationChainItem body, Long tripId, SecurityContext securityContext) throws NotFoundException;
    public abstract Response forceEndTrip(Long tripId, SecurityContext securityContext) throws NotFoundException;
}
