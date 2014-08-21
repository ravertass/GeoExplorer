package net.sfabian.geoexplorer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

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
		SharedPreferences sharedPrefs = getSharedPreferences(AddLocationActivity.SHARED_PREFS_NAME, MODE_PRIVATE);
		sharedPrefs.edit().putBoolean(AddLocationActivity.SHARED_PREFS_ADDED_BEFORE_KEY, true).apply();
		
		finish();
	}
	
	@Override
	public void onBackPressed() {
		Intent intent = new Intent(getApplicationContext(), MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}
}
