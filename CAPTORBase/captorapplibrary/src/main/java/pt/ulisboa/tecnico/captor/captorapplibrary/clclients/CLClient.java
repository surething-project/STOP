package pt.ulisboa.tecnico.captor.captorapplibrary.clclients;

import retrofit2.Retrofit;

public class CLClient {

    protected static final String CL_HOST = "stop.vps.tecnico.ulisboa.pt";
    protected static final String CL_PORT = "8443";
    protected static final String CL_LOCAL_HOST = "10.0.2.2";
    protected static final String CL_LOCAL_HOST_PORT = "8080";
    protected static final String CL_QSCD_DEPLOYED = "qscd-heroku.herokuapp.com/";
    protected static final String CL_NOTARY_DEPLOYED = "notary-heroku.herokuapp.com/";
    protected static final String QSCD_PORT = "8445";
    protected static final String NOTARY_PORT = "8446";
    protected static final String endpoint = "http://" + CL_HOST + ':' + CL_LOCAL_HOST_PORT + '/';
    //protected static final String endpoint = "http://" + CL_LOCAL_HOST + ':' + CL_LOCAL_HOST_PORT + "/";
    //protected static final String endpointQSCD = "https://" + CL_LOCAL_HOST + ":" + QSCD_PORT;
    //protected static final String endpointNotary = "https://" + CL_LOCAL_HOST + ":" + NOTARY_PORT;
    protected static final String endpointQSCD = "https://" + CL_QSCD_DEPLOYED;
    protected static final String endpointNotary = "https://" + CL_NOTARY_DEPLOYED;
    protected Retrofit retrofit;

}
