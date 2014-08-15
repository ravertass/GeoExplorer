package net.sfabian.geoexplorer;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
	private static final String KEY_LATITUDE = "latitude";
	private static final String KEY_LONGITUDE = "longitude";
	private static final String KEY_PHOTO = "photo";
	
	// Table create statements
	private static final String CREATE_PHOTOLOCATIONS_TABLE =
			"CREATE TABLE " + PHOTOLOCATIONS_TABLE_NAME + "(" +
			KEY_ID + " INTEGER PRIMARY KEY," +
			KEY_LATITUDE + " DOUBLE," +
			KEY_LONGITUDE + " DOUBLE," +
			KEY_PHOTO + " TEXT)";
	
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
	
	public void addPhotoLocation(PhotoLocation photoLocation) {
		SQLiteDatabase database = getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(KEY_ID, photoLocation.getId());
		values.put(KEY_LATITUDE, photoLocation.getLatitude());
		values.put(KEY_LONGITUDE, photoLocation.getLongitude());
		values.put(KEY_PHOTO, photoLocation.getBase64Photo());
		
		database.insert(PHOTOLOCATIONS_TABLE_NAME, null, values);
	}
	
	// TODO: Känns sjukt oklart om den här fungerar
	public ArrayList<PhotoLocation> getAllPhotoLocations() {
		SQLiteDatabase database = getReadableDatabase();
		
		String selectQuery = SELECT_ALL + PHOTOLOCATIONS_TABLE_NAME;
		Cursor cursor = database.rawQuery(selectQuery, null);
		
		ArrayList<PhotoLocation> photoLocations = new ArrayList<PhotoLocation>();
		
		if (cursor.moveToFirst()) {
			while (!cursor.isAfterLast()) {
				PhotoLocation photoLocation = new PhotoLocation(cursor.getInt(0),
																cursor.getDouble(1),
																cursor.getDouble(2),
																cursor.getString(3));
				photoLocations.add(photoLocation);
			}
		}
		
		return photoLocations;
	}
}
