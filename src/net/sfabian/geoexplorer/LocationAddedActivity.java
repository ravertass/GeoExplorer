package net.sfabian.geoexplorer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class LocationAddedActivity extends Activity {
	
	private ImageView photoView;
	
	private PhotoLocation photoLocation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location_added);
		
		TextView locationNameView = (TextView) findViewById(R.id.location_added_location_name);
		photoView = (ImageView) findViewById(R.id.location_added_location_photo);
		
		Intent intent = getIntent();
		int photoLocationId = intent.getIntExtra(getString(R.string.intent_key_photo_location), -1);
		
		DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
//		PhotoLocation photoLocation = databaseHelper.getAllPhotoLocations().get(0);
		photoLocation = databaseHelper.getPhotoLocation(photoLocationId);
		
		locationNameView.setText(getString(R.string.explore_location_location) + " " + photoLocation.getLocationName());
		photoView.setImageBitmap(photoLocation.getPhoto());
	}
	
//	public void onWindowFocusChanged(boolean hasFocus) {
//		int width = photoView.getWidth();
//		int height = photoView.getHeight();
//		photoView.setImageBitmap(photoLocation.getPhoto());
//	}
	
	/**
	 * Called when the OK button is clicked
	 */
	public void gotoMain(View view) {
		onBackPressed();
	}
	
	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}
}
