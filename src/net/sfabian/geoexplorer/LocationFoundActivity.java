package net.sfabian.geoexplorer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class LocationFoundActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location_found);
		
		TextView locationNameView = (TextView) findViewById(R.id.location_found_location_name);

		// TODO: Plocka detta ur lokal databas, om stället är funnet
		locationNameView.setText(getString(R.string.explore_location_location) + " " + "Name of location");
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
