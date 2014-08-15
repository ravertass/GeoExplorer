package net.sfabian.geoexplorer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;

public class ExploreGridActivity extends Activity {
	
	private static final int PHOTOS_IN_ROW = 3;
	
	private LinearLayout photoGrid;
	private boolean gridInitialized = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_explore_grid);
		
		photoGrid = (LinearLayout) findViewById(R.id.explore_grid_photo_grid);
	}
	
	/**
	 * The grid is initialized in this method, because we need the size of
	 * the grid layout to calculate the height of the images (since we want
	 * them to be rectangular).
	 * TODO: Detta kanske ändras när vi väl stoppar in bilder här
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (!gridInitialized) {
			initializeGrid();
		}
	}

	// TODO: Ett annat antal bilder per rad behövs i landscape-läge!
	private void initializeGrid() {
		// TODO This will load the bitmaps for the photos of the nearby PhotoLocations
		// For now, it's just an array of strings
		final String[] photos = {"apple", "banana", "orange", "eggplant"}; 
		
		LinearLayout photoRow = null; //since the compiler makes me do this
		for (int i = 0; i < photos.length; i++) {
			if (i % PHOTOS_IN_ROW == 0) {
				photoRow = new LinearLayout(this);
				photoRow.setOrientation(LinearLayout.HORIZONTAL);
				photoRow.setWeightSum(PHOTOS_IN_ROW);
				photoRow.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
				photoGrid.addView(photoRow);
			}
			
			Button photoButton = new Button(this);
			photoButton.setText(photos[i]);
			photoButton.setLayoutParams(getPhotoParams());
			
			final int index = i;
			photoButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					gotoExploreLocation(photos[index]);
				}
			});
			
			photoRow.addView(photoButton);
		}
		gridInitialized = true;
	}

	private LinearLayout.LayoutParams getPhotoParams() {
		int gridWidth = photoGrid.getWidth();
		int photoSideLength = gridWidth / PHOTOS_IN_ROW;
		LinearLayout.LayoutParams photoParams = new LinearLayout.LayoutParams(photoSideLength, photoSideLength, 1);
//		photoParams.setMargins(5, 5, 5, 5);
		
		return photoParams;
	}
	
	/**
	 * Called when a photo in the grid is clicked 
	 * TODO: Här kommer inte en sträng att skickas med, utan typ en nyckel 
	 * till platsen/fotot i en lokal databas
	 */
	public void gotoExploreLocation(String string) {
		Intent intent = new Intent(this, ExploreLocationActivity.class);
		intent.putExtra(getString(R.string.intent_key_photo_location), string);
		startActivity(intent);
	}
}
