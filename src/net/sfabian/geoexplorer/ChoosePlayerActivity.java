package net.sfabian.geoexplorer;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ChoosePlayerActivity extends Activity {

	private LinearLayout playerList;
	private ArrayList<String> players;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choose_player);
		
		playerList = (LinearLayout) findViewById(R.id.player_list);
		
		players = new ArrayList<String>();
		
		new CreatorGetPlayers().execute(CreatorClient.API_URL);
	}

	/**
	 * This class connects to our server and performs the HTTP requests to creator
	 * @author sfabian
	 */
	private class CreatorGetPlayers extends AsyncTask<String, Void, String> {
		/**
		 * Sends a HTTP POST request to the server to retrieve nearby photolocations.
		 */
		@Override
		protected String doInBackground(String... urls) {
			try {
				return CreatorClient.doGetPlayers(urls[0]);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}
		/**
		 * This callback is run after the above method. It adds the retrieved photolocations
		 * to the local database.
		 */
		@Override
		protected void onPostExecute(String result) {
			JSONArray playersJson;
			getPlayersFromJson(result);
			addPlayerViews();
		}
	}
	
	private void getPlayersFromJson(String playersString) {
		try {	
			JSONArray playersJsonArray = new JSONArray(playersString);
			
			for (int i = 0; i < playersJsonArray.length(); i++) {
				JSONObject row = playersJsonArray.getJSONObject(i);
				Log.e("lol", row.toString());
				if (row.optJSONObject("geoexplorer") != null && 
						row.optJSONObject("geoexplorer").getInt("value") == 1 &&
						row.optJSONObject("player") != null &&
						row.optJSONObject("player").getInt("value") != 0) {
					Log.e("banan", " "+row.getJSONObject("geoexplorer").getInt("value"));
					
					String playerName = row.getString("name");
					players.add(playerName);
				}
			}
		} catch (JSONException e) {
			// Auto-generated catch block lol
			e.printStackTrace();
		}
	}
	
	private void addPlayerViews() {
		// This removes the progress bar icon
		playerList.removeAllViews();
		for (String player : players) {
			TextView playerView = new TextView(getApplicationContext());
			playerView.setText(player);
			playerList.addView(playerView);
		}
	}
}
