package net.sfabian.geoexplorer;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;

/**
 * TODO
 * Idéen är att denna klass ska implementera alla metoder som behövs för att connecta med
 * Google Play Services, och att de klasser som behöver GPS-funktionalitet ska ärva från denna.
 * Tänkbart bör även andra metoder för GPS-funktionaliteten ligga här, men det är oklart just nu!
 * 
 * @author fabian
 */
public abstract class AbstractPlayServicesActivity extends FragmentActivity implements 
	OnConnectionFailedListener, ConnectionCallbacks {

	private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	protected LocationClient locationClient;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		locationClient = new LocationClient(this, this, this);
	}
	
	/**
	 * Called when the activity becomes visible.
	 */
	@Override
	protected void onStart() {
		super.onStart();
		connect();
	}
	
	private void connect() {
		if (playServicesConnected()) {
			locationClient.connect();
		}
	}
	
	/**
	 * Called when the activity is no longer visible.
	 */
	@Override
	protected void onStop() {
		super.onStop();
		locationClient.disconnect();
	}

	private boolean playServicesConnected() {
		// Check if the Google Play services are available
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		// If services are available
		if (resultCode == ConnectionResult.SUCCESS) {
			return true;
		// If it for some reason was not available
		} else {
			// Get the error dialog from Google Play Services
			Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
					resultCode,
					this,
					CONNECTION_FAILURE_RESOLUTION_REQUEST);
			// If Google Play Services can provide an error dialog
			if (errorDialog != null) {
				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
				errorFragment.setDialog(errorDialog);
				errorFragment.show(getSupportFragmentManager(), "Location Updates");
			}
			return false;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CONNECTION_FAILURE_RESOLUTION_REQUEST) {
			if (resultCode == Activity.RESULT_OK) {
				connect();
			}
		}
	}

	@Override
	public abstract void onConnected(Bundle connectionHint);

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();

	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// TODO Auto-generated method stub
		// TODO: Den här metoden borde göra en del mer
		Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();

	}
}