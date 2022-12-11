package io.swagger.api.factories;

import io.swagger.api.TripApiService;
import io.swagger.api.impl.TripApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2019-04-24T14:07:08.201Z[GMT]")public class TripApiServiceFactory {
    private final static TripApiService service = new TripApiServiceImpl();

    public static TripApiService getTripApi() {
        return service;
    }
}
