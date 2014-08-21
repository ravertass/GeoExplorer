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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
	private static final String BUNDLE_PHOTOLOCATIONS_LOADED = "bundle_photolocations_loaded";
	
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
	
	@SuppressWarnings("static-access")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_explore_grid);
		
		// If the device was rotated
		if (savedInstanceState != null) {
			connected = savedInstanceState.getBoolean(BUNDLE_CONNECTED, false);
			windowFocusChanged = savedInstanceState.getBoolean(
					BUNDLE_WINDOW_FOCUS_CHANGED, false);
			geofencesAddedAndPhotoLocationsLoadedFromServer = savedInstanceState
					.getBoolean(BUNDLE_PHOTOLOCATIONS_LOADED, false);
		}
		
		// Set the correct amount of photos per row based on the device orientation
		if (getResources().getConfiguration().orientation == getResources()
				.getConfiguration().ORIENTATION_LANDSCAPE) {
			PHOTOS_IN_ROW = PHOTOS_IN_ROW_LANDSCAPE;
		} else {
			PHOTOS_IN_ROW = PHOTOS_IN_ROW_PORTRAIT;
		}
		
		// This keeps track of how close the user is to the photolocations
		locationProximities = new SparseArray<ExploreLocationActivity.ProximityToLocation>();
		
		// Get the photo grid view
		photoGrid = (LinearLayout) findViewById(R.id.explore_grid_photo_grid);
		
		// This registers this activity as a receiver for the broadcasts from the
		// geofence system
		LocalBroadcastManager.getInstance(this).registerReceiver(
				messageReceiver, new IntentFilter(GEOFENCE_BROADCAST));
		
		// See callbacks onWindowFocusChanged() and onConnected() for what happens next
	}
	
	/**
	 * This callback is used because it is first here that we are able
	 * to calculate the widths of the photos. The widths are needed to sample
	 * and diplay the photos correctly.
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		
		// This should happen only on rotation
		// This will display the photos
		if (windowFocusChanged && !gridInitialized) {
			getPhotoWidth();
			getPhotoLocationsFromDatabase();
			initializeGrid();
		}
		
		windowFocusChanged = true;
		
		// This should happen the first time this activity is created (i.e. not on rotation)
		if (connected && windowFocusChanged && !geofencesAddedAndPhotoLocationsLoadedFromServer) {
			getPhotoWidth();
			// This method call will get the photolocations from the server and continue
			// a thread of methods and callbacks.
			getPhotoLocationsFromServer();
		}
	}
	
	/**
	 * This method is run when the location client is connected.
	 */
	@Override
	public void onConnected(Bundle connectionHint) {
		if (connected) {
			return;
		}
		
		connected = true;
		
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
	

	/**
	 * This method removes all the photolocations from the local database.
	 * It then starts an asynchronized task to retrieve the nearby photolocations from our server.
	 */
	private void getPhotoLocationsFromServer() {
		// Remove photolocations from local database
		DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
		databaseHelper.removeAllPhotoLocations();
		
		// Get the user's current location
		Location location = locationClient.getLastLocation();
		playerLatitude = location.getLatitude();
		playerLongitude = location.getLongitude();

		// This gets the URL for our API
		String url = RestClient.API_URL;
		// Connect to the internet
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		// If we are connected
		if (networkInfo != null && networkInfo.isConnected()) {
			// Start the asynchronized task that connects to the server
			new ConnectToWebTask().execute(url);
		} else {
			Log.e(getClass().toString(), "Could not connect to the internet");
			Toast.makeText(this, "Could not connect to the internet", Toast.LENGTH_LONG).show();
			finish();
		}
	}
	
	/**
	 * This class connects to our server and performs the HTTP POST operation
	 * to get photolocations from the server. It is done in an asynchronized task
	 * because Android needs network operations to be done that way.
	 * @author sfabian
	 */
	private class ConnectToWebTask extends AsyncTask<String, Void, String> {
		/**
		 * Sends a HTTP POST request to the server to retrieve nearby photolocations.
		 */
		@Override
		protected String doInBackground(String... urls) {
			return RestClient.doGetFromServer(urls[0], playerLatitude, playerLongitude);
		}
		/**
		 * This callback is run after the above method. It adds the retrieved photolocations
		 * to the local database.
		 */
		@Override
		protected void onPostExecute(String result) {
			// Add retrieved photolocations to the local database.
			DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
			boolean areTherePhotoLocationsNearby = databaseHelper
					.addPhotoLocationsFromJson(result, getApplicationContext());
			
			// If there were any photolocations nearby
			if (areTherePhotoLocationsNearby) {
				// Load the PhotoLocation objects from the local photolocation database.
				getPhotoLocationsFromDatabase();
				// Create and add geofences for all photoloctations.
				createAndAddGeofences();
			} else {
				addNoPhotoLocationsView();
			}
		}
	}
	
	private void addNoPhotoLocationsView() {
		// This is done to remove the progress bar
		photoGrid.removeAllViews();
		
		TextView noPhotoLocationsView = new TextView(this);
		noPhotoLocationsView.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		noPhotoLocationsView.setText("No locations nearby. :(");
		
		photoGrid.addView(noPhotoLocationsView);
	}
	
	private void getPhotoLocationsFromDatabase() {
		DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
		
		photoLocations = databaseHelper.getAllPhotoLocations(photoSideLength, photoSideLength);
	}
	
	/**
	 * This method create and adds geofences for all loaded photolocations.
	 */
	private void createAndAddGeofences() {
		// Get geofences from all photolocations
		ArrayList<Geofence> geofences = new ArrayList<Geofence>();
		for (PhotoLocation photoLocation : photoLocations) {
			// We create geofences with a smaller radius ("THERE geofences").
			// When the user is in one of them, they can find the photolocation.
			geofences.add(photoLocation.toThereGeofence());
			// We also create geofences with a larger radius ("CLOSE geofences"):
			// When the user is in one of them, they can see that they are close.
			geofences.add(photoLocation.toCloseGeofence());
		}
			
		// Here we send the intent to start the tracking of geofences
		PendingIntent pendingIntent = getTransitionPendingIntent();
		locationClient.addGeofences(geofences, pendingIntent, this);
		
		// See callback "onAddGeofencesResult()" for what happens next
	}
	
	private PendingIntent getTransitionPendingIntent() {
		Intent intent = new Intent(this, ReceiveGeofenceTransitionsIntentService.class);
		
		return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}
	
	/**
	 * This is called when the geofences have been added.
	 */
	@Override
	public void onAddGeofencesResult(int statusCode, String[] geofenceRequestIds) {
		// Initialize the photo grid.
		initializeGrid();
		// This means we are kind of done!
		geofencesAddedAndPhotoLocationsLoadedFromServer = true;
	}
	
	/**
	 * This method creates views for all photolocations and adds them to the photo grid.
	 */
	private void initializeGrid() {
		// This is done to remove the progress bar
		photoGrid.removeAllViews();
		LinearLayout photoRow = null; 
		// For all the loaded photolocations
		for (int i = 0; i < photoLocations.size(); i++) {
			// Add a new row to the grid if it's time for one 
			if (i % PHOTOS_IN_ROW == 0) {
				// Create the row view and set its layout parameters
				photoRow = new LinearLayout(this);
				photoRow.setOrientation(LinearLayout.HORIZONTAL);
				photoRow.setWeightSum(PHOTOS_IN_ROW);
				photoRow.setLayoutParams(new LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
				// Add the row to the photo grid
				photoGrid.addView(photoRow);
			}
			
			final PhotoLocation photoLocation = photoLocations.get(i);
			
			// Create a button for the photolocation.
			ImageButton photoButton = new ImageButton(this);
			
			// Set the button's layout parameters.
			LinearLayout.LayoutParams photoParams = new LinearLayout.LayoutParams(
					photoSideLength, photoSideLength);
			photoButton.setLayoutParams(photoParams);

			// Set a cropped, scaled version of the photolocation photo as image
			// for the button. 
			photoButton
					.setImageBitmap(BitmapHelper.getCroppedScaledBitmap(
							photoLocation.getPhoto(), photoSideLength, photoSideLength));
			
			// Set a listener with a callback method for pressing the photolocation button
			photoButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					// This goes to the ExploreLocationActivity for this photolocation
					gotoExploreLocation(photoLocation.getId());
				}
			});
			
			// Add the button to the current row
			photoRow.addView(photoButton);
		}
		// Now the grid is initialized!
		gridInitialized = true;
	}
	

	/**
	 * Called when a photo button in the grid is pressed.
	 */
	public void gotoExploreLocation(int id) {
		// Create an intent for the ExploreLocationActivity with this photolocation
		Intent intent = new Intent(this, ExploreLocationActivity.class);
		// Add the photolocation's ID
		intent.putExtra(getString(R.string.intent_key_photo_location), id);
		// Get the proximity to the location
		ExploreLocationActivity.ProximityToLocation proximity = locationProximities.get(id);
		// If the proximity was not defined
		if (proximity == null) {
			// we say that the user is not close
			proximity = ExploreLocationActivity.ProximityToLocation.NOT_CLOSE;
		}
		// Attach the proximity to the intent
		proximity.attachTo(intent);
		
		// Start the ExploreLocationActivity
		startActivity(intent);
	}
	
	/**
	 * This receiver holds the callback that will be called from the geofence service
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
			
			// Get the transition type (exit, enter etc.)
			int transitionType = intent.getIntExtra(GEOFENCE_TRANSITION_TYPE, -1);
			
			// If the device entered a geofence, we will note it is at the corresponding
			// photolocation
			if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
				// If the device enters and is at the photolocation
				if (geofenceType.equals(PhotoLocation.GEOFENCE_THERE)) {
					// Note the proximity to the photolocation
					locationProximities.put(photoLocationId, ExploreLocationActivity.ProximityToLocation.THERE);
					// If the device enters the proximity of the photolocation
				} else if (geofenceType.equals(PhotoLocation.GEOFENCE_CLOSE)
						&& locationProximities.get(photoLocationId) != ExploreLocationActivity.ProximityToLocation.THERE) {
					// Note the proximity to the photolocation
					locationProximities.put(photoLocationId, ExploreLocationActivity.ProximityToLocation.CLOSE);
				}
			} else if (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT) {
				// If the device moves away from the photolocation
				if (geofenceType.equals(PhotoLocation.GEOFENCE_THERE)) {
					// Note the proximity to the photolocation
					locationProximities.put(photoLocationId, ExploreLocationActivity.ProximityToLocation.CLOSE);
					// If the device moves even further away from the photolocation
				} else if (geofenceType.equals(PhotoLocation.GEOFENCE_CLOSE)) {
					// Note the proximity to the photolocation
					locationProximities.put(photoLocationId, ExploreLocationActivity.ProximityToLocation.NOT_CLOSE);
				}
			}
		}
	};
	
	/**
	 * This is overriden to add the refresh button to the action bar.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate menu items for the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.explore_grid_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	/**
	 * This is overriden to add the refresh button to the action bar.
	 * Here the functionality of the button is added.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_refresh_button:
			refreshPhotoLocations();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	/**
	 * This method is run when the refresh button in the action bar is pressed.
	 * It loads photolocations from the server anew, adds geofences again and
	 * displays the photo buttons again.
	 */
	private void refreshPhotoLocations() {
		if (connected && windowFocusChanged && geofencesAddedAndPhotoLocationsLoadedFromServer) {
			// This will remove all photo buttons from the layout and add the progress bar anew.
			setContentView(R.layout.activity_explore_grid);
			// Get the photo grid view anew
			photoGrid = (LinearLayout) findViewById(R.id.explore_grid_photo_grid);
			
			// These should be set false (I believe) for methods to work.
			geofencesAddedAndPhotoLocationsLoadedFromServer = false;
			gridInitialized = false;
			// This will start the methods and callbacks to get and display new photolocations.
			getPhotoLocationsFromServer();
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		// If this is not checked, the app crashes on rotation when this has been done
		if (geofencesAddedAndPhotoLocationsLoadedFromServer) {
			// These are needed to keep track of if photolocations have been retrieved from
			// the server and the geofences have been initialized.
			savedInstanceState.putBoolean(BUNDLE_CONNECTED, connected);
			savedInstanceState.putBoolean(BUNDLE_WINDOW_FOCUS_CHANGED, windowFocusChanged);
			savedInstanceState.putBoolean(BUNDLE_PHOTOLOCATIONS_LOADED, geofencesAddedAndPhotoLocationsLoadedFromServer);
			super.onSaveInstanceState(savedInstanceState);
		}
	}
}
