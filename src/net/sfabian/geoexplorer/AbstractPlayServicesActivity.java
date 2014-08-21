package net.sfabian.geoexplorer;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;

/**
 * This abstract class contains methods for connecting to the Google Play
 * Services using their api.
 * 
 * @author sfabian
 */
public abstract class AbstractPlayServicesActivity extends ActionBarActivity implements 
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

	/**
	 * This method checks if Google Play Services are connected
	 * and shows a error dialog fragment if it is not.
	 * @return if Google Play Services are connected
	 */
	private boolean playServicesConnected() {
		// Check if the Google Play services are available
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		// If services are available
		if (resultCode == ConnectionResult.SUCCESS) {
			return true;
		// If it for some reason was not available
		} else {
			// Get an error dialog from Google Play Services
			// This dialog may let the user correct what is wrong
			// If the user does, onActivityResult() will be run
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
	
	/**
	 * This callback is called if the Google Play Services error dialog lets the
	 * user try to fix connection problems. If connections problems are fixed, 
	 * the client will try to reconnect.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CONNECTION_FAILURE_RESOLUTION_REQUEST) {
			if (resultCode == Activity.RESULT_OK) {
				connect();
			}
		}
	}

	/**
	 * This method is run when the location client has connected.
	 * It is abstract, so the extending class needs to implement it.
	 * This means the methods in this abstract class will connect the location client,
	 * and the extending class can perform location operations after this callback has
	 * been run.
	 */
	@Override
	public abstract void onConnected(Bundle connectionHint);

	/**
	 * TODO: This should probably be handled differently.
	 * This method is called if the location client for some reason disconnects.
	 * This is handled in the most simple way here: the activity is finished and
	 * the user is told of this.
	 */
	@Override
	public void onDisconnected() {
		Toast.makeText(this, "Location client disconnected", Toast.LENGTH_SHORT).show();
		finish();
	}

	/**
	 * TODO: This should probably be handled differently.
	 * This method is run if the location client for some reason cannot connect.
	 * This is handled in the most simple way here: the activity is finished
	 * and the user is told of this. 
	 */
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Toast.makeText(this, "Location client connection failed", Toast.LENGTH_SHORT).show();
		finish();
	}
}