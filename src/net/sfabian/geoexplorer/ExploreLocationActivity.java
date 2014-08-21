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

public class ExploreLocationActivity extends Activity {
	
	// kommentera
	private int todo;
	
	public enum ProximityToLocation {
		NOT_CLOSE, CLOSE, THERE;
		
		private static final String name = ProximityToLocation.class.getName();
		public void attachTo(Intent intent) {
			intent.putExtra(name, ordinal());
		}
		public static ProximityToLocation detachFrom(Intent intent) {
			return values()[intent.getIntExtra(name, -1)];
		}
	}
	
	private int photoLocationId;
	private PhotoLocation photoLocation;
	private ProximityToLocation proximity = ProximityToLocation.NOT_CLOSE;
	
	// TODO Texten på mittenknappen ska vara "OK" om man har hittat platsen. 
	// Den ska då bara leda tillbaka till griden, och inte till foundLocation.
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_explore_location);
		
		TextView locationNameView = (TextView) findViewById(R.id.explore_location_location_name);
		ImageView photoView = (ImageView) findViewById(R.id.explore_location_photo);
		TextView alreadyFoundTextView = (TextView) findViewById(R.id.explore_location_already_found_text);
		Button hereButton = (Button) findViewById(R.id.explore_location_here_button);
		
		Intent intent = getIntent();
		// Get the photolocation from the database
		photoLocationId = intent.getIntExtra(getString(R.string.intent_key_photo_location), -1);
		DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
		photoLocation = databaseHelper.getPhotoLocation(photoLocationId);
		// Get how close we are to the location
		proximity = ProximityToLocation.detachFrom(intent);
		
		locationNameView.setText(getString(R.string.explore_location_location) + " " + photoLocation.getLocationName());
		
		if (photoLocation.getFound()) {
			alreadyFoundTextView.setText(R.string.explore_location_already_found);
			// TODO såhär kanske man inte ska göra
			hereButton.setVisibility(hereButton.INVISIBLE);
			hereButton.setEnabled(false);
		}
		
		if (photoLocation.getAddedByUser()) {
			alreadyFoundTextView.setText(R.string.explore_location_added_by_user);
			// TODO såhär kanske man inte ska göra
			hereButton.setVisibility(hereButton.INVISIBLE);
			hereButton.setEnabled(false);
		}
		
		photoView.setImageBitmap(photoLocation.getPhoto());
		
		// This registers this activity as a receiver for the broadcasts from the
		// geofence system
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
			
			int transitionType = intent.getIntExtra(ExploreGridActivity.GEOFENCE_TRANSITION_TYPE, -1);
			
			// If the device entered the geofence corresponding to this location, we will note that
			if (photoLocationId == photoLocation.getId()) {
				if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
					if (geofenceType == PhotoLocation.GEOFENCE_THERE) {
						proximity = ProximityToLocation.THERE;
					} else if (geofenceType == PhotoLocation.GEOFENCE_CLOSE 
							&& proximity != ProximityToLocation.THERE) {
						proximity = ProximityToLocation.CLOSE;
					}
				} else if (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT) {
					if (geofenceType == PhotoLocation.GEOFENCE_THERE) {
						proximity = ProximityToLocation.CLOSE;
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
		Intent intent = new Intent(this, LocationFoundActivity.class);
		proximity.attachTo(intent);
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
