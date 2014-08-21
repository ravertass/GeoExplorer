package net.sfabian.geoexplorer;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class AddLocationActivity extends AbstractPlayServicesActivity {

	// Layout entities
	private Button addLocationButton;
	private ImageView photoView;
	private File tempPhotoFile;
	private Button takePhotoButton;
	private TextView locationTextView;
	private EditText locationNameEditView;
	
	// These variables will tell us the width and height of the photoView, which is needed
	// to optimally load the bitmap into the view. They cannot be set here, but must be set in
	// a callback.
	private int photoWidth = 0;
	private int photoHeight = 0;
	// Keeps track of if the location client is connected. Needed since callbacks may be run
	// in different orders.
	private boolean connected = false;
	// Keeps track of if a photo is taken. Needed since callbacks may be run in different orders.
	private boolean photoTaken = false;
	// The location from the location client, contains the player's latitude and longitude.
	private Location location;
	// The PhotoLocation that the player adds.
	private PhotoLocation photoLocation;
	
	// This width determines to what pixel width taken photos will be sampled.
	// It is chosen pragmatically: I tried higher numbers, but the server gave me errors
	// about too large transactions.
	private static final int SAMPLE_PHOTO_WIDTH = 500;
	// This number is needed, but will not really matter - the width is what will matter.
	private static final int SAMPLE_PHOTO_HEIGHT = 0;
	// The maximum number of characters for the location name.
	protected static final int MAX_LOCATION_NAME_LENGTH = 30;
	// This timestamp is used for temporary photo files saved by the Android camera.
	private static final String TIMESTAMP_FORMAT = "yyyyMMdd_HHmmss";
	
	// The name of the directory where the camera temporarily saves photo files.
	private static final String TEMP_PHOTOS_DIR_NAME = "temp_photos"; 
	// This key tells us if the callback onActivityResutl is run after a camera intent
	private static final int CAPTURE_IMAGE_REQUEST_CODE = 100; 
	// These keys are used when the activity is rotated to keep the photo if
	// one was taken.
	private static final String BUNDLE_PHOTO_PATH = "photo_path";
	private static final String BUNDLE_PHOTO_TAKEN = "photo_taken";
	// This is the name of the shared preferences used to keep track of if the
	// user as accepted the terms.
	public static final String SHARED_PREFS_NAME = "add_shared_prefs";
	// The key for the boolean that keeps track of if the user has accepted the terms.
	public static final String SHARED_PREFS_ADDED_BEFORE_KEY = "added_before_shared_prefs";
	// This key is used to keep the photo height on rotation
	private static final String BUNDLE_PHOTO_HEIGHT = "bundle_photo_height";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		// Get the shared preferences boolean to see if the user has accepted 
		// the terms of adding photolocations.
		SharedPreferences sharedPrefs = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
		boolean addedBefore = sharedPrefs.getBoolean(SHARED_PREFS_ADDED_BEFORE_KEY, false);
		
		// If the user has not accepted the terms and not added photolocations before
		if (!addedBefore) {
			// Start the activity with the terms
			Intent intent = new Intent(this, TermsActivity.class);
			startActivity(intent);
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_location);
		
		// Get all the layout entities
		locationNameEditView = (EditText) findViewById(R.id.add_location_edit_name);
		photoView = (ImageView) findViewById(R.id.add_location_photo);
		locationTextView = (TextView) findViewById(R.id.add_location_location_text);
		takePhotoButton = (Button) findViewById(R.id.add_location_take_photo_button);
		addLocationButton = (Button) findViewById(R.id.add_location_add_location_button);

		// Add the listener for the edit text view
		addEditTextListener();
		
		// If the device is rotated and a photo was taken, that photo should be kept
		if (savedInstanceState != null) {
			photoHeight = savedInstanceState.getInt(BUNDLE_PHOTO_HEIGHT);
			tempPhotoFile = new File(savedInstanceState.getString(BUNDLE_PHOTO_PATH));
			photoTaken = savedInstanceState.getBoolean(BUNDLE_PHOTO_TAKEN);
			// See callback "onWindowFocusChanged()" for what happens next
		} else {
			// Create the file where the camera will temporarily save the photo
			createTempPhotoFile();
		}
		
		// See callback "onConnected()" for what happens next
	}
	
	// This listener defines callbacks that will be run when the user
	// interacts with the edit text view for entering photolocation name.
	private void addEditTextListener() {
		locationNameEditView.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// do nothing
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// do nothing
			}

			@Override
			public void afterTextChanged(Editable s) {
				// The photolocation name should not be too long or short.
				if (s.length() >= MAX_LOCATION_NAME_LENGTH || s.length() < 0) {
					addLocationButton.setEnabled(false);
					// If a name of correct length is entered, we will try to
					// create a photo location.
				} else {
					createPhotoLocation();
				}
			}
		});
	}

	/**
	 * This method creates the temporary file where the camera will save the taken photo.
	 * It also creates the directory where the file will be kept, if it has not been
	 * created before.
	 */
	private void createTempPhotoFile() {
		File tempPhotosDir = new File(
				getExternalFilesDir(Environment.DIRECTORY_PICTURES), TEMP_PHOTOS_DIR_NAME);
		
		// If the directories do not exist, create it
		if (!tempPhotosDir.exists()) {
			if (!tempPhotosDir.mkdirs()) {
				Log.e(getClass().toString(), "Could not create temporary photo directory");
				Toast.makeText(this, "Could not create temporary photo directory", Toast.LENGTH_LONG).show();
				// "onBackPressed()" is used because it also deletes the temporary photo.
				onBackPressed();
			}
		}
		
		// Create a timestamp for the file name
		String timeStamp = new SimpleDateFormat(TIMESTAMP_FORMAT, Locale.US).format(new Date());
		
		tempPhotoFile = new File(tempPhotosDir.getPath() + File.separator + "TEMP_PHOTO_" + timeStamp + ".jpg");
	}
	
	/**
	 * This callback is overridden, because when this is run, we can get the widths
	 * and heights of layout views. This is needed, to know to which size we should
	 * sample the taken photo bitmap.
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		
		// This check is done because this shit does not really work with rotations
		if (photoHeight == 0) {
			// We need to get these here, since they will return 0 before
			// this method has been called
			photoWidth = photoView.getWidth();
			photoHeight = photoView.getHeight();
		}
		
		if (photoTaken) {
			showPhoto();
		}
		
		// The player should not be able to take a photo before we
		// can show the photo - which requires the photo view's width and height
		// to be done optimally.
		enablePhotoButton();
	}
	
	/**
	 * This method is run when the location client is connected.
	 */
	@Override
	public void onConnected(Bundle connectionHint) {
		connected = true;
		enablePhotoButton();
		// This is done here, but the photolocation will not really be
		// created if it does not have a name or a photo.
		createPhotoLocation();
	}
	
	private void enablePhotoButton() {
		// This check is needed, since the button should only be enabled when both
		// the onWindowFocusChanged callback and the onConnected callback have been run.
		if (photoHeight != 0 && connected) {
			takePhotoButton.setEnabled(true);
		}
	}

	/**
	 * This method creates gets the current location, displays it and
	 * creates a photolocation if a name is entered and a photo is taken.
	 */
	private void createPhotoLocation() {
		if (!connected) {
			return;
		}
		
		// Get the current location
		location = locationClient.getLastLocation();
		double latitude = location.getLatitude();
		double longitude = location.getLongitude();
		// Display the current location
		locationTextView.setText("Latitude: " + latitude + "\nLongitude: " + longitude);
		
		// Get the entered location name
		String locationName = locationNameEditView.getText().toString();
		
		// This is a pretty ugly way to fix the problem that the database
		// does not allow inserting strings with ' (maybe it does in some way, but no easy way)
		locationName = locationName.replaceAll("'", " ");
		locationName = locationName.replaceAll("\"", " ");
		
		// If the name is long enough and a photo is taken, create the photolocation
		// and enable the "add location" button.
		if (photoTaken && locationName.length() > 0) {
			photoLocation = new PhotoLocation(latitude,
					longitude, BitmapFactory.decodeFile(tempPhotoFile
							.getAbsolutePath()), locationName);
			
			addLocationButton.setEnabled(true);
		}
	}

	/**
	 * This method creates an intent for the camera to take a photo. The photo
	 * will be saved in a temporary file, which this activity later retrieves.
	 */
	private void takePhoto() {
		// Create an intent for taking a photo with a camera app
		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// Here we specify where the photo will be saved
		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempPhotoFile));
		
		startActivityForResult(cameraIntent, CAPTURE_IMAGE_REQUEST_CODE);
		
		// See callback "onActivityResult()" for what happens next
	}
	
	/**
	 * This is called as a callback when the user has taken a photograph (or canceled doing so).
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAPTURE_IMAGE_REQUEST_CODE) {
			// If a photo was taken
			if (resultCode == RESULT_OK) {
				Toast.makeText(this, "Photo captured!", Toast.LENGTH_SHORT).show();
				photoTaken = true;
				// We need to rotate the photo according to the orientation the device
				// was in when it was taken. We also sample the photo bitmap to
				// keep net transaction sizes down.
				rotateAndSamplePhoto();
				showPhoto();		
				// If the user canceled taking a photo
			} else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(this, "Photo canceled!", Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * This method uses another method in the BitmapHelper class to rotate and sample
	 * the photo. The photo needs to be rotated according to what orientation the device was
	 * in when it was taken, to be displayed correctly. The photo bitmap is sampled to keep 
	 * net transaction sizes down.
	 */
	private void rotateAndSamplePhoto() {
		// Rotate and sample the photo
		Bitmap photoBitmap = BitmapHelper.getSampledRotatedBitmap(
				tempPhotoFile.getAbsolutePath(), SAMPLE_PHOTO_WIDTH, SAMPLE_PHOTO_HEIGHT);
		// Save the rotated and sampled photo to the temporary file.
		// This way, the sampled and rotated photo will be sent to the server.
		BitmapHelper.saveBitmapToFile(tempPhotoFile, photoBitmap);
	}

	/**
	 * This method samples the taken photo according to screen size and displays it.
	 */
	private void showPhoto() {
		// This seems to be needed, since this method is somehow run when back is pressed.
		// This is probably because it is run in the onWindowFocusChanged() callback.
		if (!tempPhotoFile.exists()) {
			return;
		}
		
		// Here, the photo is sampled again - but now according to screen size. 
		// This is to keep the Android device's memory usage down.
		Bitmap photoBitmap = BitmapHelper.decodeSampledBitmapFromFile(
				tempPhotoFile.getAbsolutePath(), photoWidth, photoHeight);
		
		// Display the photo
		photoView.setImageBitmap(photoBitmap);
	}

	/**
	 * Called when the "take photo" button is clicked
	 */
	public void takePhotograph(View view) {
		takePhoto();
	}
	
	/**
	 * Called when the "add location" button is clicked
	 */
	public void gotoLocationAdded(View view) {
		addLocationButton.setEnabled(false);
		// Before going to the next activity, we need to send the photolocation
		// to the server.
		sendPhotoLocationToServer();
	}
	
	/** 
	 * This method connects to the photolocation server and starts a task
	 * to send the photo. If there was no internet connection, the user will be told so and
	 * the activity will stop.
	 */
	private void sendPhotoLocationToServer() {
		// Get the URL for the 'send' action
		String sendUrl = RestClient.API_URL;
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			new ConnectToWebTask().execute(sendUrl);
		} else {
			Toast.makeText(this, "No network connection available.", Toast.LENGTH_LONG).show();
			// "onBackPressed()" is used because it also deletes the temporary photo.
			onBackPressed();
		}
	}
	
	/**
	 * This class contains method for starting a asynchronized task
	 * that connects to our web service. Android needs this to be done
	 * in an asynchronized task.
	 * 
	 * @author sfabian
	 */
	private class ConnectToWebTask extends AsyncTask<String, Void, String> {
		/**
		 * This method sends the photolocation to the server.
		 */
		@Override
		protected String doInBackground(String... urls) {
			return RestClient.doSendToServer(urls[0],
					photoLocation.getLatitude(), photoLocation.getLongitude(),
					photoLocation.getBase64Photo(),
					photoLocation.getLocationName());
		}
		/**
		 * This callback is run when the server returns its result.
		 * In this, we get the ID for the photolocation, that will be used to
		 * save the photolocation in the local databases.
		 */
		@Override
		protected void onPostExecute(String result) {
			// We get the ID from the web server and sets it to the photolocation.
			// TODO: Here, error codes should be used. If the server returns something faulty,
			// we do not want the app to just crash! (well, someday I will fix this)
			photoLocation.setId(Integer.parseInt(result));
			
			
			DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
			// This is done, because otherwise we may get an error that the ID must be unique, if
			// the server's database table have been changed (e.g. something has been removed)
			databaseHelper.removeAllPhotoLocations();
			// We save the photolocation to the local database with the new ID.
			databaseHelper.addPhotoLocation(photoLocation, getApplicationContext());
			
			// This notes that this photo location was added by the user, so that
			// the user will not be able to "find" their own locations.
			databaseHelper.addAddedPhotoLocation(photoLocation.getId());
			
			// Finally, we can go to the LocationAddedActivity
			gotoNextActivity();
		}
	}
	
	/**
	 * Goes to the LocationAddedActivity.
	 */
	private void gotoNextActivity() {
		// The temporary photo file used by the camera is not needed anymore.
		tempPhotoFile.delete();
		Intent intent = new Intent(this, LocationAddedActivity.class);
		intent.putExtra(getString(R.string.intent_key_photo_location), photoLocation.getId());
		startActivity(intent);
	}
	
	@Override
	public void onBackPressed() {
		// This is because the file is only temporarily needed.
		tempPhotoFile.delete();
		super.onBackPressed();
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		// These are needed to keep track of if a photo is taken, and the photo if one was taken.
		savedInstanceState.putString(BUNDLE_PHOTO_PATH, tempPhotoFile.getAbsolutePath());
		savedInstanceState.putBoolean(BUNDLE_PHOTO_TAKEN, photoTaken);
		// It is actually pretty stupid to keep this, but for some reason it does not work
		// to get the height in onWindowFocusChanged when the device is in landscape orientation...
		savedInstanceState.putInt(BUNDLE_PHOTO_HEIGHT, photoHeight);
		super.onSaveInstanceState(savedInstanceState);
	}
}
