package net.sfabian.geoexplorer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

/**
 * This is the application's main activity.
 * It as a menu to do different things in the app.
 * 
 * @author sfabian
 */

public class MainActivity extends ActionBarActivity {

	// Keys for the shared preferences, used to keep track of if the user has used the app before.
	public static final String SHARED_PREFS_NAME = "main_shared_prefs";
	public static final String SHARED_PREFS_PLAYED_BEFORE_KEY = "played_before_shared_prefs";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Flag to keep track of if the user clicked "back" in the FirstTimeActivity.
		// It is needed, because otherwise the code may continue after finish() is called, and
		// the FirstTimeActivity is displayed again.
		boolean finishing = false;
		// This will happen if the user clicks "back" in the FirstTimeActivity.
		// This means that the user wants to exit the application, which happens here.
		if (getIntent().getBooleanExtra(getString(R.string.intent_key_stop_main_activity), false)) {
			finishing = true;
			finish();
		}
		
		// Get if the user has started the app before from shared preferences.
		SharedPreferences sharedPrefs = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
		boolean playedBefore = sharedPrefs.getBoolean(SHARED_PREFS_PLAYED_BEFORE_KEY, false);
		
		// If the user has not started the app before, or has only pressed 'back' in the FirstTimeActivity
		if (!finishing && !playedBefore) {
			// Start the FirstTimeActivity
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
	
	/**
	 * This is overridden to add the choose player button to the action button.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate menu items for the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	/**
	 * This is overridden to add the choose player button to the action bar.
	 * Here the functionality of the button is added.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_choose_player_button:
			choosePlayer();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void choosePlayer() {
		Intent intent = new Intent(this, ChoosePlayerActivity.class);
		startActivity(intent);
	}
	
}
