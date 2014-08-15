package net.sfabian.geoexplorer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class AddLocationActivity extends AbstractPlayServicesActivity {

	private Button addLocationButton;
	private ImageView photoView;
	private String photoString;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO: Här ska vi kolla om AddLocation har körts förut, det kommer att sparas i SharedPref
		if (true) {
			Intent intent = new Intent(this, TermsActivity.class);
			startActivity(intent);
		}
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_location);
		
		photoView = (ImageView) findViewById(R.id.add_location_photo);
		addLocationButton = (Button) findViewById(R.id.add_location_add_location_button);
		
		
	}
	
	/**
	 * Called when the "take photo" button is clicked
	 */
	public void takePhotograph(View view) {
		// TODO: Här ska ett intent skickas så att ett foto tas. Spännande!
		photoString = "fruitfly";
		
		addLocationButton.setEnabled(true);
	}
	
	/**
	 * Called when the "add location" button is clicked
	 */
	public void gotoLocationAdded(View view) {
		// TODO lägg till locationen här...
		Intent intent = new Intent(this, LocationAddedActivity.class);
		intent.putExtra(getString(R.string.intent_key_photo_location), photoString);
		startActivity(intent);
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		// TODO Auto-generated method stub
		
	}
}
