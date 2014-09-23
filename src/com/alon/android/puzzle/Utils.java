package com.alon.android.puzzle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.alon.android.puzzle.lazylist.ListItemData;

@SuppressLint("UseSparseArrays")
public class Utils {

	public static final String APP_NAME = "PuzzleMe";

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
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		InputStream in = getImageStream(selectedImage);
		BitmapFactory.decodeStream(in, null, options);
		in.close();

		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);

		options.inJustDecodeBounds = false;
		in = getImageStream(selectedImage);
		Bitmap result = BitmapFactory.decodeStream(in, null, options);
		in.close();
		return result;
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
			throw new PuzzleException("image not selected");
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
		handleError(e, e.getMessage());
	}

	public void handleError(Exception e, String message) {
		logError(e, message);
		message("Application Error\n" + message);
	}

	public void logError(Exception e, String message) {
		if (e == null) {
			Log.e(APP_NAME, "Error: " + message);
		} else {
			Log.e(APP_NAME, "Application Error", e);
		}

		internalLog(e, message);
	}

	public void debug(Object logged) {
		String message = logged.toString();
		Log.d(APP_NAME, message);
		internalLog(null, message);
	}

	private void internalLog(Exception e, String message) {
		if (!ActivityMain.INTERNAL_LOGS) {
			return;
		}

		String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
				Locale.US).format(new Date());

		String record = timeStamp + " " + message + "\n";

		try {
			FileOutputStream out = new FileOutputStream(getLogFile(), true);
			PrintStream print = new PrintStream(out);
			print.print(record);
			if (e != null) {
				e.printStackTrace(print);
			}
			out.close();
		} catch (Exception e2) {
			message("write to internal log failed " + e2.getMessage());
		}
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

	static public String loadFile(File file) throws Exception {
		FileInputStream in = new FileInputStream(file);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		copyStream(in, out, false);
		String result = out.toString();
		out.close();
		return result;
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

	public void download(ListItemData item,
			final InterfacePostDownload postDownload) {

		AsyncTask<ListItemData, Integer, Void> task = new AsyncTask<ListItemData, Integer, Void>() {

			@Override
			protected Void doInBackground(ListItemData... items) {
				try {
					downloadInBackground(items[0], postDownload);
				} catch (Exception e) {
					handleError(e);
				}
				return null;
			}
		};
		task.execute(item);
	}

	private void downloadInBackground(ListItemData item,
			InterfacePostDownload postDownload) throws Exception {
		File storage = getStorageSubFolder("download");

		File image = new File(storage, item.getName() + ".jpg");
		if (!image.exists()) {
			Utils.saveFileFromUrl(item.getUrlBig(), image);
		}
		GameSettings settings = new GameSettings(m_context);
		settings.setImage(Uri.fromFile(image));
		postDownload.postDownload();
	}

	static public byte[] serializeObject(Serializable object) throws Exception {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		ObjectOutputStream output = new ObjectOutputStream(bytes);
		output.writeObject(object);
		output.flush();

		byte[] result = bytes.toByteArray();

		output.close();
		bytes.close();

		return result;
	}

	static public Object deserializeObject(byte[] data) throws Exception {
		ByteArrayInputStream bytes = new ByteArrayInputStream(data);
		ObjectInputStream in = new ObjectInputStream(bytes);
		Object object = in.readObject();
		in.close();
		bytes.close();
		return object;
	}

	public void setPiecesButtonText(View topView, int buttonId) {
		Button button = (Button) topView.findViewById(buttonId);
		GameSettings settings = new GameSettings(m_context);
		button.setText(settings.getPieces() + " x " + settings.getPieces());
		button.invalidate();
	}

	public File getLogFile() {
		File folder = getStorageSubFolder(Utils.APP_NAME + "Log");
		File log = new File(folder, "app.log");
		return log;
	}

	public File getNewImage() throws Exception {
		return getNewFile("png");
	}

	public File getNewFile(String extension) throws Exception {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
				.format(new Date());
		String imageFileName = "PuzzleMe_" + timeStamp + "_";
		File storageDir = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		if (!storageDir.exists()) {
			storageDir.mkdirs();
		}
		File file = File.createTempFile(imageFileName, "." + extension,
				storageDir);
		return file;
	}

}
