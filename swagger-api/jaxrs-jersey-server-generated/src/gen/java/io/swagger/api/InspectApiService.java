package io.swagger.api;

import io.swagger.api.*;
import io.swagger.model.*;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import io.swagger.model.Checkpoint;
import io.swagger.model.Inspection;
import io.swagger.model.LocationProofInspector;

import java.util.Map;
import java.util.List;
import io.swagger.api.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.validation.constraints.*;
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2019-04-24T14:07:08.201Z[GMT]")public abstract class InspectApiService {
    public abstract Response addCheckpoint(Checkpoint body,SecurityContext securityContext) throws NotFoundException;
    public abstract Response selectInspection(Long checkpointId,SecurityContext securityContext) throws NotFoundException;
    public abstract Response showCheckpoint(Long checkpointId,SecurityContext securityContext) throws NotFoundException;
    public abstract Response showInspection(Long checkpointId,Long inspectionId,SecurityContext securityContext) throws NotFoundException;
    public abstract Response updateInspection(LocationProofInspector body,Long checkpointId,Long inspectionId,SecurityContext securityContext) throws NotFoundException;
}
