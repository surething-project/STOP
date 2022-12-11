package pt.ulisboa.tecnico.captor.cl.factories;

import pt.ulisboa.tecnico.captor.cl.api.AuthenticationApiService;
import pt.ulisboa.tecnico.captor.cl.impl.AuthenticationApiServiceImpl;

public class AuthenticationApiServiceFactory {
	private final static AuthenticationApiService service = new AuthenticationApiServiceImpl();
	
	public static AuthenticationApiService getAuthenticationApi() {
		return service;
	}
}
