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

import pt.ulisboa.tecnico.captor.captorapplibrary.clclients.TransportAPIClient;
import pt.ulisboa.tecnico.captor.captorapplibrary.holders.TransportDataHolder;
import pt.ulisboa.tecnico.captor.captorapplibrary.location.TransportLocationHandler;
import pt.ulisboa.tecnico.captor.captorapplibrary.ui.UIHandler;

public class CreateTripActivity extends AppCompatActivity {
    private static final String TAG = "CreateTripActivity";

    public static final int REQUEST_TRIP_CREATION = 99;
    public static final String RESULT_TRIP_ID ="tripId";

    private EditText date;
    private EditText time;
    private EditText noPackages;
    private EditText receiverNo;
    private TextView info;
    private Button submitAndGo;
    private Button schedule;
    private TransportLocationHandler locationHandler;
    private boolean ready;
    private boolean startNow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * Prepare window
         */
        setContentView(R.layout.activity_create_trip);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        /**
         * Setup variables
         */
        ready = false;
        date = findViewById(R.id.date);
        time = findViewById(R.id.time);
        noPackages = findViewById(R.id.packages);
        receiverNo = findViewById(R.id.receiver);
        info = findViewById(R.id.info);
        info.setText(R.string.loading);
        submitAndGo = findViewById(R.id.submit);
        schedule = findViewById(R.id.schedule);
        /**
         * Setup create and start button
         */
        submitAndGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitAndGo.setVisibility(View.INVISIBLE);
                UIHandler.hideKeyboard(CreateTripActivity.this);
                if (noPackages.getText().toString().isEmpty() ||
                        receiverNo.getText().toString().isEmpty() ||
                        receiverNo.getText().toString().length() != 9   // TEMP
                    ) {
                    UIHandler.showToast(CreateTripActivity.this, getApplicationContext(), "Invalid input");
                    submitAndGo.setVisibility(View.VISIBLE);
                } else {
                    info.setText(R.string.loading);
                    startNow = true;    // trigger to start imediately
                    createTrip();
                }
            }
        });

        /**
         * Setup location Handler
         */
        IntentFilter filter = new IntentFilter(TransportLocationHandler.LOCATION_ADDED);     // Current Location retrieved
        this.registerReceiver(receiver, filter);
        locationHandler = new TransportLocationHandler(this);
        locationHandler.start();
    }

    /**
     * Create trip and request to start
     */
    private void createTrip() {
        startNow = true;
        IntentFilter filter = new IntentFilter(TransportAPIClient.TRIP_CREATED);     // Central Ledger creates trip
        this.registerReceiver(receiver, filter);
        TransportAPIClient.getInstance().addTrip(this);
    }

    private void tripCreated() {
        UIHandler.showToast(this, getApplicationContext(), "Trip created. Id: "
                + TransportDataHolder.getInstance().getActiveTrip().getId());
        locationHandler.stop();
        /**
         * End activity
         */
        Intent data = new Intent();
        if (startNow) {
            data.putExtra(RESULT_TRIP_ID, TransportDataHolder.getInstance().getActiveTrip().getId());
        } else {
            data.putExtra(RESULT_TRIP_ID,0);
        }
        setResult(RESULT_OK,data);
        //setResult(RESULT_OK);
        finish();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TransportLocationHandler.LOCATION_ADDED.equals(action)) {
                if (!ready) {
                    info.setText(R.string.info_default);
                    submitAndGo.setVisibility(View.VISIBLE);
                    ready = true;
                }
            } else if (TransportAPIClient.TRIP_CREATED.equals(action)) {
                tripCreated();
                unregisterReceiver(this);
            }
        }
    };
}
