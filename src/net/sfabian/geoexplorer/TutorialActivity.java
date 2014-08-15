package net.sfabian.geoexplorer;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class TutorialActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tutorial);
	}
	
	/**
	 * This is called when the OK button is clicked.
	 */
	public void gotoExploreMenu(View view) {
		finish();
	}
}
