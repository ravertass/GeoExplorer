package net.sfabian.geoexplorer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class ExploreMenuActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_explore_menu);
	}

	/** 
	 * Called when the a button is clicked
	 * TODO vilken?
	 */
	public void gotoExploreGrid(View view) {
		Intent intent = new Intent(this, ExploreGridActivity.class);
		//intent.putExtra(LOCATION_PHOTOS_DIR_PATH, locationPhotosDirPath);
		startActivity(intent);
	}
	
	/** 
	 * Called when the tutorial button is clicked
	 */
	public void gotoTutorial(View view) {
		Intent intent = new Intent(this, RestTestActivity.class);
		startActivity(intent);
	}
}
