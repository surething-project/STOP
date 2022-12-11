package pt.ulisboa.tecnico.captor.cl.api;

import pt.ulisboa.tecnico.captor.captorsharedlibrary.auth.InspectLogin;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.auth.InspectRegistration;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.auth.TransportLogin;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.auth.TransportRegistration;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

public abstract class AuthenticationApiService {
	public abstract Response registerTransport(TransportRegistration body, SecurityContext securityContext) throws NotFoundException;
	public abstract Response registerInspect(InspectRegistration body, SecurityContext securityContext) throws NotFoundException;
	public abstract Response loginInspect(InspectLogin body, SecurityContext securityContext) throws NotFoundException;
	public abstract Response loginTransport(TransportLogin body, SecurityContext securityContext) throws NotFoundException;
}
