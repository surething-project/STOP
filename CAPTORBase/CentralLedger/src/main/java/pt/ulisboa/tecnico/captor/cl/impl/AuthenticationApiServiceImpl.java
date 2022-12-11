package pt.ulisboa.tecnico.captor.cl.impl;

import pt.ulisboa.tecnico.captor.captorsharedlibrary.auth.InspectLogin;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.auth.InspectRegistration;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.auth.TransportLogin;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.auth.TransportRegistration;
import pt.ulisboa.tecnico.captor.cl.clLogic.CLInspectLogic;
import pt.ulisboa.tecnico.captor.cl.clLogic.CLTransportLogic;
import pt.ulisboa.tecnico.captor.cl.api.AuthenticationApiService;
import pt.ulisboa.tecnico.captor.cl.api.NotFoundException;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

public class AuthenticationApiServiceImpl extends AuthenticationApiService {

	@Override
	public Response registerTransport(TransportRegistration body, SecurityContext securityContext)
			throws NotFoundException {
		if (body.getPublicKey() == null || body.getPublicKey().equals("")) {
			return Response.status(405).build(); // Invalid input
		}
		return Response.ok().entity(CLTransportLogic.createTransportUser(body.getPublicKey())).build();
	}

	@Override
	public Response registerInspect(InspectRegistration body, SecurityContext securityContext)
			throws NotFoundException {
		if (body.getPublicKey().equals("")) {
			return Response.status(405).build(); // Invalid input
		}
		return Response.ok().entity(CLInspectLogic.createInspectUser(body.getPublicKey())).build();
	}

	@Override
	public Response loginInspect(InspectLogin body, SecurityContext securityContext) throws NotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response loginTransport(TransportLogin body, SecurityContext securityContext) throws NotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

}
