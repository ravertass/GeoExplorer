package net.sfabian.geoexplorer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
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

public class AddLocationActivity extends AbstractPlayServicesActivity {

	private Button addLocationButton;
	private ImageView photoView;
	private File tempPhotoFile;
	private Button takePhotoButton;
	private TextView locationTextView;
	private EditText locationNameEditView;
	
	private int photoWidth = 0;
	private int photoHeight = 0;
	private boolean connected = false;
	private boolean photoTaken = false;
	private Location location;
	private PhotoLocation photoLocation;
	
	private static final String TEMP_PHOTOS_DIR_NAME = "temp_photos";
	private static final String TIMESTAMP_FORMAT = "yyyyMMdd_HHmmss"; 
	private static final int CAPTURE_IMAGE_REQUEST_CODE = 100; //TODO: Minns ej vad syftet med denna var
	private static final String BUNDLE_PHOTO_PATH = "photo_path";
	private static final String BUNDLE_PHOTO_TAKEN = "photo_taken";
	private static final int SAMPLE_PHOTO_WIDTH = 1000; //TODO lite godtycklig siffra
	private static final int SAMPLE_PHOTO_HEIGHT = 0; //TODO genom att låta den här vara 0 så löser det sig bra :P
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO: Här ska vi kolla om AddLocation har körts förut, det kommer att sparas i SharedPref
		if (false) {
			Intent intent = new Intent(this, TermsActivity.class);
			startActivity(intent);
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_location);
		
		locationNameEditView = (EditText) findViewById(R.id.add_location_edit_name);
		addEditTextListener();
		photoView = (ImageView) findViewById(R.id.add_location_photo);
		locationTextView = (TextView) findViewById(R.id.add_location_location_text);
		takePhotoButton = (Button) findViewById(R.id.add_location_take_photo_button);
		addLocationButton = (Button) findViewById(R.id.add_location_add_location_button);

		if (savedInstanceState != null) {
			// If the device is rotated, the photo should be kept
			tempPhotoFile = new File(savedInstanceState.getString(BUNDLE_PHOTO_PATH));
			photoTaken = savedInstanceState.getBoolean(BUNDLE_PHOTO_TAKEN);
			// See callback "onWindowFocusChanged()" for what happens next
		} else {
			// Create the file where the photo will be temporarily saved
			createTempPhotoFile();
		}
		
		// See callback "onConnected()" for what happens next
	}
	
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
				createPhotoLocation();
			}
		});
	}

	/**
	 * This method gets the directory on the device where the camera will temporarily save
	 * taken photos. This is stored in SharedPreferences. If this directory has not been
	 * created, this method will create it.
	 */
	private void createTempPhotoFile() {
		File tempPhotosDir = new File(
				getExternalFilesDir(Environment.DIRECTORY_PICTURES), TEMP_PHOTOS_DIR_NAME);
		
		// If the directories do not exist, create it
		if (!tempPhotosDir.exists()) {
			if (!tempPhotosDir.mkdirs()) {
				// TODO Hantera detta bättre
				Log.e(getClass().toString(), "Could not create temporary photo directory");
			}
		}
		
		// Create a timestamp for the file name
		String timeStamp = new SimpleDateFormat(TIMESTAMP_FORMAT, Locale.US).format(new Date());
		
		tempPhotoFile = new File(tempPhotosDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		
		// We need to get these here, since they will return 0 before
		// this method has been called
		photoWidth = photoView.getWidth();
		photoHeight = photoView.getHeight();
		
		if (photoTaken) {
			showPhoto();
		}
		
		enablePhotoButton();
	}
	
	private void enablePhotoButton() {
		// This check is needed, since the button should only be enabled when both
		// the onWindowFocusChanged callback and the onConnected callback have been run.
		// I don't think I can guarantee that one is run before the other.
		if (photoHeight != 0 && connected) {
			takePhotoButton.setEnabled(true);
		}
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
		// TODO lägg till locationen här...
		JSONObject json = photoLocation.toJson();
		// TODO send json to server
		// TODO wait for OK and row ID back
		// TODO add photoLocation to database
		// TODO Tills vidare görs detta bara lokalt och vi sätter ett bullshit-ID på objektet
		photoLocation.setId(new Random().nextInt(99999999));
		
		DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
		databaseHelper.addPhotoLocation(photoLocation, getApplicationContext());
		
		Intent intent = new Intent(this, LocationAddedActivity.class);
		intent.putExtra(getString(R.string.intent_key_photo_location), photoLocation.getId());
		startActivity(intent);
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		connected = true;
		enablePhotoButton();
		createPhotoLocation();
	}

	private void createPhotoLocation() {
		if (!connected) {
			return;
		}
		
		// Get the current location
		location = locationClient.getLastLocation();
		double latitude = location.getLatitude();
		double longitude = location.getLongitude();
		locationTextView.setText("Latitude: " + latitude + "\nLongitude: " + longitude);
		
		String locationName = locationNameEditView.getText().toString();
		if (photoTaken && locationName.length() > 0) {
			photoLocation = new PhotoLocation(latitude,
					longitude, BitmapFactory.decodeFile(tempPhotoFile
							.getAbsolutePath()), locationName);
			
			// If the photo is taken, a name is given and we have the location,
			// the user should be able to add the photo-location
			addLocationButton.setEnabled(true);
		}
	}

	private void takePhoto() {
		// Create an intent for taking a photo with a camera app
		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// Here we specify where the photo will be saved
		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempPhotoFile));
		
		startActivityForResult(cameraIntent, CAPTURE_IMAGE_REQUEST_CODE);
		
		// See callback "onActivityResult()" for what happens next
	}
	
	/**
	 * This is called as a callback when the user has taken a photograph (or canceled doing so) 
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAPTURE_IMAGE_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				// TODO gör något åt utkommenterad kod
//				Toast.makeText(this, R.string.toast_photo_captured, Toast.LENGTH_SHORT).show();
				photoTaken = true;
				rotateAndSamplePhoto();
				showPhoto();			
			} else if (resultCode == RESULT_CANCELED) {
//				Toast.makeText(this, R.string.toast_photo_canceled, Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void rotateAndSamplePhoto() {
		// Rotate and sample the photo
		Bitmap photoBitmap = BitmapHelper.getDecodedRotatedBitmap(
				tempPhotoFile.getAbsolutePath(), SAMPLE_PHOTO_WIDTH, SAMPLE_PHOTO_HEIGHT);
		// Save the rotated and sampled photo to the temp file.
		// This way, a sampled photo will be sent to the server.
		BitmapHelper.saveBitmapToFile(tempPhotoFile, photoBitmap);
	}

	// TODO: Döp om den här metoden, den gör lite mer
	// eller refaktorera om osv.
	private void showPhoto() {
		// This seems to be needed, since this method is somehow run when back is pressed
		// This is probably because it is run in the onWindowFocusChanged() callback
		if (!tempPhotoFile.exists()) {
			return;
		}
		
		// Sample the bitmap to the needed size
		Bitmap photoBitmap = BitmapHelper.decodeSampledBitmapFromFile(
				tempPhotoFile.getAbsolutePath(), photoWidth, photoHeight);
		
		// Display the photo
		photoView.setImageBitmap(photoBitmap);
	}
	
	@Override
	public void onBackPressed() {
		// This is because the file is only temporarily needed
		tempPhotoFile.delete();
		super.onBackPressed();
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString(BUNDLE_PHOTO_PATH, tempPhotoFile.getAbsolutePath());
		savedInstanceState.putBoolean(BUNDLE_PHOTO_TAKEN, photoTaken);
		super.onSaveInstanceState(savedInstanceState);
	}
}
