package net.sfabian.geoexplorer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.Geofence;

public class ExploreLocationActivity extends Activity {
	
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
		
		// TODO: Plocka detta ur lokal databas, om stället är funnet
		locationNameView.setText(getString(R.string.explore_location_location) + " " + "Name of location");
		
		Intent intent = getIntent();
		// Get the photolocation from the database
		photoLocationId = intent.getIntExtra(getString(R.string.intent_key_photo_location), -1);
		DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
		photoLocation = databaseHelper.getPhotoLocation(photoLocationId);
		// Get if we are at the location
		// TODO: med intenten ska man egentligen få en enum
		boolean atLocation = intent.getBooleanExtra(getString(R.string.intent_key_proximity), false);
		if (atLocation) {
			proximity = ProximityToLocation.THERE;
		}
		
		// TODO: Plocka ur en lokal databas om man har funnit stället. I så fall, visa denna text.
		alreadyFoundTextView.setText(R.string.explore_location_already_found);
		
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
			int photoLocationId = Integer.parseInt(intent.getStringExtra(ExploreGridActivity.GEOFENCE_ID));
			int transitionType = intent.getIntExtra(ExploreGridActivity.GEOFENCE_TRANSITION_TYPE, -1);
			
			// If the device entered the geofence corresponding to this location, we will note that
			if (photoLocationId == photoLocation.getId()) {
				if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
					proximity = ProximityToLocation.THERE;
				} else if (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT) {
					proximity = ProximityToLocation.NOT_CLOSE;
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
