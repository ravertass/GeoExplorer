package net.sfabian.geoexplorer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

public class RestClient {

	public static String doGetString(String url) {
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		httpGet.addHeader("accept", "text/html");
		HttpResponse response;
		
		String result = "lol";
		InputStream inputStream;
		try {
			response = httpClient.execute(httpGet);
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
				builder.append(line + "\n");
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
