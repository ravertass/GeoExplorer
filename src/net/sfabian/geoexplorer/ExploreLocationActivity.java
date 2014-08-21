package net.sfabian.geoexplorer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.Geofence;

/**
 * This activity displays the photo for a photolocation.
 * If the user believes they have find this photolocation, the user can press
 * a button to see if that is true.
 *  
 * @author sfabian
 */

public class ExploreLocationActivity extends Activity {
	
	// Report location
	private int todo;
	
	/**
	 * This is an enum for the user's proximity to a photolocation.
	 * It contains method to attach and detach this enum to an intent.
	 * @author sfabian
	 */
	public enum ProximityToLocation {
		NOT_CLOSE, CLOSE, THERE;
		private static final String name = ProximityToLocation.class.getName();
		
		/**
		 * This method is used to attach an instance of this enum to the given intent.
		 * @param intent
		 */
		public void attachTo(Intent intent) {
			intent.putExtra(name, ordinal());
		}
		/**
		 * This method is used to detach an instance of this enum from the given intent.
		 * @param intent
		 * @return the instance of this enum that is bundled with the intent
		 */
		public static ProximityToLocation detachFrom(Intent intent) {
			return values()[intent.getIntExtra(name, -1)];
		}
	}
	
	// The ID of the photolocation to explore
	private int photoLocationId;
	private PhotoLocation photoLocation;
	private ProximityToLocation proximity = ProximityToLocation.NOT_CLOSE;
	
	// TODO Texten på mittenknappen ska vara "OK" om man har hittat platsen. 
	// Den ska då bara leda tillbaka till griden, och inte till foundLocation.
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_explore_location);
		
		// Get layout entities.
		TextView locationNameView = (TextView) findViewById(R.id.explore_location_location_name);
		ImageView photoView = (ImageView) findViewById(R.id.explore_location_photo);
		TextView alreadyFoundTextView = (TextView) findViewById(R.id.explore_location_already_found_text);
		Button hereButton = (Button) findViewById(R.id.explore_location_here_button);
		
		// Get which photolocation was pressed in the ExploreGridActivity.
		Intent intent = getIntent();
		photoLocationId = intent.getIntExtra(getString(R.string.intent_key_photo_location), -1);
		// Get the photolocation from the local database.
		DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
		photoLocation = databaseHelper.getPhotoLocation(photoLocationId);
		// Get how close we are to the location.
		proximity = ProximityToLocation.detachFrom(intent);
		
		locationNameView.setText(getString(R.string.explore_location_location) + " " + photoLocation.getLocationName());
		
		// If the photolocation has already been found
		if (photoLocation.getFound()) {
			// Tell the user that it's already found
			alreadyFoundTextView.setText(R.string.explore_location_already_found);
			// Remove the button that lets the user "find" the location
			hereButton.setVisibility(hereButton.INVISIBLE);
			hereButton.setEnabled(false);
		}
		
		// If the location was added by this user
		if (photoLocation.getAddedByUser()) {
			// Tell the user that it is so
			alreadyFoundTextView.setText(R.string.explore_location_added_by_user);
			// Remove the button that lets the user "find" the location
			hereButton.setVisibility(hereButton.INVISIBLE);
			hereButton.setEnabled(false);
		}
		
		// Display the photolocatoin
		photoView.setImageBitmap(photoLocation.getPhoto());
		
		// This registers this activity as a receiver for the broadcasts from the
		// geofence system.
		LocalBroadcastManager.getInstance(this).registerReceiver(
						messageReceiver, new IntentFilter(ExploreGridActivity.GEOFENCE_BROADCAST));
	}
	
	/**
	 * This receiver holds the callback that will be called from the geofence system
	 * when a geofence transition is made.
	 */
	private BroadcastReceiver messageReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// Get the geofence ID
			String geofenceId = intent.getStringExtra(ExploreGridActivity.GEOFENCE_ID);
			String[] idParts = geofenceId.split("_");
			// The prefix of the geofenceId will tell us if it was a closeGeofence or thereGeofence
			String geofenceType = idParts[0] +"_";
			int photoLocationId = Integer.parseInt(idParts[1]);
			
			// Get the transition type of the geofence
			int transitionType = intent.getIntExtra(ExploreGridActivity.GEOFENCE_TRANSITION_TYPE, -1);
			
			// If the device entered the geofence corresponding to this location, we will note that
			if (photoLocationId == photoLocation.getId()) {
				if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
					// If the device enters and is at the photolocation
					if (geofenceType == PhotoLocation.GEOFENCE_THERE) {
						proximity = ProximityToLocation.THERE;
						// If the device enters the proximity of the photolocation
					} else if (geofenceType == PhotoLocation.GEOFENCE_CLOSE 
							&& proximity != ProximityToLocation.THERE) {
						proximity = ProximityToLocation.CLOSE;
					}
				} else if (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT) {
					// If the device moves away from the photolocation
					if (geofenceType == PhotoLocation.GEOFENCE_THERE) {
						proximity = ProximityToLocation.CLOSE;
						// If the device moves even further away from the photolocation
					} else if (geofenceType == PhotoLocation.GEOFENCE_CLOSE 
							&& proximity != ProximityToLocation.THERE) {
						proximity = ProximityToLocation.NOT_CLOSE;
					}
				}
			}
		}
	};
	
	/**
	 * Called when the "I think I'm here" button is clicked
	 */
	public void gotoLocationFound(View view) {
		// Create an intent to start the LocationFoundActivity
		Intent intent = new Intent(this, LocationFoundActivity.class);
		// Attach the user's proximity to the location
		proximity.attachTo(intent);
		// Attach the photolocation ID
		intent.putExtra(getString(R.string.intent_key_photo_location), photoLocation.getId());
		startActivity(intent);
	}
	
	/**
	 * Called when the report button is clicked
	 */
	public void gotoReportLocation(View view) {
		Intent intent = new Intent(this, ReportLocationActivity.class);
		// TODO: Här ska vi skicka med en databasnyckel till photolocationen i fråga.
		intent.putExtra(getString(R.string.intent_key_photo_location), "lol");
		startActivity(intent);
	}
}
