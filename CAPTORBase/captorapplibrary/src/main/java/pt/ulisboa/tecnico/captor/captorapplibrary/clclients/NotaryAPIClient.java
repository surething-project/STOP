package pt.ulisboa.tecnico.captor.captorapplibrary.clclients;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.protobuf.ProtoConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public class NotaryAPIClient extends CLClient {
    private static final String TAG = "NotaryAPIClient";

    public static final String COMMUNICATION_SUCCESS = "Successful Notary communication";
    public static final String COMMUNICATION_FAILURE = "Error while trying to communicate with Notary";
    public static final String CREATE_PROOF = "Proof created";
    public static final String CREATE_PROOF_FAILED = "Failed to create proof";
    public static final String ERROR = "Notary communication error: ";

    private static NotaryAPIClient clientInstance = null;
    private String token = "";
    private NotaryAPIClient.NotaryAPI notaryAPI;

    public NotaryAPIClient() {
        retrofit = new Retrofit.Builder()
                .baseUrl(endpointNotary)
                .addConverterFactory(ProtoConverterFactory.create())
                .client(generateSecureOkHttpClient())
                .build();
        notaryAPI = retrofit.create(NotaryAPIClient.NotaryAPI.class);
    }

    public interface NotaryAPI {
        @POST("/proof/create")
        Call<ResponseBody> createProof(@Header("Authorization") String token);

        @Headers({"Content-Type: application/protobuf", "Accept: application/protobuf"})
        @GET("/")
        Call<ResponseBody> testCommunication();
    }

    public static NotaryAPIClient getInstance() {
        if (clientInstance == null) {
            clientInstance = new NotaryAPIClient();
        }
        return clientInstance;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean test(final Activity activity) {
        Call<ResponseBody> call = notaryAPI.testCommunication();
        Log.d(TAG, call.request().toString());
        call.enqueue(new Callback<ResponseBody>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                handleOnResponse(activity, response, COMMUNICATION_SUCCESS, COMMUNICATION_FAILURE);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleOnFailure(activity, COMMUNICATION_FAILURE);
            }
        });
        return true;
    }

    /**
     * API call to create proof
     * @param activity Used to broadcast when response is returned
     * @return
     */
    public boolean createProof(final Activity activity) {
        Call<ResponseBody> call = notaryAPI.createProof("Bearer " + token);
        Log.d(TAG, call.request().toString());
        call.enqueue(new Callback<ResponseBody>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                handleOnResponse(activity, response, CREATE_PROOF, CREATE_PROOF_FAILED);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleOnFailure(activity, CREATE_PROOF_FAILED);
            }
        });
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void handleOnResponse(Activity activity, Response<ResponseBody> response, String success, String fail) {
        if (response.code() == 200) {
                Intent intent = new Intent(success);
                activity.sendBroadcast(intent); // broadcast intent

        } else {
            /**
             * Broadcast error to UI
             */
            Intent intent = new Intent(fail);
            activity.sendBroadcast(intent);
            Log.e(TAG, ERROR + response.code());
        }
    }

    private void handleOnFailure(Activity activity, String fail) {
        /**
         * Broadcast error to UI
         */
        Intent intent = new Intent(fail);
        activity.sendBroadcast(intent);
        Log.e(TAG, ERROR + "failure");
    }

    private OkHttpClient generateSecureOkHttpClient() {
        try {
            final TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            return new OkHttpClient.Builder()
                    .readTimeout(60, TimeUnit.SECONDS)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .sslSocketFactory(sslContext.getSocketFactory())
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    })
                    .build();

        } catch (Exception ex) {
            Log.e(TAG, ERROR + "ssl client");
            return new OkHttpClient();
        }
    }
}