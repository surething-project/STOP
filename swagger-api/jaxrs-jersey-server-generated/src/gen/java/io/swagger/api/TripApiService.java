package io.swagger.api;

import io.swagger.api.*;
import io.swagger.model.*;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import io.swagger.model.LP;
import io.swagger.model.Trip;

import java.util.Map;
import java.util.List;
import io.swagger.api.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.validation.constraints.*;
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2019-04-24T14:07:08.201Z[GMT]")public abstract class TripApiService {
    public abstract Response addTrip(Trip body,SecurityContext securityContext) throws NotFoundException;
    public abstract Response checkTripInspection(Long tripId,SecurityContext securityContext) throws NotFoundException;
    public abstract Response endTrip(LP body,Long tripId,SecurityContext securityContext) throws NotFoundException;
    public abstract Response getTripById(Long tripId,SecurityContext securityContext) throws NotFoundException;
    public abstract Response getTripLocation(Long tripId,SecurityContext securityContext) throws NotFoundException;
    public abstract Response initializeTrip(LP body,Long tripId,SecurityContext securityContext) throws NotFoundException;
    public abstract Response locateTrip(LP body,Long tripId,SecurityContext securityContext) throws NotFoundException;
    public abstract Response updateTripById(Trip body,Long tripId,SecurityContext securityContext) throws NotFoundException;
}
