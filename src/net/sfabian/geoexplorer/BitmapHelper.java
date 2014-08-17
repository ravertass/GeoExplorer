package net.sfabian.geoexplorer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

public class BitmapHelper {

	public static Bitmap getDecodedRotatedBitmap(String filePath, int reqWidth,
			int reqHeight) {
		return rotateBitmap(
				decodeSampledBitmapFromFile(filePath, reqWidth, reqHeight),
				getRotationFromFile(filePath));
	}
	
	public static Bitmap decodeSampledBitmapFromFile(String filePath, int reqWidth, int reqHeight) {
		
		// First we check the dimensions of the bitmap
		final BitmapFactory.Options options = new BitmapFactory.Options();
		// With this option, we won't have to load the pixels into memory
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);
		
		// If the photo should be rotated, then we maybe need to switch width and height
		int rotation = getRotationFromFile(filePath);
		if (rotation == 0 || rotation == 180) {
			int tempValue = reqHeight;
			reqHeight = reqWidth;
			reqWidth = tempValue;
		}
		
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		
		// This time, we need the pixels
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(filePath, options);
	}
	
	private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		
		final int rawWidth = options.outWidth;
		final int rawHeight = options.outHeight;
		int inSampleSize = 1;
		
		if (rawWidth > reqWidth || rawHeight > reqHeight) {
			final int halfWidth = rawWidth / 2;
			final int halfHeight = rawHeight / 2;

			// Calculate the largest inSampleSize that is a power of two and keeps the
			// height and the width larger then the requested height and width.
			// A power of two is needed since the decoder rounds down to the nearest power of two.
			while ((halfWidth / inSampleSize) > reqWidth
					&& (halfHeight / inSampleSize) > reqHeight) {
				inSampleSize *= 2;
			}
		}
		
		return inSampleSize;
	}
	
	public static Bitmap rotateBitmapFile(String filePath) {
		Bitmap sourceBitmap = BitmapFactory.decodeFile(filePath);
		int rotation = getRotationFromFile(filePath);
		
		Matrix matrix = new Matrix();
		matrix.postRotate(rotation);
		Bitmap rotatedBitmap = Bitmap
				.createBitmap(sourceBitmap, 0, 0, sourceBitmap.getWidth(),
						sourceBitmap.getHeight(), matrix, true);

		return rotatedBitmap;
	}
	
	public static Bitmap rotateBitmap(Bitmap sourceBitmap, int rotation) {
		Matrix matrix = new Matrix();
		matrix.postRotate(rotation);
		Bitmap rotatedBitmap = Bitmap
				.createBitmap(sourceBitmap, 0, 0, sourceBitmap.getWidth(),
						sourceBitmap.getHeight(), matrix, true);

		return rotatedBitmap;
	}

	private static int getRotationFromFile(String filePath) {
		ExifInterface exif = null;
		try {
			exif = new ExifInterface(filePath);
		} catch (IOException e) {
			Log.e("BitmapHelper", e.toString());
		}
		int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
		
		// Calculate how much we should rotate the photo
		int rotation = 0;
		switch (orientation) {
		case ExifInterface.ORIENTATION_NORMAL:
			rotation = 0;
			break;
		case ExifInterface.ORIENTATION_ROTATE_90:
			rotation = 90;
			break;
		case ExifInterface.ORIENTATION_ROTATE_180:
			rotation = 180;
			break;
		case ExifInterface.ORIENTATION_ROTATE_270:
			rotation = 270;
			break;
		}
		return rotation;
	}
	
	public static void saveBitmapToFile(File file, Bitmap bitmap) {
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file.getAbsolutePath());
			bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
		} catch (FileNotFoundException e) {
			Log.e("BitmapHelper", e.toString());
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				Log.e("BitmapHelper", e.toString());
			}
		}	
	}
}
