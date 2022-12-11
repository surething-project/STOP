package io.swagger.api.factories;

import io.swagger.api.InspectApiService;
import io.swagger.api.impl.InspectApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2019-04-24T14:07:08.201Z[GMT]")public class InspectApiServiceFactory {
    private final static InspectApiService service = new InspectApiServiceImpl();

    public static InspectApiService getInspectApi() {
        return service;
    }
}
