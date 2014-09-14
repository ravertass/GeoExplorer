/**
 * 
 */
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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * @author FELIX & fabian
 *
 */
public class CreatorClient {

	// The URL to our server API
	public static final String API_URL = "http://creator3.monkeydancers.com/trigger";
	
	// the api key to use the server api
	public static String API_KEY = "d594a2e074eed72b9f0612efb0d2660cd3ee1893";
	
	// The method names added to the 'method' POST key
	public static final String API_GET = "GET";
	public static final String API_SEND = "SEND";
	public static final String API_REPORT = "REPORT";
	
	
	public static final int HTTP_GET = 0;
	public static final int HTTP_POST = 1;
	
	
	// These are keys used to send values with HTTP POST
	public static final String API_METHOD = "method";
	public static final String API_ID = "id";
	public static final String API_APIKEY = "apikey";
	public static final String API_LATITUDE = "latitude";
	public static final String API_LONGITUDE = "longitude";
	public static final String API_PHOTO = "photo";
	public static final String API_NAME = "name";
	public static final String API_EXPLICIT = "explicit";
	public static final String API_IMPOSSIBLE = "impossible";
	public static final String API_PERSONAL = "personal";
	
	public static String doGetPlayers(String url) throws JSONException {
		HashMap<String, String> postValues = new HashMap<String, String>();
		postValues.put("key", API_KEY);
		//postValues.put("scope", "5e64030");
		String playersString = doServerOperation(
				"http://creator3.monkeydancers.com/api/v1/game_objects",
				HTTP_GET, postValues);
		
		return playersString;
	}
	
	/**
	 * @throws JSONException 
	 */
	public static String doFoundLocation(String url) throws JSONException {
		
		/*HashMap<String, String> postValues1 = new HashMap<String, String>();
		postValues1.put("key", API_KEY);
		String wizLoad = doServerOperation("http://creator3.monkeydancers.com/api/v1/game_objects/c172553", HTTP_GET, postValues1);
		Log.d("creator response1",wizLoad);
		JSONObject jsonWizLoad = new JSONObject(wizLoad);
		//*/
		
		
		///*
		HashMap<String, String> postValues2 = new HashMap<String, String>();
		postValues2.put("key", API_KEY);
		String testLoad = doServerOperation(
				"http://creator3.monkeydancers.com/api/v1/game_objects/c14edb9",
				HTTP_GET, postValues2);
		Log.d("creator response2", testLoad);
		JSONObject jsonTestLoad = new JSONObject(testLoad);
		//*/
		
		///*
		postValues2.put("key", API_KEY);
		String testFunctionLoad = doServerOperation(
				"http://creator3.monkeydancers.com/api/v1/game_objects/e1e772b",
				HTTP_GET, postValues2);
		Log.d("creator response2", testFunctionLoad);
		JSONObject jsonTestFunctionLoad = new JSONObject(testFunctionLoad);
		//*/
		
		/*
		HashMap<String, String> postValues3 = new HashMap<String, String>();
		postValues3.put("key", API_KEY);
		postValues3.put("scope", "5e64030");
		String spellLoad = doServerOperation("http://creator3.monkeydancers.com/api/v1/game_objects", HTTP_GET, postValues3);
		Log.d("creator response3",spellLoad);
		JSONArray jsonSpellLoad = new JSONArray(spellLoad);
		//*/
		
		// Create a hashmap with the values to be sent using HTTP POST
		HashMap<String, String> postValues4 = new HashMap<String, String>();
		String result;
		//postValues.put(API_METHOD, API_GET); // Might have to be POST
		
		/*
		postValues4.put(API_APIKEY, API_KEY);
		postValues4.put("actor", jsonWizLoad.getJSONObject("object").getString("identifier"));
		postValues4.put("target", jsonWizLoad.getJSONObject("object").getString("identifier"));
		//postValues.put("target", jsonSpellLoad.getJSONObject(0).getString("identifier"));
		//postValues.put(API_LONGITUDE, Double.toString(playerLatitude));
		//postValues.put(API_LATITUDE, Double.toString(playerLongitude));	
		result = doServerOperation(url, HTTP_POST, postValues4);
		Log.d("creator response4",result);
		//*/
		
		/*
		postValues4.put(API_APIKEY, API_KEY);
		postValues4.put("actor", jsonWizLoad.getJSONObject("object").getString("identifier"));
		postValues4.put("target", jsonSpellLoad.getJSONObject(0).getString("identifier"));
		result = doServerOperation(url, HTTP_POST, postValues4);
		Log.d("creator response5",result);
		//*/
		
		/*
		postValues4.put(API_APIKEY, API_KEY);
		postValues4.put("actor", jsonSpellLoad.getJSONObject(0).getString("identifier"));
		postValues4.put("target", jsonSpellLoad.getJSONObject(0).getString("identifier"));
		result = doServerOperation(url, HTTP_POST, postValues4);
		Log.d("creator response5",result);
		//*/
		
		///*
		postValues4.put(API_APIKEY, API_KEY);
		postValues4.put("actor", jsonTestLoad.getJSONObject("object").getString("identifier"));
		postValues4.put("target", jsonTestFunctionLoad.getJSONObject("object").getString("identifier"));
		result = doServerOperation(url, HTTP_POST, postValues4);
		Log.d("creator response6",result);
		//*/
		
		/*
		postValues4.put(API_APIKEY, API_KEY);
		postValues4.put("actor", jsonTestLoad.getJSONObject("object").getString("identifier"));
		postValues4.put("target", jsonTestLoad.getJSONObject("object").getString("identifier"));	
		result = doServerOperation(url, HTTP_POST, postValues4);
		Log.d("creator response7",result);
		//*/
		
		
		return result;
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
		
		return doServerOperation(url, HTTP_POST, postValues);
	}

