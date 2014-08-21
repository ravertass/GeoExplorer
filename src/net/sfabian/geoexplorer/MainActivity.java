package net.sfabian.geoexplorer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

	public static final String SHARED_PREFS_NAME = "main_shared_prefs";
	public static final String SHARED_PREFS_PLAYED_BEFORE_KEY = "played_before_shared_prefs";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		boolean finishing = false;
		// This will happen if the user clicks "back" in the FirstTimeActivity
		if (getIntent().getBooleanExtra(getString(R.string.intent_key_stop_main_activity), false)) {
			finishing = true;
			finish();
		}
		
		SharedPreferences sharedPrefs = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
		boolean playedBefore = sharedPrefs.getBoolean(SHARED_PREFS_PLAYED_BEFORE_KEY, false);
		
		// TODO: Här ska vi också kolla om appen har körts förut, det kommer att sparas i SharedPref
		if (!finishing && !playedBefore) {
			Intent intent = new Intent(this, FirstTimeActivity.class);
			startActivity(intent);
		}
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}
		
	/** 
	 * Called when the "go exploring" button is clicked in the main menu
	 */
	public void gotoExploreMenu(View view) {
		Intent intent = new Intent(this, ExploreGridActivity.class);
		startActivity(intent);
	}
	
	/** 
	 * Called when the "add location" button is clicked in the main menu
	 */
	public void gotoAddLocation(View view) {
		Intent intent = new Intent(this, AddLocationActivity.class);
		startActivity(intent);
	}
	
	/** 
	 * Called when the "about" button is clicked in the main menu
	 */
	public void gotoAbout(View view) {
		Intent intent = new Intent(this, AboutActivity.class);
		startActivity(intent);
	}
}
