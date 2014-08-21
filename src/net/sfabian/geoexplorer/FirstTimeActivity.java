package net.sfabian.geoexplorer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

/**
 * This activity is shown the first time the application is started, and gives
 * some info about the app. 
 * 
 * @author sfabian
 */

public class FirstTimeActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_first_time);
	}
	
	/** 
	 * Called when the OK button is clicked
	 */
	public void gotoMain(View view) {
		// Save in shared preferences that the user has pressed the OK button.
		// This means this activity should not be shown again.
		SharedPreferences sharedPrefs = getSharedPreferences(MainActivity.SHARED_PREFS_NAME, MODE_PRIVATE);
		sharedPrefs.edit().putBoolean(MainActivity.SHARED_PREFS_PLAYED_BEFORE_KEY, true).apply();
		
		finish();
	}
	
	@Override
	public void onBackPressed() {
		Intent intent = new Intent(getApplicationContext(), MainActivity.class);
		// If this flag and this extra were not set, the application would go to the
		// main menu when pressing the back button - but we want to exit the app, since
		// the user has not really seen the main menu.
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(getString(R.string.intent_key_stop_main_activity), true);
		
		startActivity(intent);
		finish();
	}
}
