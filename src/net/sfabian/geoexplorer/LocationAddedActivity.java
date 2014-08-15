package net.sfabian.geoexplorer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class LocationAddedActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location_added);
		
		TextView locationNameView = (TextView) findViewById(R.id.location_added_location_name);
		TextView photoView = (TextView) findViewById(R.id.location_added_location_photo);
		
		// TODO: Plocka detta ur lokal databas, om stället är funnet
		locationNameView.setText(getString(R.string.explore_location_location) + " " + "Name of location");
		
		Intent intent = getIntent();
		String photoString = intent.getStringExtra(getString(R.string.intent_key_photo_location));
		photoView.setText(photoString);
	}
	
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
