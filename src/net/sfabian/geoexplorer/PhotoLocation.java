package net.sfabian.geoexplorer;

import java.io.ByteArrayOutputStream;
import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.location.Geofence;

public class PhotoLocation {

	// Kommentera (och egentligen: refaktorera...)
	private int todo;
	
	private int id;
	private double latitude;
	private double longitude;
	private Bitmap photo;
	private String locationName;
	private boolean addedByUser;
	private boolean found;
	
	// TODO: De här siffrorna kan behöva optimeras
	private final static float closeRadius = 500; // in metres
	private final static float thereRadius = 50; // in metres
	// TODO: Den här siffran är ganska slumpvald
	// This is fow how long the geofence will be active
	private final static int geofenceExpirationDuration = 10000000; // in milliseconds (TODO: välj mer medvetet)
	private final static int geofenceTransitionTypes = 
			Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT;
	private static final String KEY_LATITUDE = "key_latitude";
	private static final String KEY_LONGITUDE = "key_longitude";
	private static final String KEY_PHOTO = "key_photo";
	private static final String SAVED_PHOTOS_DIR_NAME = "saved_photos";
	
	public static final String GEOFENCE_THERE = "there_";
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
	
	public PhotoLocation(double latitude, double longitude, Bitmap photo, String locationName) {
		this(-1, latitude, longitude, photo, locationName, false, false);
	}
	
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
	 * TODO
	 * @param base64Photo
	 * @return
	 */
	private static Bitmap base64ToBitmap(String base64Photo) {
		byte[] bytes = Base64.decode(base64Photo, Base64.DEFAULT);
		
		Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
		return bitmap;
	}
	
	/**
	 * TODO
	 * @return
	 */
	public String getBase64Photo() {
		return bitmapToBase64(photo);
	}
	
	private String bitmapToBase64(Bitmap photo) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		byte[] bytes = baos.toByteArray();
		
		String encodedImage = Base64.encodeToString(bytes, Base64.DEFAULT);
		return encodedImage;
	}

	public Geofence toCloseGeofence() {
		return toGeofence(closeRadius, GEOFENCE_CLOSE);
	}
	
	public Geofence toThereGeofence() {
		return toGeofence(thereRadius, GEOFENCE_THERE);
	}
	
	private Geofence toGeofence(float radius, String idPrefix) {
		String geofenceId = idPrefix + id;
		return new Geofence.Builder()
				.setRequestId(geofenceId)
				.setTransitionTypes(geofenceTransitionTypes)
				.setCircularRegion(latitude, longitude, radius)
				.setExpirationDuration(geofenceExpirationDuration)
				.build();
	}
	
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		
		try {
			json.put(KEY_LATITUDE, latitude);
			json.put(KEY_LONGITUDE, longitude);
			json.put(KEY_PHOTO, bitmapToBase64(photo));
		} catch (JSONException e) {
			Log.e(getClass().toString(), e.toString());
		}
		
		return json;
	}
	
	/**
	 * 
	 * @param context use getApplicationContext()
	 * @return
	 */
	public String getPhotoFilePath(Context context) {
		File tempPhotosDir = new File(
				context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), SAVED_PHOTOS_DIR_NAME);
		
		// If the directories do not exist, create it
		if (!tempPhotosDir.exists()) {
			if (!tempPhotosDir.mkdirs()) {
				// TODO Hantera detta bättre
				Log.e(getClass().toString(), "Could not create saved photo directory");
			}
		}
		
		File savedPhotoFile = new File(tempPhotosDir.getPath() + File.separator + "IMG_" + id + ".jpg");
		// If the file does not exist, we need to create it
		if (!savedPhotoFile.exists()) {
			BitmapHelper.saveBitmapToFile(savedPhotoFile, photo);
		}
		
		return savedPhotoFile.getAbsolutePath();
	}
}
