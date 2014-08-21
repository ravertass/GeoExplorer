package net.sfabian.geoexplorer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * This activity is shown when the user has added their own photolocation.
 * It shows the photolocation and its name, and lets the user go back to the main menu. 
 * 
 * @author sfabian
 */

public class LocationAddedActivity extends Activity {
	
	// The layout view where the photo will be shown.
	private ImageView photoView;
	
	// The added photolocation.
	private PhotoLocation photoLocation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location_added);
		
		// Get layout entities.
		TextView locationNameView = (TextView) findViewById(R.id.location_added_location_name);
		photoView = (ImageView) findViewById(R.id.location_added_location_photo);
		
		// Get the ID of the added photolocation, so that it can be selected from the local database.
		Intent intent = getIntent();
		int photoLocationId = intent.getIntExtra(getString(R.string.intent_key_photo_location), -1);
		
		// Get the photolocation from the local database.
		DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
		photoLocation = databaseHelper.getPhotoLocation(photoLocationId);
		
		// Display the photolocation's name and display the photolocation photo.
		locationNameView.setText(getString(R.string.explore_location_location) + " " + photoLocation.getLocationName());
		photoView.setImageBitmap(photoLocation.getPhoto());
	}
	
	/**
	 * Called when the OK button is clicked
	 */
	public void gotoMain(View view) {
		onBackPressed();
	}
	
	/**
	 * The way this is overriden, the user will not be taken back to the
	 * AddLocationActivity when pressing back or OK, they will be taken
	 * to the main menu.
	 */
	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}
}
