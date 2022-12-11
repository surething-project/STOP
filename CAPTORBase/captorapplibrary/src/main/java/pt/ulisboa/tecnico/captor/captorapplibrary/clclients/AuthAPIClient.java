package pt.ulisboa.tecnico.captor.captorapplibrary.clclients;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;

import okhttp3.ResponseBody;
import pt.ulisboa.tecnico.captor.captorapplibrary.holders.InspectDataHolder;
import pt.ulisboa.tecnico.captor.captorapplibrary.holders.TransportDataHolder;
import pt.ulisboa.tecnico.captor.captorapplibrary.user.InspectUser;
import pt.ulisboa.tecnico.captor.captorapplibrary.user.TransportUser;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.auth.InspectRegistration;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.auth.TransportRegistration;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public class AuthAPIClient extends CLClient {
    private static final String TAG = "AuthAPIClient";

    public static final String USER_CREATED = "User created";
    public static final String USER_NOT_CREATED = "User not created";

    private static AuthAPIClient clientInstance = null;
    private AuthAPI authAPI;

    public AuthAPIClient() {
        retrofit = new Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        authAPI = retrofit.create(AuthAPI.class);
    }

    public interface AuthAPI {
        @Headers({"Content-Type: application/json", "Accept: application/json"})
        @POST("/CentralLedger/v1/auth/transport/register")
        Call<ResponseBody> registerTransport(@Body TransportRegistration transportRegistration);

        @Headers({"Content-Type: application/json", "Accept: application/json"})
        @POST("/CentralLedger/v1/auth/inspect/register")
        Call<ResponseBody> registerInspect(@Body InspectRegistration inspectRegistration);

        // login missing - is it needed?
    }

    public static AuthAPIClient getInstance() {
        if (clientInstance == null) {
            clientInstance = new AuthAPIClient();
        }
        return clientInstance;
    }

    /**
     * API call to register transport client, sets username
     * @param activity Used to broadcast when response is returned
     * @return
     */
    public boolean registerTransport(final Activity activity) {
        TransportRegistration registration = new TransportRegistration();
        registration.setPublicKey(TransportDataHolder.getInstance().getUser().getPublicKey());

        Call<ResponseBody> call = authAPI.registerTransport(registration);
        Log.d(TAG, call.request().toString());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 200) {
                    TransportUser user = TransportDataHolder.getInstance().getUser();
                    try {
                        //user.setUsername(response.body().string());
                        user.setId(Long.valueOf(response.body().string()));
                        TransportDataHolder.getInstance().setUser(user);

                        Intent intent = new Intent(USER_CREATED);
                        activity.sendBroadcast(intent); // broadcast user created intent
                    } catch (IOException e) {
                        /**
                         * Broadcast error to UI
                         */
                        Intent intent = new Intent(USER_NOT_CREATED);
                        activity.sendBroadcast(intent);
                        Log.e(TAG, "Register transport error: " + e.getLocalizedMessage());
                    }
                } else {
                    /**
                     * Broadcast error to UI
                     */
                    Intent intent = new Intent(USER_NOT_CREATED);
                    activity.sendBroadcast(intent);
                    Log.e(TAG, "Register transport error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                /**
                 * Broadcast error to UI
                 */
                Intent intent = new Intent(USER_NOT_CREATED);
                activity.sendBroadcast(intent);
                Log.e(TAG, "Register transport error: failure");
            }
        });
        return true;
    }

    /**
     * API call to register inspect client, sets username
     * @param activity Used to broadcast when response is returned
     * @return
     */
    public boolean registerInspect(final Activity activity) {
        InspectRegistration registration = new InspectRegistration();
        registration.setPublicKey(InspectDataHolder.getInstance().getUser().getPublicKey());

        Call<ResponseBody> call = authAPI.registerInspect(registration);
        Log.d(TAG, call.request().toString());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 200) {
                    InspectUser user = InspectDataHolder.getInstance().getUser();
                    try {
                        //user.setUsername(response.body().string());
                        user.setId(Long.valueOf(response.body().string()));
                        InspectDataHolder.getInstance().setUser(user);

                        Intent intent = new Intent(USER_CREATED);
                        activity.sendBroadcast(intent); // broadcast intent
                    } catch (IOException e) {
                        /**
                         * Broadcast error to UI
                         */
                        Intent intent = new Intent(USER_NOT_CREATED);
                        activity.sendBroadcast(intent);
                        Log.e(TAG, "Register inspect error: " + e.getLocalizedMessage());
                    }
                } else {
                    /**
                     * Broadcast error to UI
                     */
                    Intent intent = new Intent(USER_NOT_CREATED);
                    activity.sendBroadcast(intent);
                    Log.e(TAG, "Register inspect error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                /**
                 * Broadcast error to UI
                 */
                Intent intent = new Intent(USER_NOT_CREATED);
                activity.sendBroadcast(intent);
                Log.e(TAG, "Register inspect error: " + t.getLocalizedMessage());
            }
        });
        return true;
    }
}
