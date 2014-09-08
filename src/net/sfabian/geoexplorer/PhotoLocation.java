package net.sfabian.geoexplorer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.location.Geofence;

/**
 * This class models a 'photolocation', which is very central to this app.
 * The photolocation contains a location (latitude and longitude), a photograph
 * and a name for the location. It also contains an ID (the ID of the photolocation
 * in both the server's and the local databases) and booleans that determine if the
 * photolocation has already been found and if it was added by the user. 
 * 
 * This class should probably actually be refactored. It is used both for photolocations
 * loaded from the database and for newly added photolocations that do not have an ID.
 * 
 * @author sfabian
 */

public class PhotoLocation {
	
	// The ID of this photolocation in both the local and the server's databases
	private int id;
	// The location of this photolocation
	private double latitude;
	private double longitude;
	// A bitmap of the photo of this photolocatoin
	private Bitmap photo;
	// The name of this photolocation
	private String locationName;
	// If this photolocation was added by this user
	private boolean addedByUser;
	// If this photolocation has been found by this user
	private boolean found;
	
	// The radius in meters that determines if a user is close to the photolocation.
	private final static float closeRadius = 1000;
	// The radius in meters that determines if the user is at the photolocation.
	private final static float thereRadius = 100; 

	// This is for how long a geofence of this photolocation will be active
	// It is chosen kind of randomly, I must admit
	private final static int geofenceExpirationDuration = 10000000; // in milliseconds
	// These are the geofence transition types that are relevant for photolocations
	private final static int geofenceTransitionTypes = 
			Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT;
	
	// The prefix of a geofence ID for a THERE geofence
	public static final String GEOFENCE_THERE = "there_";
	// The prefix of a geofence ID for a CLOSE geofence
	public static final String GEOFENCE_CLOSE = "close_";
	
	public PhotoLocation(int id, double latitude, double longitude,
			Bitmap photo, String locationName, boolean found,
			boolean addedByUser) {
		this.id = id;
		this.latitude = latitude;
		this.longitude = longitude;
		this.photo = photo;
		this.locationName = locationName;
		this.addedByUser = addedByUser;
		this.found = found;
	}
	
	/**
	 * This constructor is used when adding a new photo location, and it yet
	 * has not retrieved an ID from the server.
	 */
	public PhotoLocation(double latitude, double longitude, Bitmap photo, String locationName) {
		this(-1, latitude, longitude, photo, locationName, false, false);
	}
	
	/**
	 * This is used when photolocations are created from JSON and then added to the database.
	 * This is because JSONObject is taken from the server, and the server does not keep track
	 * of whether users have found photolocations or what users have added them.
	 */
	public PhotoLocation(int id, double latitude, double longitude, String base64Photo, String locationName) {
		this(id, latitude, longitude, base64ToBitmap(base64Photo), locationName, false, false);
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public double getLatitude() {
		return latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	public Bitmap getPhoto() {
		return photo;
	}
	
	public String getLocationName() {
		return locationName;
	}
	
	public boolean getAddedByUser() {
		return addedByUser;
	}
	
	public boolean getFound() {
		return found;
	}
	
	/**
	 * This decode a bitmap saved as a base64 string back to a bitmap.
	 * This is needed to retrieve photos from the server.
	 * @param base64Photo bitmap encoded in base64
	 * @return the decoded bitmap
	 */
	private static Bitmap base64ToBitmap(String base64Photo) {
		byte[] bytes = Base64.decode(base64Photo, Base64.DEFAULT);
		
		Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
		return bitmap;
	}
	
	/**
	 * @return the photolocation's photo a a base64 string
	 */
	public String getBase64Photo() {
		return bitmapToBase64(photo);
	}
	
	/**
	 * This encodes a bitmap to a base64 string.
	 * This is needed to be able to send photos to the server.
	 * @param photo
	 * @return
	 */
	private String bitmapToBase64(Bitmap photo) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		byte[] bytes = baos.toByteArray();
		
		String encodedImage = Base64.encodeToString(bytes, Base64.DEFAULT);
		return encodedImage;
	}

	/**
	 * @return a CLOSE geofence of this photolocation
	 */
	public Geofence toCloseGeofence() {
		return toGeofence(closeRadius, GEOFENCE_CLOSE);
	}
	
	/**
	 * @return a THERE geofence of this photolocation
	 */
	public Geofence toThereGeofence() {
		return toGeofence(thereRadius, GEOFENCE_THERE);
	}
	
	/**
	 * Creates a geofence of his photolocation.
	 * @param radius the radius of the geofence
	 * @param idPrefix what prefix the geofence ID should have
	 * @return
	 */
	private Geofence toGeofence(float radius, String idPrefix) {
		String geofenceId = idPrefix + id;
		return new Geofence.Builder()
				.setRequestId(geofenceId)
				.setTransitionTypes(geofenceTransitionTypes)
				.setCircularRegion(latitude, longitude, radius)
				.setExpirationDuration(geofenceExpirationDuration)
				.build();
	}
	
	/**
	 * This creates a file for the photo and returns its path. 
	 * @param context use getApplicationContext()
	 * @return
	 */
	@SuppressWarnings("static-access")
	public String getPhotoFilePath(Context context) {

		// Create a file path for the file
		String filePath = "IMG_" + id + ".jpg";

		// Create a file output stream and write the bitmap photo to it
		FileOutputStream fos;
		try {
			fos = context.openFileOutput(filePath, context.MODE_PRIVATE);
			photo.compress(Bitmap.CompressFormat.JPEG, 90, fos);
		} catch (FileNotFoundException e) {
			Log.e(getClass().toString(), "Could not save photo to file");
		}
	
		String absolutePath = context.getFilesDir() + File.separator + filePath;
		
		return absolutePath;
	}
}
