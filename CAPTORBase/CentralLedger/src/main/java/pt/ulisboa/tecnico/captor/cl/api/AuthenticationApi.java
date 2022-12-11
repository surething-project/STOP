package pt.ulisboa.tecnico.captor.cl.api;

import jakarta.servlet.ServletConfig;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.auth.InspectLogin;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.auth.InspectRegistration;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.auth.TransportLogin;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.auth.TransportRegistration;
import pt.ulisboa.tecnico.captor.cl.factories.AuthenticationApiServiceFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@Path("/auth")
public class AuthenticationApi {
	private final AuthenticationApiService delegate;

	public AuthenticationApi(@Context ServletConfig servletContext) {
		AuthenticationApiService delegate = null;
		
		if (servletContext != null) {
			String implClass = servletContext.getInitParameter("AuthenticationApi.implementation");
			if (implClass != null && !"".equals(implClass.trim())) {
				try {
					delegate = (AuthenticationApiService) Class.forName(implClass).newInstance();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
		
		if (delegate == null) {
			delegate = AuthenticationApiServiceFactory.getAuthenticationApi();
		}
		this.delegate = delegate;
	}
	
	@POST
	@Path("/transport/register")
	
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Operation(summary = "Add a new transport user", description = "This endpoint is used to register a transport user. It receives the transport user details and returns the ID of the transport user.", security = {
			@SecurityRequirement(name = "ledger_auth", scopes = {
			""        })    }, tags={ "authentication" })
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Transport user was created successfully. The associated transport user ID is returned", content = @Content(schema = @Schema(implementation = Long.class))),

			@ApiResponse(responseCode = "405", description = "Invalid input") })
	public Response registerTransport(@Parameter(description = "Transport registration object to be added", required = true) TransportRegistration body
			, @Context SecurityContext securityContext)
					throws NotFoundException {
		return delegate.registerTransport(body, securityContext);
	}
	
	@POST
	@Path("/inspect/register")
	
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Add a new inspect user", description = "This endpoint is used to register a inspect user. It receives the inspect user details and returns the ID of the inspect user.", security = {
			@SecurityRequirement(name = "ledger_auth", scopes = {
			""        })    }, tags={ "authentication" })
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Inspect user was created successfully. The associated inspect user ID is returned", content = @Content(schema = @Schema(implementation = Long.class))),

			@ApiResponse(responseCode = "405", description = "Invalid input") })
	public Response registerInspect(@Parameter(description = "Inspect registration object to be added", required = true) InspectRegistration body
			,@Context SecurityContext securityContext) 
					throws NotFoundException {
		return delegate.registerInspect(body, securityContext);
	}
	
	@POST
	@Path("/inspect/login")
	
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Operation(summary = "Login a inspect user", description = "This endpoint is used to login a inspect user. It receives the inspect user details and returns a session ID.", security = {
			@SecurityRequirement(name = "ledger_auth", scopes = {
			""        })    }, tags={ "authentication" })
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Inspect user was logged in successfully. The associated inspect session ID is returned", content = @Content(schema = @Schema(implementation = String.class))),

			@ApiResponse(responseCode = "405", description = "Invalid input") })
	public Response loginInspect(@Parameter(description = "Inspect login object to be validated", required = true) InspectLogin body
			,@Context SecurityContext securityContext) 
					throws NotFoundException {
		return delegate.loginInspect(body, securityContext);
	}
	
	@POST
	@Path("/transport/login")
	
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Operation(summary = "Login a transport user", description = "This endpoint is used to login a transport user. It receives the transport user details and returns a session ID.", security = {
			@SecurityRequirement(name = "ledger_auth", scopes = {
			""        })    }, tags={ "authentication" })
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Transport user was logged in successfully. The associated transport session ID is returned", content = @Content(schema = @Schema(implementation = String.class))),

			@ApiResponse(responseCode = "405", description = "Invalid input") })
	public Response loginTransport(@Parameter(description = "Transport login object to be validated", required = true) TransportLogin body
			,@Context SecurityContext securityContext) 
					throws NotFoundException {
		return delegate.loginTransport(body, securityContext);
	}
}
