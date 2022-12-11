package pt.ulisboa.tecnico.captor.captorapplibrary.clclients;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.protobuf.ByteString;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import eu.surething_project.core.Signature;
import eu.surething_project.signature.util.SignatureManager;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import pt.ulisboa.tecnico.qscd.contract.LocationProofsProto.CitizenCard;
import pt.ulisboa.tecnico.qscd.contract.LocationProofsProto.CitizenCardSigned;
import pt.ulisboa.tecnico.qscd.contract.OrganizationProto.Organization;
import pt.ulisboa.tecnico.qscd.contract.ProverProto.Prover;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.protobuf.ProtoConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public class QscdAPIClient extends CLClient {
    private static final String TAG = "QSCDAPIClient";

    public static final String COMMUNICATION_SUCCESS = "Successful QSCD communication";
    public static final String COMMUNICATION_FAILURE = "Error while trying to communicate with QSCD";
    public static final String SESSION_CREATED = "Created session";
    public static final String SESSION_CREATE_FAILED = "Failed to create session";
    public static final String SESSION_JOINED = "Joined session";
    public static final String SESSION_JOIN_FAILED = "Failed to join session";
    public static final String SESSION_FINISHED = "Finished session";
    public static final String SESSION_FINISH_FAILED = "Failed to finish session";
    public static final String ERROR = "QSCD communication error: ";

    private static QscdAPIClient clientInstance = null;
    private String token = "";
    private String sessionId = "";
    private String proverId = "";
    private String ccId = "";
    private String citizenCardSignature = "";
    private String publicKey = "";
    private QscdAPIClient.QscdAPI qscdAPI;

    public QscdAPIClient() {
        retrofit = new Retrofit.Builder()
                .baseUrl(endpointQSCD)
                .addConverterFactory(ProtoConverterFactory.create())
                .client(generateSecureOkHttpClient())
                .build();
        qscdAPI = retrofit.create(QscdAPIClient.QscdAPI.class);
    }

    public void setToken(String token) { this.token = token; }

    public String getToken() { return this.token; }

    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getSessionId() { return this.sessionId; }

    public void setProverId(String proverId) { this.proverId = proverId; }

    public String getProverId() { return this.proverId; }

    public void setCcId(String ccId) { this.ccId = ccId; }

    public void setCitizenCardSignature(String citizenCardSignature) { this.citizenCardSignature = citizenCardSignature; }

    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }

    public interface QscdAPI {
        @Headers({"Content-Type: application/protobuf"})
        @POST("/session/create")
        Call<ResponseBody> createSession(@Body Organization organization);

        @Headers({"Content-Type: application/protobuf"})
        @PUT("/session/{id}/join")
        Call<ResponseBody> joinSession(@Path(value = "id", encoded = true) String id, @Body Prover prover);

        @PUT("/session/finish")
        Call<ResponseBody> finishSession(@Header("Authorization") String token);

        @Headers({"Content-Type: application/protobuf"})
        @POST("/citizenCard/register/signed")
        Call<ResponseBody> registerSignedCitizenCard(@Header("Authorization") String token, @Body CitizenCardSigned citizenCardSigned);

        @GET("/")
        Call<ResponseBody> testCommunication();
    }

    public static QscdAPIClient getInstance() {
        if (clientInstance == null) {
            clientInstance = new QscdAPIClient();
        }
        return clientInstance;
    }

    public boolean test(final Activity activity) {
        Call<ResponseBody> call = qscdAPI.testCommunication();
        Log.d(TAG, call.request().toString());
        call.enqueue(new Callback<ResponseBody>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                handleOnResponse(activity, response, COMMUNICATION_SUCCESS, COMMUNICATION_FAILURE);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleOnFailure(activity, t, COMMUNICATION_FAILURE);
            }
        });
        return true;
    }


    /**
     * API call to create session
     * @param activity Used to broadcast when response is returned
     * @return
     */
    public void createSession(final Activity activity) {
        Organization organization = Organization.newBuilder()
                .setName("inspector")
                .build();

        Call<ResponseBody> call = qscdAPI.createSession(organization);
        Log.d(TAG, call.request().toString());
        call.enqueue(new Callback<ResponseBody>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                handleOnResponse(activity, response, SESSION_CREATED, SESSION_CREATE_FAILED);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleOnFailure(activity, t, SESSION_CREATE_FAILED);
            }
        });
    }


    /**
     * API call to join session
     * @param activity Used to broadcast when response is returned
     * @return
     */
    public boolean joinSessionAndRegisterCitizenCardSigned(final Activity activity, final PrivateKey privateKey) {
        Prover prover = Prover.newBuilder()
                .setId(proverId)
                .build();

        Call<ResponseBody> call = qscdAPI.joinSession(sessionId, prover);
        Log.d(TAG, call.request().toString());
        call.enqueue(new Callback<ResponseBody>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                handleOnResponse(activity, response, SESSION_JOINED, SESSION_JOIN_FAILED);
                registerCitizenCardSigned(activity, privateKey);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleOnFailure(activity, t, SESSION_JOIN_FAILED);
            }
        });
        return true;
    }

    /**
     * API call to finish session
     * @param activity Used to broadcast when response is returned
     * @return
     */
    public boolean finishSession(final Activity activity) {
        Call<ResponseBody> call = qscdAPI.finishSession("Bearer " + token);
        Log.d(TAG, call.request().toString());
        call.enqueue(new Callback<ResponseBody>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                handleOnResponse(activity, response, SESSION_FINISHED, SESSION_FINISH_FAILED);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleOnFailure(activity, t, SESSION_FINISH_FAILED);
            }
        });
        return true;
    }

    /**
     * API call to finish session
     * @param activity Used to broadcast when response is returned
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public boolean registerCitizenCardSigned(final Activity activity, PrivateKey privateKey) {
        CitizenCard citizenCard = CitizenCard.newBuilder()
                .setId(ccId)
                .setSignature(createSignature(ccId, privateKey))
                .build();

        CitizenCardSigned citizenCardSigned = CitizenCardSigned.newBuilder()
                .setCitizenCard(citizenCard)
                .setSignature(createSignature(citizenCardSignature, privateKey))
                .build();

        Log.d(TAG, "Token: " + token);
        Call<ResponseBody> call = qscdAPI.registerSignedCitizenCard("Bearer " + token, citizenCardSigned);
        Log.d(TAG, call.request().toString());
        call.enqueue(new Callback<ResponseBody>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                handleOnResponse(activity, response, SESSION_FINISHED, SESSION_FINISH_FAILED);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleOnFailure(activity, t, SESSION_FINISH_FAILED);
            }
        });
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void handleOnResponse(Activity activity, Response<ResponseBody> response, String success, String fail) {
        if (response.code() == 200) {
            try {
                JsonObject jsonObject = new Gson().fromJson(response.body().string(), JsonObject.class);

                if (success.equals(SESSION_JOINED)) {
                    token = jsonObject.get("token").toString().replace("\"", "");
                    String data = jsonObject.get("data").toString().replace("\"", "");

                    JsonObject dataObject = new Gson().fromJson(data, JsonObject.class);
                    sessionId = dataObject.get("sessionId").toString().replace("\"", "");
                    proverId = dataObject.get("proverId").toString().replace("\"", "");
                    Log.d(TAG, "Token: " + token);
                    Log.d(TAG, "Session Id: " + sessionId);
                    Log.d(TAG, "Prover Id: " + proverId);
                }

            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
            }

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

    private void handleOnFailure(Activity activity, Throwable t, String fail) {
        /**
         * Broadcast error to UI
         */
        Intent intent = new Intent(fail);
        activity.sendBroadcast(intent);
        Log.e(TAG, ERROR + t.getMessage());
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private Signature createSignature(String message, PrivateKey privateKey) {
        try {
            Random random = new Random();
            byte[] signature = SignatureManager.sign(message.getBytes(StandardCharsets.UTF_8), privateKey);
            return Signature.newBuilder()
                    .setValue(ByteString.copyFrom(signature))
                    .setCryptoAlgo("SHA256withRSA")
                    .setNonce(random.nextLong())
                    .build();

        } catch (Exception e) {
            Log.e(TAG, "register citizen card signed error");
            return Signature.newBuilder().getDefaultInstanceForType();
        }
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
