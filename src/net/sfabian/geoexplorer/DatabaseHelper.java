package net.sfabian.geoexplorer;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.BitmapFactory;

/**
 * This class contains methods to use the local database: methods like inserting in,
 * selecting from and deleting from tables. The databases are used to store photolocations
 * and data related to them.
 * 
 * @author sfabian
 */

public class DatabaseHelper extends SQLiteOpenHelper {
	
	private static final int DATABASE_VERSION = 2; 
	
	private static final String DATABASE_NAME = "GeoExplorer";
	
	// Table names
	// The first table keeps all local photolocations
	// The three others just keeps IDs of photolocations, to know if a user
	// has found photolocations, added them themself or reported some photolocations.
	private static final String PHOTOLOCATIONS_TABLE_NAME = "PhotoLocations";
	private static final String ADDED_PHOTOLOCATIONS_TABLE_NAME = "AddedPhotoLocations";
	private static final String FOUND_PHOTOLOCATIONS_TABLE_NAME = "FoundPhotoLocations";
	private static final String REPORTED_PHOTOLOCATIONS_TABLE_NAME = "ReportedPhotoLocations";
	
	// Common column name
	private static final String KEY_ID = "id";
	
	// PhotoLocations column names
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
	
	/**
	 * This creates the tables if they have not been created before.
	 */
	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(CREATE_PHOTOLOCATIONS_TABLE);
		database.execSQL(CREATE_ADDED_PHOTOLOCATIONS_TABLE);
		database.execSQL(CREATE_FOUND_PHOTOLOCATIONS_TABLE);
		database.execSQL(CREATE_REPORTED_PHOTOLOCATIONS_TABLE);
	}
	
	/**
	 * Drops all tables and adds them again, to be used on upgrade.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		database.execSQL(DROP_TABLES + PHOTOLOCATIONS_TABLE_NAME);
		database.execSQL(DROP_TABLES + ADDED_PHOTOLOCATIONS_TABLE_NAME);
		database.execSQL(DROP_TABLES + FOUND_PHOTOLOCATIONS_TABLE_NAME);
		database.execSQL(DROP_TABLES + REPORTED_PHOTOLOCATIONS_TABLE_NAME);
		
		onCreate(database);
	}
	
	/**
	 * Adds a photolocation to the local database.
	 * The context is needed for file operations (the photos are saved in files, not in the database).
	 * @param photoLocation PhotoLocation to add.
	 * @param context needed for file operations.
	 * @return if the photo was added.
	 */
	public boolean addPhotoLocation(PhotoLocation photoLocation, Context context) {
		// This is a failsafe - photoLocations can have ID -1. They will have ID -1
		// first when they are added by the user, until the server has returned a correct
		// ID. This should hopefully never be run, but is here to be safe.
		if (photoLocation.getId() == -1) {
			return false;
		}
		
		SQLiteDatabase database = getWritableDatabase();

		// Query to add the photolocation to the photolocation table.
		String sqlQuery = "INSERT INTO " + PHOTOLOCATIONS_TABLE_NAME + " ("
				+ KEY_ID + ", " + KEY_LATITUDE + ", " + KEY_LONGITUDE + ", "
				+ KEY_PHOTO_PATH + ", " + KEY_LOCATION_NAME + ") VALUES ('"
				+ photoLocation.getId() + "', '" + photoLocation.getLatitude()
				+ "', '" + photoLocation.getLongitude() + "', '"
				+ photoLocation.getPhotoFilePath(context) + "', '" + photoLocation.getLocationName() + "');";

		database.execSQL(sqlQuery);
		
		return true;
	}
	
	/**
	 * This method is used to add a photoLocation (or more correctly, its ID)
	 * to the table of found photolocations. This keeps track of those, so the user
	 * cannot find photoLocations multiple times.
	 * @param id
	 */
	public void addFoundPhotoLocation(int id) {		
		SQLiteDatabase database = getWritableDatabase();

		// Query to insert photolocation ID in the found photolocations table.
		String sqlQuery = "INSERT INTO " + FOUND_PHOTOLOCATIONS_TABLE_NAME + " ("
				+ KEY_ID + ") VALUES ('" + id + "');";
		
		database.execSQL(sqlQuery);
	}
	
	/**
	 * This method is used to add a photoLocation (or more correctly, its ID)
	 * to the table of photolocations added by the user. This keeps track of those, 
	 * so the user cannot find photoLocations they added themselves.
	 * @param id
	 */
	public void addAddedPhotoLocation(int id) {		
		SQLiteDatabase database = getWritableDatabase();

		// Query to insert photolocation ID in the added photolocations table.
		String sqlQuery = "INSERT INTO " + ADDED_PHOTOLOCATIONS_TABLE_NAME + " ("
				+ KEY_ID + ") VALUES ('" + id + "');";

		database.execSQL(sqlQuery);
	}
	
	/**
	 * This method is used to add a photoLocation (or more correctly, its ID)
	 * to the table of photolocations reported by the user. This keeps track of those, 
	 * so that they are not displayed for this user, because the user probably does not
	 * want to see photos that they reported.
	 * @param id
	 */
	public void addReportedPhotoLocation(int id) {		
		SQLiteDatabase database = getWritableDatabase();

		// Query to insert photolocation ID in the reported photolocations table.
		String sqlQuery = "INSERT INTO " + REPORTED_PHOTOLOCATIONS_TABLE_NAME + " ("
				+ KEY_ID + ") VALUES ('" + id + "');";
		
		database.execSQL(sqlQuery);
	}
	
	/**
	 * Get all downloaded photolocations, except the ones that the user has reported.
	 * Also notes if the photolocations have been found or added.
	 * @param reqWidth
	 * @param reqHeight
	 * @return all photolocations in the local database
	 */
	public ArrayList<PhotoLocation> getAllPhotoLocations(int reqWidth, int reqHeight) {
		SQLiteDatabase database = getReadableDatabase();

		// Query to get all photolocations in the photolocations table.
		String selectQuery = SELECT_ALL + PHOTOLOCATIONS_TABLE_NAME + " WHERE "
				+ KEY_ID + " NOT IN (" + "SELECT " + KEY_ID + " FROM "
				+ REPORTED_PHOTOLOCATIONS_TABLE_NAME + ");";
		Cursor cursor = database.rawQuery(selectQuery, null);
		
		ArrayList<PhotoLocation> photoLocations = new ArrayList<PhotoLocation>();
		
		// If the database cursor was succesfully loaded and 
		// there are any photolocations in the table
		if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
			// Keep iterating through all selected photolocations
			while (!cursor.isAfterLast()) {
				int id = cursor.getInt(cursor.getColumnIndex(KEY_ID));
				// See if the photolocation was already found or was added by this user
				boolean isFound = isPhotoLocationFound(id);
				boolean isAdded = isPhotoLocationAdded(id);
				
				// Create a PhotoLocation object from the photolocation in the table 
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
		
		if (cursor != null) {
			cursor.close();
		}
		
		return photoLocations;
	}
	
	/**
	 * Removes all photolocations from the local database, so the ones nearby
	 * can be loaded.
	 */
	public void removeAllPhotoLocations() {
		SQLiteDatabase database = getReadableDatabase();
		
		database.delete(PHOTOLOCATIONS_TABLE_NAME, null, null);	
	}

	/**
	 * Gets a photolocation with the given ID from the local database.
	 * @param photoLocationId
	 * @return null if no photoLocation with that id exists in the database
	 */
	public PhotoLocation getPhotoLocation(int photoLocationId) {
		SQLiteDatabase database = getReadableDatabase();
		
		// Query to select photolocation from photolocations table
		String selectQuery = SELECT_ALL + PHOTOLOCATIONS_TABLE_NAME + " WHERE "
				+ KEY_ID + " = " + photoLocationId;
		Cursor cursor = database.rawQuery(selectQuery, null);
		
		PhotoLocation photoLocation = null;
		// If the database cursor was correctly loaded and the select query returned anything
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			
			int id = cursor.getInt(cursor.getColumnIndex(KEY_ID));
			// See if the photolocation was already found or was added by this user
			boolean isFound = isPhotoLocationFound(id);
			boolean isAdded = isPhotoLocationAdded(id);
			
			// Create a PhotoLocation object from the photolocation in the table
			photoLocation = new PhotoLocation(id,
											cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE)),
											cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE)),
											BitmapFactory.decodeFile(
												cursor.getString(cursor.getColumnIndex(KEY_PHOTO_PATH))),
											cursor.getString(cursor.getColumnIndex(KEY_LOCATION_NAME)),
											isFound,
											isAdded);
		}
		
		if (cursor != null) {
			cursor.close();
		}
		
		return photoLocation;	
	}
	
	/**
	 * @param photoLocationId
	 * @return if the photolocation with given ID was already found
	 */
	public boolean isPhotoLocationFound(int photoLocationId) {
		return isPhotoLocationInTable(photoLocationId, FOUND_PHOTOLOCATIONS_TABLE_NAME);
	}
	
	/**
	 * @param photoLocationId
	 * @return if the photolocation with given ID was added by this user
	 */
	public boolean isPhotoLocationAdded(int photoLocationId) {
		return isPhotoLocationInTable(photoLocationId, ADDED_PHOTOLOCATIONS_TABLE_NAME);
	}
	
	/**
	 * @param photoLocationId
	 * @param tableName
	 * @return if the photolocation with the given ID is in the given table
	 */
	private boolean isPhotoLocationInTable(int photoLocationId, String tableName) {
		SQLiteDatabase database = getReadableDatabase();
		
		// Query to select photolocation with given ID from given table
		String selectQuery = SELECT_ALL + tableName + " WHERE "
				+ KEY_ID + " = " + photoLocationId;
		Cursor cursor = database.rawQuery(selectQuery, null);
		
		// Will be true if there is a location with this ID in this table
		boolean isFound = (cursor != null && cursor.getCount() > 0);
		
		if (cursor != null) {
			cursor.close();	
		}
		
		return isFound;	
	}

	/**
	 * Adds photolocations to the local databse from the given JSON string. Is used 
	 * with photolocations loaded from the server, since they come in a JSON string.
	 * @param jsonPhotoLocations
	 * @param context
	 * @return if there were any photolocations in the json
	 */
	public boolean addPhotoLocationsFromJson(String jsonPhotoLocations, Context context) {
		try {
			// Create the JSON array with photolocations from the given string
			JSONArray jsonArray = new JSONArray(jsonPhotoLocations);
			
			if (jsonArray.length() == 0) {
				return false;
			}
			
			// If there are any photolocations in the JSON array, loop through them
			for (int i = 0; i < jsonArray.length(); i++) {
				// A JSONObject row contains the info for a photolocation
				JSONObject row = jsonArray.getJSONObject(i);
				// Create the PhotoLocation from the given JSON row
				PhotoLocation photoLocation = new PhotoLocation(row.getInt(RestClient.API_ID),
																row.getDouble(RestClient.API_LATITUDE),
																row.getDouble(RestClient.API_LONGITUDE),
																row.getString(RestClient.API_PHOTO),
																row.getString(RestClient.API_NAME));
				// Add the photolocation to the local database
				addPhotoLocation(photoLocation, context);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}
}
