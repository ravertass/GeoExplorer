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

/**
 * This class has static methods to be used on bitmaps. The methods scales, samples
 * and rotates bitmaps, and saves them to files.
 * 
 * @author sfabian
 */

public class BitmapHelper {

	/**
	 * Used to scale a bitmap and crop it into a square form.
	 * @param sourceBitmap
	 * @param reqWidth The requested width for the resulting bitmap.
	 * @param reqHeight The requested height for the resulting bitmap.
	 * @return a cropped and scaled bitmap.
	 */
	public static Bitmap getCroppedScaledBitmap(Bitmap sourceBitmap, int reqWidth, int reqHeight) {
		// This is a nifty way to get a square region of the bitmap.
		Bitmap croppedBitmap;
		if (sourceBitmap.getWidth() > sourceBitmap.getHeight()) {
			croppedBitmap = Bitmap.createBitmap(
					sourceBitmap, 
					sourceBitmap.getWidth()/2 - sourceBitmap.getHeight()/2,
					0,
					sourceBitmap.getHeight(),
					sourceBitmap.getHeight());
		} else {
			croppedBitmap = Bitmap.createBitmap(
					sourceBitmap,
					0,
					sourceBitmap.getHeight()/2 - sourceBitmap.getWidth()/2,
					sourceBitmap.getWidth(),
					sourceBitmap.getWidth());
		}
		// Here we scale the bitmap.
		Bitmap scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, reqWidth, reqHeight, false);
		
		return scaledBitmap;
	}
	
	/**
	 * This method samples and rotates a bitmap, since taken photos
	 * often have a unnecessarily high resolution and since the photo is not
	 * automatically rotated according to the orientation the device was in when
	 * taking the photo.
	 * @param filePath path to the photo file.
	 * @param reqWidth requested width of the sampled photo.
	 * @param reqHeight requested height of the sampled photo.
	 * @return a sampled and rotated photo.
	 */
	public static Bitmap getSampledRotatedBitmap(String filePath, int reqWidth,
			int reqHeight) {
		return rotateBitmap(
				decodeSampledBitmapFromFile(filePath, reqWidth, reqHeight),
				getRotationFromFile(filePath));
	}
	
	/**
	 * Samples an image to the requested width and height. Good to keep memory usage down
	 * when displaying bitmaps.
	 * @param filePath the path to the image.
	 * @param reqWidth the requested width.
	 * @param reqHeight the requested height.
	 * @return a bitmap sampled to the requested size.
	 */
	public static Bitmap decodeSampledBitmapFromFile(String filePath, int reqWidth, int reqHeight) {
		// First we check the dimensions of the bitmap.
		final BitmapFactory.Options options = new BitmapFactory.Options();
		// With this option, we won't have to load the pixels into memory.
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);
		
		// If the photo should be rotated, then we maybe need to switch width and height
		int rotation = getRotationFromFile(filePath);
		if (rotation == 0 || rotation == 180) {
			int tempValue = reqHeight;
			reqHeight = reqWidth;
			reqWidth = tempValue;
		}
		
		// Calculate the inSampleSize, used to sample the photo.
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		
		// This time, we need the pixels.
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(filePath, options);
	}
	
	/**
	 * Calculates the inSampleSize to sample a bitmap to the requested size.
	 * @param options bitmap options.
	 * @param reqWidth requested width.
	 * @param reqHeight requested height.
	 * @return inSampleSize
	 */
	private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		
		final int rawWidth = options.outWidth;
		final int rawHeight = options.outHeight;
		int inSampleSize = 1;
		
		// If any dimension of the bitmap is larger than the requested dimension
		if (rawWidth > reqWidth || rawHeight > reqHeight) {
			final int halfWidth = rawWidth / 2;
			final int halfHeight = rawHeight / 2;

			// Calculate the largest inSampleSize that is a power of two and keep the
			// height and the width larger than the requested height and width.
			// A power of two is needed since the decoder rounds down to the nearest power of two.
			while ((halfWidth / inSampleSize) > reqWidth
					&& (halfHeight / inSampleSize) > reqHeight) {
				inSampleSize *= 2;
			}
		}
		
		return inSampleSize;
	}
	
	/**
	 * Rotates a bitmap according to given rotation.
	 * @param sourceBitmap bitmap to rotate.
	 * @param rotation in degrees.
	 * @return the rotated bitmap.
	 */
	private static Bitmap rotateBitmap(Bitmap sourceBitmap, int rotation) {
		Matrix matrix = new Matrix();
		matrix.postRotate(rotation);
		Bitmap rotatedBitmap = Bitmap
				.createBitmap(sourceBitmap, 0, 0, sourceBitmap.getWidth(),
						sourceBitmap.getHeight(), matrix, true);

		return rotatedBitmap;
	}

	/**
	 * Determines how a photo should be rotated, according to
	 * in which orientation it was taken.
	 * @param filePath of photo to rotate.
	 * @return how the photo should be rotated, in degrees.
	 */
	private static int getRotationFromFile(String filePath) {
		// Get the exifinterface from the photo file.
		ExifInterface exif = null;
		try {
			exif = new ExifInterface(filePath);
		} catch (IOException e) {
			Log.e("BitmapHelper", e.toString());
		}
		// Get the orientation from the exifinterface.
		int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
		
		// Calculate how much we should rotate the photo.
		// This is determined by the orientation in which the photo was taken.
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
	
	/**
	 * Saves the given bitmap to the given file.
	 * @param file to save the bitmap in.
	 * @param bitmap to save in the file.
	 */
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