	/**
	 * Sends a HTTP POST request to the given URL, with the values in the hashmap
	 * as POST values.
	 * @param url
	 * @param postValues
	 * @return
	 */
	public static String doServerOperation(String url, int type, HashMap<String, String> postValues) {
		// Create a HTTP client
		HttpClient httpClient = new DefaultHttpClient();
		
		// Create a http-request to the given URL
		HttpUriRequest httpRequest = null;
		switch(type) {
		case (HTTP_GET):
			// Append data to url
			url += "?";
			boolean first = true;
			for (String key : postValues.keySet()) {
				String val = postValues.get(key);
				if (first) {
					first = false;
				} else {
					url += "&";
				}
				url+= key + "=" + val;
			}
			httpRequest = new HttpGet(url); // Create a GET request to the given URL
			break;
		case (HTTP_POST):
			// Add all key/value pairs in the hashmap to the POST request
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			for (String key : postValues.keySet()) {
				nameValuePairs.add(new BasicNameValuePair(key, postValues.get(key)));
			}
			httpRequest = new HttpPost(url); // Create a POST request to the given URL
			try {
				((HttpPost) httpRequest).setEntity(new UrlEncodedFormEntity(nameValuePairs));
			} catch (UnsupportedEncodingException e1) {
				Log.e("RestClient", e1.toString());
			}
			break;
		}
		// Add the expected return type to the header
		httpRequest.addHeader("Accept", "application/json");
		
		
		// The response from the server
		HttpResponse response;
		
		String result = "-1";
		InputStream inputStream;
		try {
			// Execute the POST request and get a response from the server
			response = httpClient.execute(httpRequest);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				// Get the stream from the server and convert it into a string
				inputStream = entity.getContent();
				result = convertStreamToString(inputStream);
				inputStream.close();
			}
		} catch (ClientProtocolException e) {
			Log.e("RestClient", e.toString());
		} catch (IllegalStateException e) {
			Log.e("RestClient", e.toString());
		} catch (IOException e) {
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
