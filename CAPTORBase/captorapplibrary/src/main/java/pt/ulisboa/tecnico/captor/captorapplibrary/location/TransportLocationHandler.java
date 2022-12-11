package pt.ulisboa.tecnico.captor.captorapplibrary.location;

import android.app.Activity;
import android.content.Intent;

import pt.ulisboa.tecnico.captor.captorapplibrary.holders.TransportDataHolder;

/**
 * TransportLocationHandler handles the collection of location points of the Transport app
 */
public class TransportLocationHandler extends LocationHandler {

    /**
     * The string used to broadcast intent when location point is collected
     */
    public static final String LOCATION_ADDED = "Location added";

    /**
     * @param activity the UI activity
     */
    public TransportLocationHandler(Activity activity) {
        super(activity);
    }

    /**
     * This method is called when a location point is collected. The location point for the Transport app is treated here.
     */
    @Override
    protected void saveLocation() {
        TransportDataHolder.getInstance().setLastLocation(currentLocation);
        informUI();
    }

    /**
     * Broadcast Intent to UI
     */
    private void informUI() {
        Intent intent = new Intent(LOCATION_ADDED);
        activity.sendBroadcast(intent);
    }
}
