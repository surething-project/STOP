package pt.ulisboa.tecnico.captor.cl.impl;

import pt.ulisboa.tecnico.captor.captorsharedlibrary.inspection.Inspection;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.trip.LocationChainItem;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.trip.Trip;
import pt.ulisboa.tecnico.captor.cl.clLogic.CLTransportLogic;
import pt.ulisboa.tecnico.captor.cl.api.ApiResponseMessage;
import pt.ulisboa.tecnico.captor.cl.api.NotFoundException;
import pt.ulisboa.tecnico.captor.cl.api.TripApiService;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

public class TripApiServiceImpl extends TripApiService {
    @Override
    public Response addTrip(Trip body, SecurityContext securityContext) throws NotFoundException {
    	
    	// if json was deserialized correctly, then input is correct (for now)
    	Long result = CLTransportLogic.createTrip(body); // trip Id
    	if (result == null) {
    		Response.status(404).build(); // invalid input
    	}
    	return Response.ok().entity(result).build();
    }
    @Override
    public Response checkTripInspection(Long tripId, SecurityContext securityContext) throws NotFoundException {
        // do some magic!
        //return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    	Inspection inspection = CLTransportLogic.checkInspection(tripId);
    	if (inspection == null) {
    		return Response.status(404).build();
    	}
    	return Response.ok().entity(inspection).build();
    }
    @Override
    public Response endTrip(LocationChainItem body, Long tripId, SecurityContext securityContext) throws NotFoundException {
    	// if json was deserialized correctly, then input is correct (for now)
        
    	if(CLTransportLogic.endTrip(tripId, body)) {
    		return Response.ok().build();
    	}
    	
    	return Response.status(404).build(); //trip not found
    }
    @Override
    public Response getTripById(Long tripId, SecurityContext securityContext) throws NotFoundException {
    	Trip trip = CLTransportLogic.getTrip(tripId);
    	if (trip == null) {
    		return Response.status(404).build(); //trip not found
    	}
    	return Response.ok().entity(trip).build();
    }
    @Override
    public Response getTripLocation(Long tripId, SecurityContext securityContext) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response initializeTrip(LocationChainItem body, Long tripId, SecurityContext securityContext) throws NotFoundException {
    	// if json was deserialized correctly, then input is correct (for now)
    	
    	if (CLTransportLogic.startTrip(tripId, body)) {
    		return Response.ok().build();
    	}
    	return Response.status(404).build(); //trip not found
    }
    @Override
    public Response locateTrip(LocationChainItem body, Long tripId, SecurityContext securityContext) throws NotFoundException {
    	// if json was deserialized correctly, then input is correct (for now)
        
    	if (CLTransportLogic.addLocation(tripId, body)) {
    		return Response.ok().build();
    	}
    	
    	return Response.status(404).build(); //trip not found
    }
    @Override
    public Response updateTripById(Trip body, Long tripId, SecurityContext securityContext) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
	@Override
	public Response addProof(LocationChainItem body, Long tripId, SecurityContext securityContext) throws NotFoundException {
		// if json was deserialized correctly, then input is correct (for now)
		
		if (CLTransportLogic.addProof(tripId, body)) {
    		return Response.ok().build();
    	}
    	
    	return Response.status(404).build(); //trip not found
	}
	@Override
	public Response forceEndTrip(Long tripId, SecurityContext securityContext) throws NotFoundException {
		// To use in evaluation in case something goes wrong
		
		if (CLTransportLogic.forceEnd(tripId)) {
    		return Response.ok().build();
    	}
		
		return Response.status(404).build(); //trip not found
	}
}
