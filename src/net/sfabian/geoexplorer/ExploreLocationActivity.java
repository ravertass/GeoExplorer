package net.sfabian.geoexplorer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ExploreLocationActivity extends Activity {
	
	public enum ProximityToLocation {
		NOT_CLOSE, CLOSE, THERE;
	}
	
	private String photoString;
	
	// TODO Texten på mittenknappen ska vara "OK" om man har hittat platsen. 
	// Den ska då bara leda tillbaka till griden, och inte till foundLocation.
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_explore_location);
		
		TextView locationNameView = (TextView) findViewById(R.id.explore_location_location_name);
		TextView photoView = (TextView) findViewById(R.id.explore_location_photo);
		TextView alreadyFoundTextView = (TextView) findViewById(R.id.explore_location_already_found_text);
		
		// TODO: Plocka detta ur lokal databas, om stället är funnet
		locationNameView.setText(getString(R.string.explore_location_location) + " " + "Name of location");
		
		Intent intent = getIntent();
		photoString = intent.getStringExtra(getString(R.string.intent_key_photo_location));
		photoView.setText(photoString);
		
		// TODO: Plocka ur en lokal databas om man har funnit stället. I så fall, visa denna text.
		alreadyFoundTextView.setText(R.string.explore_location_already_found);
	}
	
	/**
	 * Called when the "I think I'm here" button is clicked
	 */
	public void gotoLocationFound(View view) {
		Intent intent = new Intent(this, LocationFoundActivity.class);
		// TODO: Här ska vi skicka med hur nära användaren är till platsen.
		intent.putExtra(getString(R.string.intent_key_proximity), ProximityToLocation.THERE);
		// TODO: Här ska vi skicka med en databasnyckel till photolocationen i fråga.
		intent.putExtra(getString(R.string.intent_key_photo_location), photoString);
		startActivity(intent);
	}
	
	/**
	 * Called when the report button is clicked
	 */
	public void gotoReportLocation(View view) {
		Intent intent = new Intent(this, ReportLocationActivity.class);
		// TODO: Här ska vi skicka med en databasnyckel till photolocationen i fråga.
		intent.putExtra(getString(R.string.intent_key_photo_location), photoString);
		startActivity(intent);
	}
}
