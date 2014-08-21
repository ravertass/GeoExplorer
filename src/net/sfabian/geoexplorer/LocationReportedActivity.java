package net.sfabian.geoexplorer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * This activity is shown when a photolocation has been reported by the user.
 * It thanks the user for doing so.
 * 
 * @author fabian
 */
public class LocationReportedActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location_reported);
	}
	
	/**
	 * This is called when the "continue exploring" button is clicked
	 */
	public void gotoExploreGrid(View view) {
		onBackPressed();
	}
	
	/**
	 * By overriding this method, the user will be taken back to the
	 * ExploreGridActivity instead of the ReportLocationActivity.
	 */
	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, ExploreGridActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}
}
