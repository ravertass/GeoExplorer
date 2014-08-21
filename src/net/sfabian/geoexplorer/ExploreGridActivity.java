package net.sfabian.geoexplorer;

import java.util.ArrayList;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;

/**
 * This activity displays a grid with photos of nearby photolocations.
 * The user can press photos to open an activity with that photo.
 * This is in a way the 'main game activity'.
 * 
 * @author sfabian
 */

// I hope it's possible to understand the thread the callbacks in this activity!

public class ExploreGridActivity extends AbstractPlayServicesActivity 
				implements OnAddGeofencesResultListener {
	
	// Kommentera och lägg till uppdatera-knapp i actionbaren.
	// Lägg också till laddaranimation, samt se till att det inte kraschar om inget hittas
	// och att det sägs då att inget hittades.
	private int todo;
	
	// These numbers keep track of how many photos should be in a row in the grid.
	private int PHOTOS_IN_ROW;
	private static final int PHOTOS_IN_ROW_LANDSCAPE = 5;
	private static final int PHOTOS_IN_ROW_PORTRAIT = 3;
	// These are keys used in intents to and from the geofence API.
	public static final String GEOFENCE_BROADCAST = "geofence_broadcast";
	public static final String GEOFENCE_ID = "geofence_id";
	public static final String GEOFENCE_TRANSITION_TYPE = "geofence_transition_type";
	// These keys are used to keep these variables on rotation. 
	private static final String BUNDLE_CONNECTED = "bundle_connected";
	private static final String BUNDLE_WINDOW_FOCUS_CHANGED = "bundle_window_focus_changed";
	
	// The grid layout with all photolocation photos.
	private LinearLayout photoGrid;
	
	// The photolocations in the grid.
	private ArrayList<PhotoLocation> photoLocations;
	// The side dimensions of the photos in the grid, used to sample and display photos correctly.
	// It is initialized first in onWindowFocusChanged().
	private int photoSideLength;
	// These variables keep track if certain tasks have been performed, because
	// they are needed to be checked in callbacks that may be called in different orders.
	private boolean gridInitialized = false;
	private boolean connected = false;
	private boolean windowFocusChanged = false;
	private boolean geofencesAddedAndPhotoLocationsLoadedFromServer = false;
	// The current latitude and longitude of the player.
	private double playerLatitude;
	private double playerLongitude;
	// This map tells us how close the device is to a certain location (the keys are photoLoc IDs).
	private SparseArray<ExploreLocationActivity.ProximityToLocation> locationProximities;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_explore_grid);
		
		if (savedInstanceState != null) {
			connected = savedInstanceState.getBoolean(BUNDLE_CONNECTED, false);
			windowFocusChanged = savedInstanceState.getBoolean(BUNDLE_WINDOW_FOCUS_CHANGED, false);
		}
		
		if (getResources().getConfiguration().orientation == getResources()
				.getConfiguration().ORIENTATION_LANDSCAPE) {
			PHOTOS_IN_ROW = PHOTOS_IN_ROW_LANDSCAPE;
		} else {
			PHOTOS_IN_ROW = PHOTOS_IN_ROW_PORTRAIT;
		}
		
		locationProximities = new SparseArray<ExploreLocationActivity.ProximityToLocation>();
		
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
			// Get the geofence ID
			String geofenceId = intent.getStringExtra(GEOFENCE_ID);
			String[] idParts = geofenceId.split("_");
			// The prefix of the geofenceId will tell us if it was a closeGeofence or thereGeofence
			String geofenceType = idParts[0] + "_";
			int photoLocationId = Integer.parseInt(idParts[1]);
			
			int transitionType = intent.getIntExtra(GEOFENCE_TRANSITION_TYPE, -1);
			
			Log.d(this.getClass().toString(), "Geofence transition! ID: "
					+ GEOFENCE_ID + ", type: " + GEOFENCE_TRANSITION_TYPE );
			
			// If the device entered a geofence, we will note it is at the corresponding
			// photolocation
			if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
				// If the device enters is at the photo location
				if (geofenceType.equals(PhotoLocation.GEOFENCE_THERE)) {
					locationProximities.put(photoLocationId, ExploreLocationActivity.ProximityToLocation.THERE);
					// If the device enters the proximity of the photo location
				} else if (geofenceType.equals(PhotoLocation.GEOFENCE_CLOSE)
						&& locationProximities.get(photoLocationId) != ExploreLocationActivity.ProximityToLocation.THERE) {
					locationProximities.put(photoLocationId, ExploreLocationActivity.ProximityToLocation.CLOSE);
				}
			} else if (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT) {
				// If the device moves away from the photo location
				if (geofenceType.equals(PhotoLocation.GEOFENCE_THERE)) {
					locationProximities.put(photoLocationId, ExploreLocationActivity.ProximityToLocation.CLOSE);
					// If the device moves even further away from the photo location
				} else if (geofenceType.equals(PhotoLocation.GEOFENCE_CLOSE)) {
					locationProximities.put(photoLocationId, ExploreLocationActivity.ProximityToLocation.NOT_CLOSE);
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
		
//		nytt
		// This should happen only on rotation
		if (windowFocusChanged && !gridInitialized) {
			getPhotoWidth();
			getPhotoLocationsFromDatabase();
			initializeGrid();
		}
		
		windowFocusChanged = true;
		
		// This should happen the first time this activity is created (i.e. not on rotation)
		if (connected && windowFocusChanged && !geofencesAddedAndPhotoLocationsLoadedFromServer) {
			getPhotoWidth();
			getPhotoLocationsFromServer();
		}
	}

	private void getPhotoWidth() {
		int gridWidth = photoGrid.getWidth();
		photoSideLength = gridWidth / PHOTOS_IN_ROW;
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
					photoSideLength, photoSideLength);
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
	}
	
	/**
	 * Called when a photo in the grid is clicked 
	 */
	public void gotoExploreLocation(int id) {
		Intent intent = new Intent(this, ExploreLocationActivity.class);
		intent.putExtra(getString(R.string.intent_key_photo_location), id);
		// TODO gör detta med enum-proximity
		ExploreLocationActivity.ProximityToLocation proximity = locationProximities.get(id);
		if (proximity == null) {
			proximity = ExploreLocationActivity.ProximityToLocation.NOT_CLOSE;
		}
		proximity.attachTo(intent);
		startActivity(intent);
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		connected = true;
		
		// This should happen the first time this activity is created (i.e. not on rotation)
		if (connected && windowFocusChanged && !geofencesAddedAndPhotoLocationsLoadedFromServer) {
			getPhotoWidth();
			getPhotoLocationsFromServer();
		}
	}
	
	private void createAndAddGeofences() {
		// Get geofences from all photolocations
		ArrayList<Geofence> geofences = new ArrayList<Geofence>();
		for (PhotoLocation photoLocation : photoLocations) {
			geofences.add(photoLocation.toThereGeofence());
			geofences.add(photoLocation.toCloseGeofence());
		}
			
		// Here we send the intent to start the tracking of geofences
		PendingIntent pendingIntent = getTransitionPendingIntent();
		locationClient.addGeofences(geofences, pendingIntent, this);
	}
	
	private PendingIntent getTransitionPendingIntent() {
		Intent intent = new Intent(this, ReceiveGeofenceTransitionsIntentService.class);
		
		//TODO Varför dessa parametrar?
		return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	@Override
	public void onAddGeofencesResult(int statusCode, String[] geofenceRequestIds) {
		initializeGrid();
		geofencesAddedAndPhotoLocationsLoadedFromServer = true;
	}
	
	private void getPhotoLocationsFromServer() {
		DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
		databaseHelper.removeAllPhotoLocations();
		
		Location location = locationClient.getLastLocation();
		playerLatitude = location.getLatitude();
		playerLongitude = location.getLongitude();
		
		String url = RestClient.API_URL;
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			new ConnectToWebTask().execute(url);
		} else {
			//helloView.setText("No network connection available.");
		}
	}
	
	private class ConnectToWebTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			return RestClient.doGetFromServer(urls[0], playerLatitude, playerLongitude);
		}
		@Override
		protected void onPostExecute(String result) {
			DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
			databaseHelper.addPhotoLocationsFromJson(result, getApplicationContext());
			
			getPhotoLocationsFromDatabase();
			createAndAddGeofences();
		}
	}
}
