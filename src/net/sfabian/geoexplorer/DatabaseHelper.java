package net.sfabian.geoexplorer;

import java.io.File;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.BitmapFactory;
import android.util.Log;

// TODO: Det är lite otydlig hur den här kommer att bli.

public class DatabaseHelper extends SQLiteOpenHelper {
	
	private static final int DATABASE_VERSION = 2; //TODO fett oklart vad denna siffra är
	
	private static final String DATABASE_NAME = "geoExplorer";
	
	// Tables
	private static final String PHOTOLOCATIONS_TABLE_NAME = "photoLocations";
	private static final String ADDED_PHOTOLOCATIONS_TABLE_NAME = "addedPhotoLocations";
	
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
	
	private static final String DROP_TABLES = "DROP TABLES IF EXISTS ";
	
	private static final String SELECT_ALL ="SELECT * FROM ";
	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(CREATE_PHOTOLOCATIONS_TABLE);
		database.execSQL(CREATE_ADDED_PHOTOLOCATIONS_TABLE);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		database.execSQL(DROP_TABLES + PHOTOLOCATIONS_TABLE_NAME);
		database.execSQL(DROP_TABLES + ADDED_PHOTOLOCATIONS_TABLE_NAME);
		
		onCreate(database);
	}
	
	public boolean addPhotoLocation(PhotoLocation photoLocation, Context context) {
		if (photoLocation.getId() == -1) {
			return false;
		}
		
		SQLiteDatabase database = getWritableDatabase();
		
//		ContentValues values = new ContentValues();
//		values.put(KEY_ID, photoLocation.getId());
//		values.put(KEY_LATITUDE, photoLocation.getLatitude());
//		values.put(KEY_LONGITUDE, photoLocation.getLongitude());
//		values.put(KEY_PHOTO, photoLocation.getBase64Photo());
		
		String sqlQuery = "INSERT INTO " + PHOTOLOCATIONS_TABLE_NAME + " ("
				+ KEY_ID + ", " + KEY_LATITUDE + ", " + KEY_LONGITUDE + ", "
				+ KEY_PHOTO_PATH + ", " + KEY_LOCATION_NAME + ") VALUES ('" + photoLocation.getId() + "', '" + photoLocation.getLatitude()
				+ "', '" + photoLocation.getLongitude() + "', '" + photoLocation.getPhotoFilePath(context) + "', '" + photoLocation.getLocationName() + "');";
		Log.d(getClass().toString(), "query: "+sqlQuery);
		database.execSQL(sqlQuery);

//		long id = database.insert(PHOTOLOCATIONS_TABLE_NAME, null, values);
		
		return true;
	}
	
	// TODO: Känns sjukt oklart om den här fungerar
	public ArrayList<PhotoLocation> getAllPhotoLocations() {
		SQLiteDatabase database = getReadableDatabase();
		
		String selectQuery = SELECT_ALL + PHOTOLOCATIONS_TABLE_NAME;
		Cursor cursor = database.rawQuery(selectQuery, null);
		
		ArrayList<PhotoLocation> photoLocations = new ArrayList<PhotoLocation>();
		
		if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
			while (!cursor.isAfterLast()) {
				PhotoLocation photoLocation = new PhotoLocation(
						cursor.getInt(cursor.getColumnIndex(KEY_ID)),
						cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE)),
						cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE)),
						BitmapFactory.decodeFile(cursor.getString(cursor
								.getColumnIndex(KEY_PHOTO_PATH))),
						cursor.getString(cursor
								.getColumnIndex(KEY_LOCATION_NAME)));
				photoLocations.add(photoLocation);
				cursor.moveToNext();
			}
		}
		
		cursor.close();
		return photoLocations;
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
			photoLocation = new PhotoLocation(cursor.getInt(cursor.getColumnIndex(KEY_ID)),
											cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE)),
											cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE)),
											BitmapFactory.decodeFile(
												cursor.getString(cursor.getColumnIndex(KEY_PHOTO_PATH))),
											cursor.getString(cursor.getColumnIndex(KEY_LOCATION_NAME)));
		}
		
		cursor.close();
		return photoLocation;	
	}
}
