package net.sfabian.geoexplorer;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

public class RestTestActivity extends Activity {

	private TextView helloView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rest_test);
		
		helloView = (TextView) findViewById(R.id.rest_test_hello);
		
		connectToNetwork();
	}

	private void connectToNetwork() {
		String url = "http://f.ethnoll.se/geoexplorer_api/?method=get&latitude=2.0&longitude=1.0";// + json.toString();
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			new ConnectToWebTask().execute(url);
		} else {
			helloView.setText("No network connection available.");
		}
	}
	
	private class ConnectToWebTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... urls) {
			return "lol";
//			return RestClient.doGetString(urls[0]);
		}

		@Override
		protected void onPostExecute(String result) {
			helloView.setText(result);
		}
	}
}
