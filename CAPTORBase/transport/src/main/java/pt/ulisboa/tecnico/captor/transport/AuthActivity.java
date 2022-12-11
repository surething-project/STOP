package pt.ulisboa.tecnico.captor.transport;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import pt.ulisboa.tecnico.captor.captorapplibrary.clclients.AuthAPIClient;
import pt.ulisboa.tecnico.captor.captorapplibrary.holders.TransportDataHolder;
import pt.ulisboa.tecnico.captor.captorapplibrary.ui.UIHandler;
import pt.ulisboa.tecnico.captor.captorapplibrary.user.TransportUser;

public class AuthActivity extends AppCompatActivity {
    private static final String TAG = "AuthActivity";

    public static final int REQUEST_AUTH = 35;
    private static final String TRANSPORT_NAME = "TRANSPORT";

    private Button login;
    private EditText fiscal;
    private EditText pass;
    private TextView loading;
    private TextView error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * Prepare window
         */
        setContentView(R.layout.activity_auth);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        /**
         * Prepare variables
         */
        login = findViewById(R.id.login);
        fiscal = findViewById(R.id.fiscal_number);
        pass = findViewById(R.id.password);
        loading = findViewById(R.id.loading);
        error = findViewById(R.id.error);
        /**
         * Prepare login button
         */
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login.setVisibility(View.INVISIBLE);
                UIHandler.hideKeyboard(AuthActivity.this);
                error.setVisibility(View.INVISIBLE);
                loading.setVisibility(View.VISIBLE);
                try {
                    fiscal.setVisibility(View.INVISIBLE);
                    pass.setVisibility(View.INVISIBLE);
                    int fiscalNumber = Integer.valueOf(fiscal.getText().toString());
                    String password = pass.getText().toString();
                    TransportUser user = new TransportUser();
                    user.setUsername(TRANSPORT_NAME);
                    TransportDataHolder.getInstance().setUser(user);
                    user.registerUser(AuthActivity.this);    // broadcasts AuthAPIClient.USER_CREATED when successful
                } catch (Exception e) { // not int
                    invalidCredentials();
                }
            }
        });

        /**
         * Prepare filter for when user is registered
         */
        IntentFilter filter = new IntentFilter(AuthAPIClient.USER_CREATED);
        this.registerReceiver(receiver, filter);
        filter = new IntentFilter(AuthAPIClient.USER_NOT_CREATED);
        this.registerReceiver(receiver, filter);
    }

    private void invalidCredentials() {
        loading.setVisibility(View.INVISIBLE);
        pass.getText().clear();
        error.setVisibility(View.VISIBLE);
        login.setVisibility(View.VISIBLE);
        fiscal.setVisibility(View.VISIBLE);
        pass.setVisibility(View.VISIBLE);
    }

    private void validCredentials() {
        loading.setVisibility(View.INVISIBLE);
        UIHandler.showToast(AuthActivity.this, getApplicationContext(), "Logged in");
        /**
         * End activity
         */
        /*Intent data = new Intent();
        data.putExtra("something","something");
        setResult(RESULT_OK,data);*/
        setResult(RESULT_OK);
        finish();
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (AuthAPIClient.USER_CREATED.equals(action)) {
                validCredentials();
                unregisterReceiver(this);
            } else if (AuthAPIClient.USER_NOT_CREATED.equals(action)) {
                invalidCredentials();
            }
        }
    };
}
