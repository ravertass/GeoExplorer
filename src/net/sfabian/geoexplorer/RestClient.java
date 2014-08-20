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
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

public class RestClient {
	
	public static final String API_URL = "http://f.ethnoll.se/geoexplorer_api/";
	public static final String API_GET = "GET";
	public static final String API_SEND = "SEND";
	public static final String API_REPORT = "REPORT";
	
	public static final String API_METHOD = "method";
	public static final String API_ID = "id";
	public static final String API_LATITUDE = "latitude";
	public static final String API_LONGITUDE = "longitude";
	public static final String API_PHOTO = "photo";
	public static final String API_NAME = "name";
	public static final String API_EXPLICIT = "explicit";
	public static final String API_IMPOSSIBLE = "impossible";
	public static final String API_PERSONAL = "personal";
	
	// remove
	public static String getGetUrl(double latitude, double longitude) {
		return API_GET + API_LATITUDE + latitude + API_LONGITUDE + longitude;
	}
	
	public static String getReportUrl(int id, boolean explicit,
			boolean impossible, boolean personal) {
		return API_REPORT + API_ID + id + API_EXPLICIT + boolToInt(explicit)
				+ API_IMPOSSIBLE + boolToInt(impossible) + API_PERSONAL
				+ boolToInt(personal);
	}
	
	private static int boolToInt(boolean bool) {
		if (bool) {
			return 1;
		} else {
			return 0;
		}
	}
	
	public static String doSendToServer(String url, double latitude,
			double longitude, String photo, String name) {
		HashMap<String, String> headerValues = new HashMap<String, String>();
		headerValues.put(API_METHOD, API_SEND);
		headerValues.put(API_LATITUDE, Double.toString(latitude));
		headerValues.put(API_LONGITUDE, Double.toString(longitude));
		headerValues.put(API_PHOTO, photo);
		headerValues.put(API_NAME, name);
		
		return doGetString(url, headerValues);
	}

	public static String doGetString(String url, HashMap<String, String> headerValues) {
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);
		httpPost.addHeader("accept", "text/html");
		
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		for (String key : headerValues.keySet()) {
			nameValuePairs.add(new BasicNameValuePair(key, headerValues.get(key)));
		}
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		HttpResponse response;
		
		String result = "-1";
		InputStream inputStream;
		try {
			response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				inputStream = entity.getContent();
				result = convertStreamToString(inputStream);
				inputStream.close();
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}
	
	public static JSONObject doGetJson(String url) {
		// This is the JSONObject that will be returned
		JSONObject json = null;
		HttpClient httpClient = new DefaultHttpClient();
		// - Prepare a request object
		HttpGet httpGet = new HttpGet(url);
		// - Accept JSON
		httpGet.addHeader("accept", "application/json");
		// - Execute the request
		HttpResponse response;

		// - get the entity contents and convert it to string
		InputStream inputStream;
		try {
			response = httpClient.execute(httpGet);
			// - Get the response entity
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				inputStream = entity.getContent();
				
				String result = convertStreamToString(inputStream);
				// - construct a JSON object with result
				json = new JSONObject(result);
				// - Closing the input stream will trigger connection release
				inputStream.close();
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return json;
	}
	
	private static String convertStreamToString(InputStream inputStream) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		StringBuilder builder = new StringBuilder();
		
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
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
		return builder.toString();
	}
}
