package com.alon.android.puzzle;

import java.io.InputStream;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

@SuppressLint("UseSparseArrays")
public class Utils {

	private Context m_context;
	private SoundPool m_sound;
	private HashMap<Integer, Integer> m_loadedSounds;

	public Utils(Context context) {
		m_context = context;
	}

	public void loadSound(int soundId) {
		if (m_sound == null) {
			m_sound = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
			m_loadedSounds = new HashMap<Integer, Integer>();
		}

		int loadId = m_sound.load(m_context, soundId, 1);
		m_loadedSounds.put(soundId, loadId);
	}

	public void playSound(int soundId) {
		int loadId = m_loadedSounds.get(soundId);
		m_sound.play(loadId, 1, 1, 0, 0, 1);
	}

	public void message(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(m_context);
		builder.setMessage(message);
		AlertDialog alert = builder.create();
		alert.show();
	}

	public Bitmap decodeSampledBitmapFromUri(Uri selectedImage, int reqWidth,
			int reqHeight) throws Exception {
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

	private InputStream getImageStream(Uri selectedImage) throws Exception {
		if (selectedImage == null) {
			throw new Exception("image not selected");
		}
		InputStream imageStream = m_context.getContentResolver()
				.openInputStream(selectedImage);
		return imageStream;
	}

	public void setFullScreen(Activity activity) {
		activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
		activity.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	public void handleError(Exception e) {
		Log.e("tag", "error in application", e);
		message("error in application " + e.getMessage());

	}

}
