package pt.ulisboa.tecnico.captor.cl.api;

import pt.ulisboa.tecnico.captor.captorsharedlibrary.inspection.Checkpoint;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.inspection.InspectorLocationProof;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

public abstract class InspectApiService {
	public abstract Response addCheckpoint(Checkpoint body, SecurityContext securityContext) throws NotFoundException;
    public abstract Response selectInspection(Long checkpointId, SecurityContext securityContext) throws NotFoundException;
    public abstract Response showCheckpoint(Long checkpointId, SecurityContext securityContext) throws NotFoundException;
    public abstract Response showInspection(Long checkpointId, Long inspectionId, SecurityContext securityContext) throws NotFoundException;
    public abstract Response updateInspection(InspectorLocationProof body, Long checkpointId, Long inspectionId,SecurityContext securityContext) throws NotFoundException;
    public abstract Response forceEndInspection(Long checkpointId, Long inspectionId, SecurityContext securityContext) throws NotFoundException;
}
