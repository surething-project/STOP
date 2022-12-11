package io.swagger.api;

import io.swagger.model.*;
import io.swagger.api.InspectApiService;
import io.swagger.api.factories.InspectApiServiceFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import io.swagger.model.Checkpoint;
import io.swagger.model.Inspection;
import io.swagger.model.LocationProofInspector;

import java.util.Map;
import java.util.List;
import io.swagger.api.NotFoundException;

import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.servlet.ServletConfig;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.*;
import javax.validation.constraints.*;


@Path("/inspect")


@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2019-04-24T14:07:08.201Z[GMT]")public class InspectApi  {
   private final InspectApiService delegate;

   public InspectApi(@Context ServletConfig servletContext) {
      InspectApiService delegate = null;

      if (servletContext != null) {
         String implClass = servletContext.getInitParameter("InspectApi.implementation");
         if (implClass != null && !"".equals(implClass.trim())) {
            try {
               delegate = (InspectApiService) Class.forName(implClass).newInstance();
            } catch (Exception e) {
               throw new RuntimeException(e);
            }
         } 
      }

      if (delegate == null) {
         delegate = InspectApiServiceFactory.getInspectApi();
      }

      this.delegate = delegate;
   }

    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @Operation(summary = "Add a new checkpoint", description = "This endpoint is used to register a checkpoint. It receives the checkpoint details adn returns the ID of the checkpoint.", security = {
        @SecurityRequirement(name = "ledger_auth", scopes = {
            ""        })    }, tags={ "inspection" })
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "Checkpoint was created successfully. The associated checkpoint ID is returned", content = @Content(schema = @Schema(implementation = Long.class))),
        
        @ApiResponse(responseCode = "405", description = "Invalid input") })
    public Response addCheckpoint(@Parameter(description = "Checkpoint object to be added" ,required=true) Checkpoint body

,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.addCheckpoint(body,securityContext);
    }
    @POST
    @Path("/{checkpointId}/select")
    
    @Produces({ "application/json" })
    @Operation(summary = "Select a random vehicle for inspection", description = "This endpoint is used to select a trip for inspection. It receives the ID of the checkpoint and returns the ID of the inspection", security = {
        @SecurityRequirement(name = "ledger_auth", scopes = {
            ""        })    }, tags={ "inspection" })
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "Selection was successful. The inspection information is returned.", content = @Content(schema = @Schema(implementation = Inspection.class))),
        
        @ApiResponse(responseCode = "400", description = "Invalid ID supplied") })
    public Response selectInspection(@Parameter(description = "ID of checkpoint",required=true) @PathParam("checkpointId") Long checkpointId
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.selectInspection(checkpointId,securityContext);
    }
    @GET
    @Path("/{checkpointId}")
    
    @Produces({ "application/json" })
    @Operation(summary = "Show information about a checkpoint", description = "This endpoint is used to return the information regarding a specific checkpoint. It receives the ID of the checkpoint and returns the information about the checkpoint.", security = {
        @SecurityRequirement(name = "ledger_auth", scopes = {
            ""        })    }, tags={ "inspection" })
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "Checkpoint information is returned", content = @Content(schema = @Schema(implementation = Checkpoint.class))),
        
        @ApiResponse(responseCode = "400", description = "Invalid ID supplied") })
    public Response showCheckpoint(@Parameter(description = "ID of checkpoint",required=true) @PathParam("checkpointId") Long checkpointId
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.showCheckpoint(checkpointId,securityContext);
    }
    @GET
    @Path("/{checkpointId}/update/{inspectionId}")
    
    @Produces({ "application/json" })
    @Operation(summary = "Show a inspection proof", description = "This endpoint is used to show a inspection proof. It receives the ID of the checkpoint and the ID of the inspection and it returns the inspection info and proof.", security = {
        @SecurityRequirement(name = "ledger_auth", scopes = {
            ""        })    }, tags={ "inspection" })
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "Inspection proof is returned", content = @Content(schema = @Schema(implementation = Object.class))),
        
        @ApiResponse(responseCode = "400", description = "Invalid ID supplied") })
    public Response showInspection(@Parameter(description = "ID of checkpoint",required=true) @PathParam("checkpointId") Long checkpointId
,@Parameter(description = "ID of inspection",required=true) @PathParam("inspectionId") Long inspectionId
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.showInspection(checkpointId,inspectionId,securityContext);
    }
    @POST
    @Path("/{checkpointId}/update/{inspectionId}")
    @Consumes({ "application/json" })
    
    @Operation(summary = "Upload a inspection proof", description = "This endpoint is used to upload a inspection proof. It receives the ID of the checkpoint, the ID of the inspection and the inspection proof", security = {
        @SecurityRequirement(name = "ledger_auth", scopes = {
            ""        })    }, tags={ "inspection" })
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "Proof was uploaded successfully."),
        
        @ApiResponse(responseCode = "400", description = "Invalid ID supplied"),
        
        @ApiResponse(responseCode = "405", description = "Invalid input") })
    public Response updateInspection(@Parameter(description = "Proof object to be added" ,required=true) LocationProofInspector body

,@Parameter(description = "ID of checkpoint",required=true) @PathParam("checkpointId") Long checkpointId
,@Parameter(description = "ID of inspection",required=true) @PathParam("inspectionId") Long inspectionId
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.updateInspection(body,checkpointId,inspectionId,securityContext);
    }
}
