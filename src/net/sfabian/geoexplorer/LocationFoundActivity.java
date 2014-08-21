package net.sfabian.geoexplorer;

import net.sfabian.geoexplorer.ExploreLocationActivity.ProximityToLocation;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * This activity is shown when the user thinks they have found a photolocation.
 * The activity will show if the user has, or if the user is close by, or if the user is not
 * close at all. 
 * 
 * @author fabian
 */

public class LocationFoundActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location_found);
		
		// Get layout entities.
		TextView locationNameView = (TextView) findViewById(R.id.location_found_location_name);
		ImageView smileyView = (ImageView) findViewById(R.id.location_found_smiley);
		TextView areWeThereTextView = (TextView) findViewById(R.id.location_found_are_we_there_text);
		
		// Get how close the user is to the location from the intent.
		Intent intent = getIntent();
		ProximityToLocation proximity = ProximityToLocation.detachFrom(intent);
		
		// If the location was found
		if (proximity == ProximityToLocation.THERE) {
			// Get the photolocation from the local database
			int photoLocationId = intent.getIntExtra(getString(R.string.intent_key_photo_location), -1);
			DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
			PhotoLocation photoLocation = databaseHelper.getPhotoLocation(photoLocationId);

			// Display photolocation name and a nice smiley!
			locationNameView.setText(getString(R.string.explore_location_location)
					+ " " + photoLocation.getLocationName());
			areWeThereTextView.setText(R.string.location_found_well_done);
			smileyView.setImageResource(R.drawable.smiley_found);
			// Add to the database that this location is found
			databaseHelper.addFoundPhotoLocation(photoLocationId);
			// If the location is close by
		} else if (proximity == ProximityToLocation.CLOSE) {
			// Display the appropriate text and a cool smiley.
			areWeThereTextView.setText(R.string.location_found_close);
			smileyView.setImageResource(R.drawable.smiley_close);
			// If the location is not even close
		} else if (proximity == ProximityToLocation.NOT_CLOSE) {
			// Display the appropriate text and a sad smiley.
			smileyView.setImageResource(R.drawable.smiley_not_close);
			areWeThereTextView.setText(R.string.location_found_not_close);
		}
	}
	
	/**
	 * Called when the 'OK' button is clicked
	 */
	public void gotoExploreGrid(View view) {
		onBackPressed();
	}
	
	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, ExploreGridActivity.class);
		// With this flag, pushing "back" at the ExploreGridActivity won't make you go back to
		// this activity or the ExploreLocationActivity
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}
}
