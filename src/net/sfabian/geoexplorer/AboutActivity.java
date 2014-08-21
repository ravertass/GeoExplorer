package net.sfabian.geoexplorer;

import android.app.Activity;
import android.os.Bundle;

/**
 * This activity simply contains a text about the app.
 * @author sfabian
 */

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
	}
}
