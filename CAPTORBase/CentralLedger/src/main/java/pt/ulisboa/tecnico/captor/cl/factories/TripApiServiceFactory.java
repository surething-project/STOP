package pt.ulisboa.tecnico.captor.cl.factories;

import pt.ulisboa.tecnico.captor.cl.api.TripApiService;
import pt.ulisboa.tecnico.captor.cl.impl.TripApiServiceImpl;

public class TripApiServiceFactory {
    private final static TripApiService service = new TripApiServiceImpl();

    public static TripApiService getTripApi() {
        return service;
    }
}
