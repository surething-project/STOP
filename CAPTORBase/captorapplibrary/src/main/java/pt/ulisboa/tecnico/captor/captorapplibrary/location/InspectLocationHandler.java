package pt.ulisboa.tecnico.captor.captorapplibrary.location;

import android.app.Activity;
import android.content.Intent;

import pt.ulisboa.tecnico.captor.captorapplibrary.holders.InspectDataHolder;

public class InspectLocationHandler extends LocationHandler {

    public static final String LOCATION_ADDED = "Location added";

    public InspectLocationHandler(Activity activity) {
        super(activity);
    }

    @Override
    protected void saveLocation() {
        InspectDataHolder.getInstance().setLastLocation(currentLocation);
        Intent intent = new Intent(LOCATION_ADDED);
        activity.sendBroadcast(intent);
    }
}
