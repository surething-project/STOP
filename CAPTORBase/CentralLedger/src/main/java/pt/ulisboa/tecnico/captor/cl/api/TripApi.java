package pt.ulisboa.tecnico.captor.cl.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.servlet.ServletConfig;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.trip.LocationChainItem;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.trip.Trip;
import pt.ulisboa.tecnico.captor.cl.factories.TripApiServiceFactory;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@Path("/trip")
public class TripApi  {
   private final TripApiService delegate;

   public TripApi(@Context ServletConfig servletContext) {
      TripApiService delegate = null;

      if (servletContext != null) {
         String implClass = servletContext.getInitParameter("TripApi.implementation");
         if (implClass != null && !"".equals(implClass.trim())) {
            try {
               delegate = (TripApiService) Class.forName(implClass).newInstance();
            } catch (Exception e) {
               throw new RuntimeException(e);
            }
         } 
      }

      if (delegate == null) {
         delegate = TripApiServiceFactory.getTripApi();
      }

      this.delegate = delegate;
   }

    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @Operation(summary = "Add a new trip", description = "This endpoint is used to report a scheduled transportation of a set of specific goods. It received the information about the transportation.", security = {
        @SecurityRequirement(name = "ledger_auth", scopes = {
            ""        })    }, tags={ "trip" })
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "Trip was created successfully. The associated trip ID is returned", content = @Content(schema = @Schema(implementation = Integer.class))),
        
        @ApiResponse(responseCode = "405", description = "Invalid input") })
    public Response addTrip(@Parameter(description = "Trip object to be added" ,required=true) Trip body

, @Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.addTrip(body,securityContext);
    }
    @GET
    @Path("/{tripId}/inspect")
    
    @Produces({ "application/json" })
    @Operation(summary = "Check if trip was selected for inspection", description = "This endpoint is used to check if the specific trip was selected for inspection. It received the ID of the trip and returns a boolean indicating if the trip was selected.", security = {
        @SecurityRequirement(name = "ledger_auth", scopes = {
            ""        })    }, tags={ "trip" })
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "Trip inspection status", content = @Content(schema = @Schema(implementation = Object.class))),
        
        @ApiResponse(responseCode = "400", description = "Invalid ID supplied") })
    public Response checkTripInspection(@Parameter(description = "ID of trip to begin",required=true) @PathParam("tripId") Long tripId
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.checkTripInspection(tripId,securityContext);
    }
    @POST
    @Path("/{tripId}/end")
    @Consumes({ "application/json" })
    
    @Operation(summary = "End trip", description = "This endpoint is used to report the end of the transportation. It receives the ID of the specific trip and the coordinates of the final location of the trip.", security = {
        @SecurityRequirement(name = "ledger_auth", scopes = {
            ""        })    }, tags={ "trip" })
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "Trip was ended successfully"),
        
        @ApiResponse(responseCode = "400", description = "Invalid ID supplied"),
        
        @ApiResponse(responseCode = "404", description = "Trip not found"),
        
        @ApiResponse(responseCode = "405", description = "Validation exception") })
    public Response endTrip(@Parameter(description = "Initial location point" ,required=true) LocationChainItem body

,@Parameter(description = "ID of trip to begin",required=true) @PathParam("tripId") Long tripId
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.endTrip(body,tripId,securityContext);
    }
    @GET
    @Path("/{tripId}")
    
    @Produces({ "application/json" })
    @Operation(summary = "Find trip by ID", description = "This endpoint is used to return information about a submitted scheduled transportation of a set of specific goods. It receives the ID of the specific trip and returns the trip information.", security = {
        @SecurityRequirement(name = "ledger_auth", scopes = {
            ""        })    }, tags={ "trip" })
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "Trip information is returned", content = @Content(schema = @Schema(implementation = Trip.class))),
        
        @ApiResponse(responseCode = "400", description = "Invalid ID supplied"),
        
        @ApiResponse(responseCode = "404", description = "Trip not found") })
    public Response getTripById(@Parameter(description = "ID of trip to return",required=true) @PathParam("tripId") Long tripId
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.getTripById(tripId,securityContext);
    }
    @GET
    @Path("/{tripId}/locate")
    
    @Produces({ "application/json" })
    @Operation(summary = "Get current trip location", description = "This endpoint is used to retrieve the current location of the transportation. It receives the ID of the specific trip and it returns the current coordinates of the trip.", security = {
        @SecurityRequirement(name = "ledger_auth", scopes = {
            ""        })    }, tags={ "trip" })
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "Trip location is returned", content = @Content(schema = @Schema(implementation = LocationChainItem.class))),
        
        @ApiResponse(responseCode = "404", description = "Trip not found"),
        
        @ApiResponse(responseCode = "400'", description = "Invalid ID supplied") })
    public Response getTripLocation(@Parameter(description = "ID of trip to return",required=true) @PathParam("tripId") Long tripId
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.getTripLocation(tripId,securityContext);
    }
    @POST
    @Path("/{tripId}/begin")
    @Consumes({ "application/json" })
    
    @Operation(summary = "Initialize trip", description = "This endpoint is used to indicate the initialization of a scheduled transportion of a specific set of goods. It receives the ID of the specific trip and the initial coordinates of the trip.", security = {
        @SecurityRequirement(name = "ledger_auth", scopes = {
            ""        })    }, tags={ "trip" })
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "Trip was initiated successfully"),
        
        @ApiResponse(responseCode = "400", description = "Invalid ID supplied"),
        
        @ApiResponse(responseCode = "404", description = "Trip not found"),
        
        @ApiResponse(responseCode = "405", description = "Validation exception") })
    public Response initializeTrip(@Parameter(description = "Initial coordinates" ,required=true) LocationChainItem body

,@Parameter(description = "ID of trip to begin",required=true) @PathParam("tripId") Long tripId
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.initializeTrip(body,tripId,securityContext);
    }
    @POST
    @Path("/{tripId}/locate")
    @Consumes({ "application/json" })
    
    @Operation(summary = "Update trip location", description = "This endpoint is used to report the current location of the transportation. It receives the ID of the specific trip and the current coordinates of the trip.", security = {
        @SecurityRequirement(name = "ledger_auth", scopes = {
            ""        })    }, tags={ "trip" })
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "Trip location was updated successfully"),
        
        @ApiResponse(responseCode = "400", description = "Invalid ID supplied"),
        
        @ApiResponse(responseCode = "404", description = "Trip not found"),
        
        @ApiResponse(responseCode = "405", description = "Validation exception") })
    public Response locateTrip(@Parameter(description = "Initial coordinates" ,required=true) LocationChainItem body

,@Parameter(description = "ID of trip to begin",required=true) @PathParam("tripId") Long tripId
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.locateTrip(body,tripId,securityContext);
    }
    @PUT
    @Path("/{tripId}")
    @Consumes({ "application/json" })
    
    @Operation(summary = "Update an existing trip", description = "This endpoint is used to update information about a submitted scheduled transportation of a set of specific goods. It receives the ID of the specific trip and the updated trip information.", security = {
        @SecurityRequirement(name = "ledger_auth", scopes = {
            ""        })    }, tags={ "trip" })
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "Trip was updated successfully"),
        
        @ApiResponse(responseCode = "400", description = "Invalid ID supplied"),
        
        @ApiResponse(responseCode = "404", description = "Trip not found"),
        
        @ApiResponse(responseCode = "405", description = "Validation exception") })
    public Response updateTripById(@Parameter(description = "Trip object to be updated" ,required=true) Trip body

,@Parameter(description = "ID of trip to update",required=true) @PathParam("tripId") Long tripId
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.updateTripById(body,tripId,securityContext);
    }
    
    //add proof
    @POST
    @Path("/{tripId}/proof")
    @Consumes({ "application/json" })
    
    @Operation(summary = "add trip trip", description = "This endpoint is used to report the current location of the transportation. It receives the ID of the specific trip and the current coordinates of the trip.", security = {
        @SecurityRequirement(name = "ledger_auth", scopes = {
            ""        })    }, tags={ "trip" })
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "Trip location was updated successfully"),
        
        @ApiResponse(responseCode = "400", description = "Invalid ID supplied"),
        
        @ApiResponse(responseCode = "404", description = "Trip not found"),
        
        @ApiResponse(responseCode = "405", description = "Validation exception") })
    public Response addProof(@Parameter(description = "Initial coordinates" ,required=true) LocationChainItem body

	,@Parameter(description = "ID of trip to begin",required=true) @PathParam("tripId") Long tripId
	,@Context SecurityContext securityContext)
	    throws NotFoundException {
        return delegate.addProof(body,tripId,securityContext);
    }
    
    /**
     * To use in evaluation in case something goes wrong
     */
    @POST
    @Path("/{tripId}/force")
    @Consumes({ "application/json" })
    public Response forceEndTrip( @PathParam("tripId") Long tripId,@Context SecurityContext securityContext)
	    throws NotFoundException {
        return delegate.forceEndTrip(tripId,securityContext);
    }
    
}
