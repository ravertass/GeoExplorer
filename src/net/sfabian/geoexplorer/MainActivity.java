package net.sfabian.geoexplorer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}
	
	/** 
	 * Called when the Game button is clicked in the main menu
	 */
	public void gotoExploreMenu(View view) {
		Intent intent = new Intent(this, ExploreMenuActivity.class);
		//intent.putExtra(LOCATION_PHOTOS_DIR_PATH, locationPhotosDirPath);
		startActivity(intent);
	}
}
