package net.sfabian.geoexplorer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

public class ReportLocationActivity extends Activity {

	private boolean explicitCheckboxChecked = false;
	private boolean impossibleCheckboxChecked = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_report_location);
		
		TextView photoView = (TextView) findViewById(R.id.report_location_photo);
		
		Intent intent = getIntent();
		String photoString = intent.getStringExtra(getString(R.string.intent_key_photo_location));
		photoView.setText(photoString);
	}
	
	/**
	 * This is called when a checkbox is clicked.
	 */
	public void onCheckboxClicked(View view) {
		boolean checked = ((CheckBox) view).isChecked();
		
		int viewId = view.getId();
		if (viewId == R.id.report_location_explicit_checkbox) {
			explicitCheckboxChecked = checked;
		} else if (viewId == R.id.report_location_impossible_checkbox) {
			impossibleCheckboxChecked = checked;
		}
	}
	
	/**
	 * This is called when the report button is clicked.
	 */
	public void gotoLocationReported(View view) {
		//TODO: Här ska fotot faktiskt rapporteras också
		
		Intent intent = new Intent(this, LocationReportedActivity.class);
		startActivity(intent);
	}
}
