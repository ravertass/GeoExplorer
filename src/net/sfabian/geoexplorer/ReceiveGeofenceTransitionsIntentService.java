package net.sfabian.geoexplorer;

import java.util.List;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class ReceiveGeofenceTransitionsIntentService extends IntentService {

	public ReceiveGeofenceTransitionsIntentService() {
		super("ReceiveGeofenceTransitionsIntentService");
	}
	
	/**
	 * This method will be called when the device makes a geofence transition
	 * (enters or exits a geofence).
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		
		if (LocationClient.hasError(intent)) {
			int errorCode = LocationClient.getErrorCode(intent);
			Log.e(this.getClass().toString(), "Location Services error: " + Integer.toString(errorCode));
		} else {
			int transitionType = LocationClient.getGeofenceTransition(intent);
			if ((transitionType == Geofence.GEOFENCE_TRANSITION_ENTER)
					|| (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT)) {
				List<Geofence> triggerList = LocationClient.getTriggeringGeofences(intent);
				
				for (Geofence geofence : triggerList) {
					sendBroadcast(geofence.getRequestId(), transitionType);
				}	
			} else {
				Log.e(this.getClass().toString(), "Geofence transition error: "
						+ Integer.toString(transitionType));
			}
		}
	}

	/**
	 * This method broadcasts to listening activities the ID and transition type
	 * of a geofence. The ID should be the same as the database IDs for photolocations.
	 * @param id
	 */
	private void sendBroadcast(String id, int transitionType) {
		Intent intent = new Intent(ExploreGridActivity.GEOFENCE_BROADCAST);
		intent.putExtra(ExploreGridActivity.GEOFENCE_ID, id);
		intent.putExtra(ExploreGridActivity.GEOFENCE_TRANSITION_TYPE, transitionType);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}
}
