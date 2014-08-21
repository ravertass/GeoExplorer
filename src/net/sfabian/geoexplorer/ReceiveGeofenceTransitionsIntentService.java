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

/**
 * This service checks for geofence transitions. It broadcasts info about
 * those to listening activities (ExploreGrid and ExploreLocation).
 * It also creates notifications when the user is close to non-found photolocations.
 * 
 * @author sfabian
 */

public class ReceiveGeofenceTransitionsIntentService extends IntentService {

	// This ID is used for the notification
	private static final int NOTIFICATION_ID = 9001;

	public ReceiveGeofenceTransitionsIntentService() {
		super("ReceiveGeofenceTransitionsIntentService");
	}
	
	/**
	 * This method is called when the device makes a geofence transition
	 * (enters or exits a geofence). It broadcasts this info to listening activities
	 * and posts notifications.
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		// If there is an error 
		if (LocationClient.hasError(intent)) {
			int errorCode = LocationClient.getErrorCode(intent);
			Log.e(this.getClass().toString(), "Location Services error: " + Integer.toString(errorCode));
			// If there is no error
		} else {
			// Get the transition type
			int transitionType = LocationClient.getGeofenceTransition(intent);
			// If the user has entered or exited any geofences
			if ((transitionType == Geofence.GEOFENCE_TRANSITION_ENTER)
					|| (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT)) {
				// Get all the triggering geofences
				List<Geofence> triggerList = LocationClient.getTriggeringGeofences(intent);
				
				// Get the databasehelper
				DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
				
				// For all triggering geofences
				for (Geofence geofence : triggerList) {
					String[] splitId = geofence.getRequestId().split("_");
					// Get the proximity (close or there)
					String proximity = splitId[0] + "_";
					// Get the photolocation ID
					int id = Integer.parseInt(splitId[1]);
					// If user entered a CLOSE geofence (is close to a photolocation)
					// and the photolocation has not been found by the user before
					// and the photolocation was not added by this user
					if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER
							&& proximity.equals(PhotoLocation.GEOFENCE_CLOSE) &&
									!databaseHelper.isPhotoLocationAdded(id) &&
									!databaseHelper.isPhotoLocationFound(id)) {
						// Post a notification about it
						postNotification();
					}
					// Broadcast the geofence info to listening activities
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
	 * of a geofence. This ID contains the type of geofence (CLOSE or THERE) and
	 * the database ID for the photolocation.
	 * @param id
	 */
	private void sendBroadcast(String id, int transitionType) {
		Intent intent = new Intent(ExploreGridActivity.GEOFENCE_BROADCAST);
		intent.putExtra(ExploreGridActivity.GEOFENCE_ID, id);
		intent.putExtra(ExploreGridActivity.GEOFENCE_TRANSITION_TYPE, transitionType);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}
	
	/**
	 * This method posts a notification about being close to a location.
	 */
	private void postNotification() {
		NotificationCompat.Builder builder = 
				new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.ic_notification)
				.setContentTitle("Close to a location!")
				.setContentText("You seem to be close to one or multiple locations...");
		
		// This intent means that the user till go to the ExploreGridActivity if the
		// notification is pressed.
		Intent resultIntent = new Intent(this, ExploreGridActivity.class);
		
		// This handles the back button the correct way.
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
