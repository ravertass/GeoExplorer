package net.sfabian.geoexplorer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.util.Log;

/**
 * This class contains static method for connecting with our server.
 * The methods are used to get photolocations, send photolocations and
 * report photolocations.
 *  
 * @author sfabian
 */

public class RestClient {
	
	// The URL to our server API
	public static final String API_URL = "http://f.ethnoll.se/geoexplorer_api/";
	// The method names added to the 'method' POST key
	public static final String API_GET = "GET";
	public static final String API_SEND = "SEND";
	public static final String API_REPORT = "REPORT";
	
	// These are keys used to send values with HTTP POST
	public static final String API_METHOD = "method";
	public static final String API_ID = "id";
	public static final String API_LATITUDE = "latitude";
	public static final String API_LONGITUDE = "longitude";
	public static final String API_PHOTO = "photo";
	public static final String API_NAME = "name";
	public static final String API_EXPLICIT = "explicit";
	public static final String API_IMPOSSIBLE = "impossible";
	public static final String API_PERSONAL = "personal";
	
	/**
	 * This method performs the "get" operation.
	 * It gets all photolocations nearby the user from the server's database.
	 * @param playerLatitude the player's current latitude
	 * @param playerLongitude the player's current longitude
	 * @return a string of a JSONArray with all nearby photolocations.
	 */
	public static String doGetFromServer(String url, double playerLatitude,
			double playerLongitude) {
		// Create a hashmap with the values to be sent using HTTP POST
		HashMap<String, String> postValues = new HashMap<String, String>();
		postValues.put(API_METHOD, API_GET);
		postValues.put(API_LATITUDE, Double.toString(playerLatitude));
		postValues.put(API_LONGITUDE, Double.toString(playerLongitude));		
		
		return doServerOperation(url, postValues);
	}
	
	/**
	 * This method performs the "send" operation.
	 * It sends a photolocation that is added to the server's database.
	 * @param latitude of the photolocation
	 * @param longitude of the photolocation
	 * @param photo of the photolocation, in a base64 string
	 * @param name of the photolocation
	 * @return
	 */
	public static String doSendToServer(String url, double latitude,
			double longitude, String photo, String name) {
		// Create a hashmap with the values to be sent using HTTP POST
		HashMap<String, String> postValues = new HashMap<String, String>();
		postValues.put(API_METHOD, API_SEND);
		postValues.put(API_LATITUDE, Double.toString(latitude));
		postValues.put(API_LONGITUDE, Double.toString(longitude));
		postValues.put(API_PHOTO, photo);
		postValues.put(API_NAME, name);
		
		return doServerOperation(url, postValues);
	}

	/**
	 * Sends a HTTP POST request to the given URL, with the values in the hashmap
	 * as POST values.
	 * @param url
	 * @param postValues
	 * @return
	 */
	public static String doServerOperation(String url, HashMap<String, String> postValues) {
		// Create a HTTP client
		HttpClient httpClient = new DefaultHttpClient();
		// Create a POST request to the given URL
		HttpPost httpPost = new HttpPost(url);
		// Add the expected return type to the header
		httpPost.addHeader("accept", "text/html");
		
		// Add all key/value pairs in the hashmap to the POST request
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		for (String key : postValues.keySet()) {
			nameValuePairs.add(new BasicNameValuePair(key, postValues.get(key)));
		}
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		} catch (UnsupportedEncodingException e1) {
			Log.e("RestClient", e1.toString());
		}
		
		// The response from the server
		HttpResponse response;
		
		String result = "-1";
		InputStream inputStream;
		try {
			// Execute the POST request and get a response from the server
			response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				// Get the stream from the server and convert it into a string
				inputStream = entity.getContent();
				result = convertStreamToString(inputStream);
				inputStream.close();
			}
		} catch (ClientProtocolException e) {
			Log.e("RestClient", e.toString());
		} catch (IllegalStateException | IOException e) {
			Log.e("RestClient", e.toString());
		}
		
		// Return the string result from the server
		return result;
	}
	
	/**
	 * This methods turns an input stream into a string
	 * @param inputStream
	 * @return a string from the stream.
	 */
	private static String convertStreamToString(InputStream inputStream) {
		// Create a reader for the stream.
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		// This builder will build a string from the stream.
		StringBuilder builder = new StringBuilder();
		
		String line = null;
		try {
			// As long as there are lines in the stream reader, append lines to the string
			while ((line = reader.readLine()) != null) {
				// Maybe a linebreak should be concatenated here, but it should not be 
				// needed for our values and I don't want to be bothered with not adding
				// a linebreak to the last line.
				builder.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// return the string from the stream
		return builder.toString();
	}
}
