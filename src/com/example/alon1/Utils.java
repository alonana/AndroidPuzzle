package com.example.alon1;

import java.io.FileNotFoundException;
import java.io.InputStream;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

public class Utils {

	private Context m_context;

	public Utils(Context context) {
		m_context = context;
	}

	public void message(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(m_context);
		builder.setMessage(message);
		AlertDialog alert = builder.create();
		alert.show();
	}

	public Bitmap decodeSampledBitmapFromUri(Uri selectedImage, int reqWidth,
			int reqHeight) {
		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory
				.decodeStream(getImageStream(selectedImage), null, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeStream(getImageStream(selectedImage), null,
				options);
	}

	private int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		int height = options.outHeight;
		int width = options.outWidth;
		int inSampleSize = 1;

		while (height > reqHeight || width > reqWidth) {

			height /= 2;
			width /= 2;
			inSampleSize *= 2;
		}

		return inSampleSize;
	}

	private InputStream getImageStream(Uri selectedImage) {
		try {
			InputStream imageStream = m_context.getContentResolver()
					.openInputStream(selectedImage);
			return imageStream;
		} catch (FileNotFoundException e) {
			Log.e("tag", "failed to open file", e);
			message("error openning file " + selectedImage);
			return null;
		}
	}

}
