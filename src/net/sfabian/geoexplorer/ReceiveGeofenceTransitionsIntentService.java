package net.sfabian.geoexplorer;

import java.util.List;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

public class ReceiveGeofenceTransitionsIntentService extends IntentService {
	
	private static final int NOTIFICATION_ID = 9001;

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
					if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER
							&& (geofence.getRequestId().split("_")[0]+"_")
									.equals(PhotoLocation.GEOFENCE_CLOSE)) {
						postNotification();
					}
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
	
	private void postNotification() {
		NotificationCompat.Builder builder = 
				new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.notification)
				.setContentTitle("Close to location!")
				.setContentText("You seem to be close to one or multiple locations...");
		// We create an intent to go to the photo grid activity
		Intent resultIntent = new Intent(this, ExploreGridActivity.class);
		
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(ExploreGridActivity.class);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
				PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(resultPendingIntent);
		NotificationManager notificationManager = 
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(NOTIFICATION_ID, builder.build());
	}
}
