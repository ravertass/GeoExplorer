package net.sfabian.geoexplorer;

import java.util.ArrayList;
import java.util.HashSet;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;
import com.google.android.gms.location.LocationStatusCodes;

public class ExploreGridActivity extends AbstractPlayServicesActivity 
				implements OnAddGeofencesResultListener {
	
	private static final int PHOTOS_IN_ROW = 3;
	public static final String GEOFENCE_BROADCAST = "geofence_broadcast";
	public static final String GEOFENCE_ID = "geofence_id";
	public static final String GEOFENCE_TRANSITION_TYPE = "geofence_transition_type";
	
	private LinearLayout photoGrid;
	
	private ArrayList<PhotoLocation> photoLocations;
	private boolean gridInitialized = false;
	private int photoSideLength;
	private boolean connected = false;
	private HashSet<Integer> thereGeofences;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_explore_grid);
		
		thereGeofences = new HashSet<Integer>();
		
		photoGrid = (LinearLayout) findViewById(R.id.explore_grid_photo_grid);
		
		// This registers this activity as a receiver for the broadcasts from the
		// geofence system
		LocalBroadcastManager.getInstance(this).registerReceiver(
				messageReceiver, new IntentFilter(GEOFENCE_BROADCAST));
	}
	
	/**
	 * This receiver holds the callback that will be called from the geofence system
	 * when a geofence transition is made.
	 */
	private BroadcastReceiver messageReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			int photoLocationId = Integer.parseInt(intent.getStringExtra(GEOFENCE_ID));
			int transitionType = intent.getIntExtra(GEOFENCE_TRANSITION_TYPE, -1);
			
			Log.d(this.getClass().toString(), "Geofence transition! ID: "
					+ GEOFENCE_ID + ", type: " + GEOFENCE_TRANSITION_TYPE );
			
			// If the device entered a geofence, we will note it is at the corresponding
			// photolocation
			if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
				thereGeofences.add(photoLocationId);
			} else if (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT) {
				if (thereGeofences.contains(photoLocationId)) {
					thereGeofences.remove(photoLocationId);
				}
			}
		}
	};
	
	/**
	 * The grid is initialized in this method, because we need the size of
	 * the grid layout to calculate the height of the images (since we want
	 * them to be rectangular).
	 * TODO: Detta kanske ändras när vi väl stoppar in bilder här
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (!gridInitialized) {
			int gridWidth = photoGrid.getWidth();
			photoSideLength = gridWidth / PHOTOS_IN_ROW;
			getPhotoLocationsFromDatabase();
			initializeGrid();
		}
	}
	
	private void getPhotoLocationsFromDatabase() {
		DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
		
		photoLocations = databaseHelper.getAllPhotoLocations(photoSideLength, photoSideLength);
	}

	// TODO: Ett annat antal bilder per rad behövs i landscape-läge!
	private void initializeGrid() {
		LinearLayout photoRow = null; //since the compiler makes me do this
		for (int i = 0; i < photoLocations.size(); i++) {
			if (i % PHOTOS_IN_ROW == 0) {
				photoRow = new LinearLayout(this);
				photoRow.setOrientation(LinearLayout.HORIZONTAL);
				photoRow.setWeightSum(PHOTOS_IN_ROW);
				photoRow.setLayoutParams(new LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
				photoGrid.addView(photoRow);
			}
			
			final PhotoLocation photoLocation = photoLocations.get(i);
			
			ImageButton photoButton = new ImageButton(this);
			
			LinearLayout.LayoutParams photoParams = new LinearLayout.LayoutParams(
					photoSideLength, photoSideLength, 1);
			photoButton.setLayoutParams(photoParams);

			photoButton
					.setImageBitmap(BitmapHelper.getCroppedScaledBitmap(
							photoLocation.getPhoto(), photoSideLength, photoSideLength));
			
			photoButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					gotoExploreLocation(photoLocation.getId());
				}
			});
			
			photoRow.addView(photoButton);
		}
		gridInitialized = true;
		createAndAddGeofences();
	}
	
	/**
	 * Called when a photo in the grid is clicked 
	 */
	public void gotoExploreLocation(int id) {
		Intent intent = new Intent(this, ExploreLocationActivity.class);
		intent.putExtra(getString(R.string.intent_key_photo_location), id);
		// TODO gör detta med enum-proximity
		intent.putExtra(getString(R.string.intent_key_proximity), thereGeofences.contains(id));
		startActivity(intent);
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		connected  = true;
		
		createAndAddGeofences();
	}
	
	private void createAndAddGeofences() {
		// This check is to be sure that both onWindowFocusChanged() and
		// onConnected() callbacks have been run
		if (connected && gridInitialized) {
			// Get geofences from all photolocations
			ArrayList<Geofence> geofences = new ArrayList<Geofence>();
			for (PhotoLocation photoLocation : photoLocations) {
				geofences.add(photoLocation.toThereGeofence());
			}
			
			// Here we send the intent to start the tracking of geofences
			PendingIntent pendingIntent = getTransitionPendingIntent();
			locationClient.addGeofences(geofences, pendingIntent, this);
		}
	}
	
	private PendingIntent getTransitionPendingIntent() {
		Intent intent = new Intent(this, ReceiveGeofenceTransitionsIntentService.class);
		
		//TODO Varför dessa parametrar?
		return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	@Override
	public void onAddGeofencesResult(int statusCode, String[] geofenceRequestIds) {
		// TODO Auto-generated method stub
		if (statusCode == LocationStatusCodes.SUCCESS) {
			Log.e(this.getClass().toString(), "success");
		}
	}
}
