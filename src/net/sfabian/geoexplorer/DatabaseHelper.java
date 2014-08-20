package net.sfabian.geoexplorer;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.BitmapFactory;
import android.util.Log;

// TODO: Det är lite otydlig hur den här kommer att bli.

public class DatabaseHelper extends SQLiteOpenHelper {
	
	private static final int DATABASE_VERSION = 2; //TODO fett oklart vad denna siffra är
	
	private static final String DATABASE_NAME = "GeoExplorer";
	
	// Tables
	private static final String PHOTOLOCATIONS_TABLE_NAME = "PhotoLocations";
	private static final String ADDED_PHOTOLOCATIONS_TABLE_NAME = "AddedPhotoLocations";
	private static final String FOUND_PHOTOLOCATIONS_TABLE_NAME = "FoundPhotoLocations";
	private static final String REPORTED_PHOTOLOCATIONS_TABLE_NAME = "ReportedPhotoLocations";
	
	// Common column name
	private static final String KEY_ID = "id";
	
	// photoLocations column names
	public static final String KEY_LATITUDE = "latitude";
	public static final String KEY_LONGITUDE = "longitude";
	public static final String KEY_PHOTO_PATH = "photo_path";
	public static final String KEY_LOCATION_NAME = "location_name";
	
	// Table create statements
	private static final String CREATE_PHOTOLOCATIONS_TABLE =
			"CREATE TABLE " + PHOTOLOCATIONS_TABLE_NAME + "(" +
			KEY_ID + " INTEGER PRIMARY KEY," +
			KEY_LATITUDE + " DOUBLE," +
			KEY_LONGITUDE + " DOUBLE," +
			KEY_PHOTO_PATH + " TEXT," +
			KEY_LOCATION_NAME + " TEXT)";
	private static final String CREATE_ADDED_PHOTOLOCATIONS_TABLE =
			"CREATE TABLE " + ADDED_PHOTOLOCATIONS_TABLE_NAME + "(" +
			KEY_ID + " INTEGER PRIMARY KEY)";
	private static final String CREATE_FOUND_PHOTOLOCATIONS_TABLE =
			"CREATE TABLE " + FOUND_PHOTOLOCATIONS_TABLE_NAME + "(" +
			KEY_ID + " INTEGER PRIMARY KEY)";
	private static final String CREATE_REPORTED_PHOTOLOCATIONS_TABLE =
			"CREATE TABLE " + REPORTED_PHOTOLOCATIONS_TABLE_NAME + "(" +
			KEY_ID + " INTEGER PRIMARY KEY)";
	
	private static final String DROP_TABLES = "DROP TABLES IF EXISTS ";
	
	private static final String SELECT_ALL = "SELECT * FROM ";
	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(CREATE_PHOTOLOCATIONS_TABLE);
		database.execSQL(CREATE_ADDED_PHOTOLOCATIONS_TABLE);
		database.execSQL(CREATE_FOUND_PHOTOLOCATIONS_TABLE);
		database.execSQL(CREATE_REPORTED_PHOTOLOCATIONS_TABLE);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		database.execSQL(DROP_TABLES + PHOTOLOCATIONS_TABLE_NAME);
		database.execSQL(DROP_TABLES + ADDED_PHOTOLOCATIONS_TABLE_NAME);
		database.execSQL(DROP_TABLES + FOUND_PHOTOLOCATIONS_TABLE_NAME);
		database.execSQL(DROP_TABLES + REPORTED_PHOTOLOCATIONS_TABLE_NAME);
		
