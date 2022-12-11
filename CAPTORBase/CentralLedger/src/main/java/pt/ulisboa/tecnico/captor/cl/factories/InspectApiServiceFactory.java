package pt.ulisboa.tecnico.captor.cl.factories;

import pt.ulisboa.tecnico.captor.cl.api.InspectApiService;
import pt.ulisboa.tecnico.captor.cl.impl.InspectApiServiceImpl;

public class InspectApiServiceFactory {
    private final static InspectApiService service = new InspectApiServiceImpl();

    public static InspectApiService getInspectApi() {
        return service;
    }
}
