package com.alon.android.puzzle;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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
		Integer loadId = m_loadedSounds.get(soundId);
		if (loadId == null) {
			throw new RuntimeException("sound not loaded to util " + soundId);
		}
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

	public void setFullScreen() {
		Activity activity = (Activity) m_context;
		activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
		activity.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	public void handleError(Exception e) {
		Log.e("tag", "error in application", e);
		message("error in application " + e.getMessage());

	}

	static public void sleep(long milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
		}
	}

	public String getResourceText(int id) throws Exception {
		InputStream in = m_context.getResources().openRawResource(id);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		copyStream(in, out, false);
		String data = out.toString();
		out.close();
		return data;
	}

	static public void copyStream(InputStream in, OutputStream out,
			boolean closeOutput) throws Exception {
		byte[] buffer = new byte[1024];
		while (true) {
			int read = in.read(buffer);
			if (read == -1) {
				break;
			}
			out.write(buffer, 0, read);
		}
		in.close();
		if (closeOutput) {
			out.close();
		}
	}

	public File getStorageFolder() {
		File folder;
		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			folder = new File(
					android.os.Environment.getExternalStorageDirectory(), this
							.getClass().getName());
		} else {
			folder = m_context.getCacheDir();
		}

		if (!folder.exists()) {
			folder.mkdirs();
		}
		return folder;
	}

	public File getStorageSubFolder(String subFolder) {
		File storage = getStorageFolder();
		File subFile = new File(storage, subFolder);

		if (!subFile.exists()) {
			subFile.mkdirs();
		}
		return subFile;
	}

	static public void saveFileFromUrl(String url, File file) throws Exception {
		URL imageUrl = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
		conn.setConnectTimeout(30000);
		conn.setReadTimeout(30000);
		conn.setInstanceFollowRedirects(true);
		InputStream is = conn.getInputStream();
		OutputStream os = new FileOutputStream(file);
		Utils.copyStream(is, os, true);
		conn.disconnect();
	}
}
