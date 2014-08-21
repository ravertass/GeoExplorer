package net.sfabian.geoexplorer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

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
		SharedPreferences sharedPrefs = getSharedPreferences(MainActivity.SHARED_PREFS_NAME, MODE_PRIVATE);
		sharedPrefs.edit().putBoolean(MainActivity.SHARED_PREFS_PLAYED_BEFORE_KEY, true).apply();
		finish();
	}
	
	@Override
	public void onBackPressed() {		
		Intent intent = new Intent(getApplicationContext(), MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(getString(R.string.intent_key_stop_main_activity), true);
		startActivity(intent);
		finish();
	}
}
