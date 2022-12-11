package pt.ulisboa.tecnico.captor.cl.impl;

import pt.ulisboa.tecnico.captor.captorsharedlibrary.inspection.Checkpoint;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.inspection.Inspection;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.inspection.InspectorLocationProof;
import pt.ulisboa.tecnico.captor.cl.clLogic.CLInspectLogic;
import pt.ulisboa.tecnico.captor.cl.api.InspectApiService;
import pt.ulisboa.tecnico.captor.cl.api.NotFoundException;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

public class InspectApiServiceImpl extends InspectApiService {
	@Override
    public Response addCheckpoint(Checkpoint body, SecurityContext securityContext) throws NotFoundException {
		// if json was deserialized correctly, then input is correct (for now)
		Long result = CLInspectLogic.createCheckpoint(body);
		if (result == null) {
			System.out.println("Invalid checkpoint");
    		Response.serverError().build(); // invalid input
    	}
		return Response.ok().entity(result).build();
    }
    @Override
    public Response selectInspection(Long checkpointId, SecurityContext securityContext) throws NotFoundException {
    	Inspection inspection = CLInspectLogic.createInspection(checkpointId);
    	if (inspection == null) {
    		System.out.println("Invalid inspection");
    		return Response.status(404).build(); // checkpoint not found or not able to select vehicle
    	}
    	return Response.ok().entity(inspection).build();
    }
    @Override
    public Response showCheckpoint(Long checkpointId, SecurityContext securityContext) throws NotFoundException {
    	Checkpoint checkpoint = CLInspectLogic.getCheckpoint(checkpointId);
    	if (checkpoint == null) {
    		System.out.println("Invalid checkpoint");
    		return Response.status(404).build(); // checkpoint not found
    	}
    	return Response.ok().entity(checkpoint).build();
    	
    }
    @Override
    public Response showInspection(Long checkpointId, Long inspectionId, SecurityContext securityContext) throws NotFoundException {
        Inspection inspection = CLInspectLogic.getInspection(checkpointId, inspectionId);
    	if (inspection == null) {
    		System.out.println("Invalid inspection");
    		return Response.status(404).build(); // checkpoint or inspection not found
    	}
    	return Response.ok().entity(inspection).build();
    }
    @Override
    public Response updateInspection(InspectorLocationProof body, Long checkpointId, Long inspectionId, SecurityContext securityContext) throws NotFoundException {
    	Inspection inspection = CLInspectLogic.closeInspection(checkpointId, inspectionId, body);
    	if (inspection == null) {
    		System.out.println("Invalid inspection");
    		return Response.status(404).build(); // checkpoint not found
    	}
    	return Response.ok().entity(inspection).build();
    }
	@Override
	public Response forceEndInspection(Long checkpointId, Long inspectionId, SecurityContext securityContext)
			throws NotFoundException {
		// To use in evaluation in case something goes wrong
		if (CLInspectLogic.forceEndInspection(checkpointId, inspectionId)) {
			return Response.ok().build();
		}
		return Response.status(404).build();
	}
}
