package net.sfabian.geoexplorer;

import android.app.Activity;
import android.content.Intent;
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
		// TODO: Här ska vi i SharedPref sätta att AddLocation har körts, så att man inte kommer till
		// den här aktiviteten igen
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
