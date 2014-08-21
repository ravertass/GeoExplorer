package net.sfabian.geoexplorer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

/**
 * This activity is shown the first time the user tries to add a photolocation.
 * It displays the terms for doing so and will not be shown again if they are accepted.
 * 
 * @author sfabian
 */

public class TermsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_terms);
	}
	
	/** 
	 * Called when the OK button is clicked
	 */
	public void gotoAddLocation(View view) {
		// Save in shared preferences that the user has pressed the OK button.
		// This means this activity should not be shown again.
		SharedPreferences sharedPrefs = getSharedPreferences(AddLocationActivity.SHARED_PREFS_NAME, MODE_PRIVATE);
		sharedPrefs.edit().putBoolean(AddLocationActivity.SHARED_PREFS_ADDED_BEFORE_KEY, true).apply();
		
		finish();
	}
	
	@Override
	public void onBackPressed() {
		Intent intent = new Intent(getApplicationContext(), MainActivity.class);
		// If this flag and this extra were not set, the application would go to the
		// AddLocationActivity when pressing the back button - but we want to go back to
		// the main menu, because the user should not be able to add photolocations if they
		// have not accepted the terms.
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}
}