		onCreate(database);
	}
	
	public boolean addPhotoLocation(PhotoLocation photoLocation, Context context) {
		if (photoLocation.getId() == -1) {
			return false;
		}
		
		SQLiteDatabase database = getWritableDatabase();

		String sqlQuery = "INSERT INTO " + PHOTOLOCATIONS_TABLE_NAME + " ("
				+ KEY_ID + ", " + KEY_LATITUDE + ", " + KEY_LONGITUDE + ", "
				+ KEY_PHOTO_PATH + ", " + KEY_LOCATION_NAME + ") VALUES ('"
				+ photoLocation.getId() + "', '" + photoLocation.getLatitude()
				+ "', '" + photoLocation.getLongitude() + "', '"
				+ photoLocation.getPhotoFilePath(context) + "', '" + photoLocation.getLocationName() + "');";
		Log.d(getClass().toString(), "query: "+sqlQuery);
		database.execSQL(sqlQuery);
		
		return true;
	}
	
	public void addFoundPhotoLocation(int id) {		
		SQLiteDatabase database = getWritableDatabase();

		String sqlQuery = "INSERT INTO " + FOUND_PHOTOLOCATIONS_TABLE_NAME + " ("
				+ KEY_ID + ") VALUES ('" + id + "');";
		Log.d(getClass().toString(), "query: "+sqlQuery);
		database.execSQL(sqlQuery);
	}
	
	public void addAddedPhotoLocation(int id) {		
		SQLiteDatabase database = getWritableDatabase();

		String sqlQuery = "INSERT INTO " + ADDED_PHOTOLOCATIONS_TABLE_NAME + " ("
				+ KEY_ID + ") VALUES ('" + id + "');";
		Log.d(getClass().toString(), "query: "+sqlQuery);
		database.execSQL(sqlQuery);
	}
	
	public void addReportedPhotoLocation(int id) {		
		SQLiteDatabase database = getWritableDatabase();

		String sqlQuery = "INSERT INTO " + REPORTED_PHOTOLOCATIONS_TABLE_NAME + " ("
				+ KEY_ID + ") VALUES ('" + id + "');";
		Log.d(getClass().toString(), "query: "+sqlQuery);
		database.execSQL(sqlQuery);
	}
	
	/**
	 * Get all downloaded photolocations, except the ones that the user has reported.
	 * Also notes if the photolocations have been found or added.
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public ArrayList<PhotoLocation> getAllPhotoLocations(int reqWidth, int reqHeight) {
		SQLiteDatabase database = getReadableDatabase();

		String selectQuery = SELECT_ALL + PHOTOLOCATIONS_TABLE_NAME + " WHERE "
				+ KEY_ID + " NOT IN (" + "SELECT " + KEY_ID + " FROM "
				+ REPORTED_PHOTOLOCATIONS_TABLE_NAME + ");";
		Cursor cursor = database.rawQuery(selectQuery, null);
		
		ArrayList<PhotoLocation> photoLocations = new ArrayList<PhotoLocation>();
		
		if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
			while (!cursor.isAfterLast()) {
				int id = cursor.getInt(cursor.getColumnIndex(KEY_ID));
				boolean isFound = isPhotoLocationFound(id);
				boolean isAdded = isPhotoLocationAdded(id);
				
				PhotoLocation photoLocation = new PhotoLocation(
						id,
						cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE)),
						cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE)),
						BitmapHelper.decodeSampledBitmapFromFile(cursor.getString(cursor
								.getColumnIndex(KEY_PHOTO_PATH)), reqWidth, reqHeight),
						cursor.getString(cursor
								.getColumnIndex(KEY_LOCATION_NAME)),
						isFound,
						isAdded);
				photoLocations.add(photoLocation);
				cursor.moveToNext();
			}
		}
		
		cursor.close();
		return photoLocations;
	}
	
	public void removeAllPhotoLocations() {
		SQLiteDatabase database = getReadableDatabase();
		
		database.delete(PHOTOLOCATIONS_TABLE_NAME, null, null);	
	}

	/**
	 * @param photoLocationId
	 * @return null if no photoLocation with that id exists in the database
	 */
	public PhotoLocation getPhotoLocation(int photoLocationId) {
		SQLiteDatabase database = getReadableDatabase();
		
		String selectQuery = SELECT_ALL + PHOTOLOCATIONS_TABLE_NAME + " WHERE "
				+ KEY_ID + " = " + photoLocationId;
		Cursor cursor = database.rawQuery(selectQuery, null);
		
		
		
		PhotoLocation photoLocation = null;
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			
			int id = cursor.getInt(cursor.getColumnIndex(KEY_ID));
			boolean isFound = isPhotoLocationFound(id);
			boolean isAdded = isPhotoLocationAdded(id);
			
			photoLocation = new PhotoLocation(id,
											cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE)),
											cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE)),
											BitmapFactory.decodeFile(
												cursor.getString(cursor.getColumnIndex(KEY_PHOTO_PATH))),
											cursor.getString(cursor.getColumnIndex(KEY_LOCATION_NAME)),
											isFound,
											isAdded);
		}
		
		cursor.close();
		return photoLocation;	
	}
	
	private boolean isPhotoLocationFound(int photoLocationId) {
		return isPhotoLocationInTable(photoLocationId, FOUND_PHOTOLOCATIONS_TABLE_NAME);
	}
	
	private boolean isPhotoLocationAdded(int photoLocationId) {
		return isPhotoLocationInTable(photoLocationId, ADDED_PHOTOLOCATIONS_TABLE_NAME);
	}
	
	private boolean isPhotoLocationInTable(int photoLocationId, String tableName) {
		SQLiteDatabase database = getReadableDatabase();
		
		String selectQuery = SELECT_ALL + tableName + " WHERE "
				+ KEY_ID + " = " + photoLocationId;
		Cursor cursor = database.rawQuery(selectQuery, null);
		
		boolean isFound = (cursor != null && cursor.getCount() > 0);
		
		cursor.close();
		
		return isFound;	
	}

	// If this works life is magic
	public void addPhotoLocationsFromServer(String jsonPhotoLocations, Context context) {
		try {
			JSONArray jsonArray = new JSONArray(jsonPhotoLocations);
			
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject row = jsonArray.getJSONObject(i);
				PhotoLocation photoLocation = new PhotoLocation(row.getInt(RestClient.API_ID),
																row.getDouble(RestClient.API_LATITUDE),
																row.getDouble(RestClient.API_LONGITUDE),
																row.getString(RestClient.API_PHOTO),
																row.getString(RestClient.API_NAME));
				addPhotoLocation(photoLocation, context);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
