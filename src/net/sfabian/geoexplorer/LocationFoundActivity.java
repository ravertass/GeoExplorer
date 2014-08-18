package net.sfabian.geoexplorer;

import net.sfabian.geoexplorer.ExploreLocationActivity.ProximityToLocation;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class LocationFoundActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location_found);
		
		TextView locationNameView = (TextView) findViewById(R.id.location_found_location_name);
		ImageView smileyView = (ImageView) findViewById(R.id.location_found_smiley);
		TextView areWeThereTextView = (TextView) findViewById(R.id.location_found_are_we_there_text);
		
		Intent intent = getIntent();
		ProximityToLocation proximity = ProximityToLocation.detachFrom(intent);
		
		// If the location was found
		if (proximity == ProximityToLocation.THERE) {
			// Get the photolocation from the database
			int photoLocationId = intent.getIntExtra(getString(R.string.intent_key_photo_location), -1);
			DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
			PhotoLocation photoLocation = databaseHelper.getPhotoLocation(photoLocationId);

			locationNameView.setText(getString(R.string.explore_location_location)
					+ " " + photoLocation.getLocationName());
			areWeThereTextView.setText(R.string.location_found_well_done);
			smileyView.setImageResource(R.drawable.smiley_found);
			// If the location is close by
		} else if (proximity == ProximityToLocation.CLOSE) {
			areWeThereTextView.setText(R.string.location_found_close);
			smileyView.setImageResource(R.drawable.smiley_close);
			// If the location is not even close
		} else if (proximity == ProximityToLocation.NOT_CLOSE) {
			smileyView.setImageResource(R.drawable.smiley_not_close);
			areWeThereTextView.setText(R.string.location_found_not_close);
		}
	}
	
	/**
	 * Called when the ok button is clicked
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
