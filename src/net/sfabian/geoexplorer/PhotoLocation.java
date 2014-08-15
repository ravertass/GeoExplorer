package net.sfabian.geoexplorer;

import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.google.android.gms.location.Geofence;

public class PhotoLocation {

	private int id;
	private double latitude;
	private double longitude;
	private Bitmap photo;
	
	// TODO: De här siffrorna kan behöva optimeras
	private final static float closeRadius = 500; // in metres
	private final static float thereRadius = 50; // in metres
	// TODO: Den här siffran är ganska slumpvald
	// This is fow how long the geofence will be active
	private final static int geofenceExpirationDuration = 10000000; // in seconds (TODO: tror jag)
	private final static int geofenceTransitionTypes = 
			Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT;
	
	public PhotoLocation(int id, double latitude, double longitude, Bitmap photo) {
		this.id = id;
		this.latitude = latitude;
		this.longitude = longitude;
		this.photo = photo;
	}
	
	public PhotoLocation(int id, double latitude, double longitude, String base64Photo) {
		this(id, latitude, longitude, base64ToBitmap(base64Photo));
	}
	
	public int getId() {
		return id;
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
		return toGeofence(closeRadius);
	}
	
	public Geofence toThereGeofence() {
		return toGeofence(thereRadius);
	}
	
	private Geofence toGeofence(float radius) {
		return new Geofence.Builder()
				.setRequestId(Integer.toString(id))
				.setTransitionTypes(geofenceTransitionTypes)
				.setCircularRegion(latitude, longitude, radius)
				.setExpirationDuration(geofenceExpirationDuration)
				.build();
	}
}
