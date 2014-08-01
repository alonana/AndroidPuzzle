package com.alon.android.puzzle;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

public class MainActivity extends ActionBarActivity implements
		OnPreDrawListener {

	private static final String SAVED_URI = MainActivity.class.getSimpleName()
			+ "uri";

	private static final int SELECT_PHOTO = 100;
	private static final int TAKE_PHOTO = 101;

	private Utils m_utils;
	private Uri m_uri;
	private boolean m_inAction;
	private boolean m_loadImageRequired;
	private Uri m_cameraOutputUri;

	public MainActivity() {
		m_utils = new Utils(this);
		m_inAction = false;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		m_utils.setFullScreen(this);
		setContentView(R.layout.activity_main);

		Button button = (Button) findViewById(R.id.btnStart);
		button.setEnabled(false);

		m_utils.loadSound(R.raw.start);

		if (!getPackageManager()
				.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			Button camera = (Button) findViewById(R.id.btnCamera);
			camera.setEnabled(false);
		}

		m_loadImageRequired = false;
		ImageView imageView = (ImageView) findViewById(R.id.imagePreview);
		ViewTreeObserver observer = imageView.getViewTreeObserver();
		observer.addOnPreDrawListener(this);
	}

	@Override
	public boolean onPreDraw() {
		try {
			loadImageFromUri();
		} catch (Exception e) {
			m_utils.handleError(e);
		}
		return true;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {

		if (m_uri != null) {
			outState.putString(SAVED_URI, m_uri.toString());
		}

		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {

		super.onRestoreInstanceState(savedInstanceState);

		try {
			onRestoreInstanceStateWorker(savedInstanceState);
		} catch (Exception e) {
			m_utils.handleError(e);
		}
	}

	private void onRestoreInstanceStateWorker(Bundle savedInstanceState)
			throws Exception {
		String uri = savedInstanceState.getString(SAVED_URI);
		if (uri != null) {
			m_uri = Uri.parse(uri);
			m_loadImageRequired = true;
		}
	}

	private void resizePreview() {
		ImageView imageView = (ImageView) findViewById(R.id.imagePreview);
		int height = imageView.getMeasuredHeight();
		int width = imageView.getMeasuredWidth();
		int min = Math.min(height, width);
		if (min > 50) {
			LayoutParams params = imageView.getLayoutParams();
			params.width = min;
			imageView.setLayoutParams(params);
		}
	}

	public void getPictureFromGallery(View view) {
		if (m_inAction) {
			return;
		}
		m_inAction = true;
		try {
			getPictureFromGalleryWorker();
		} finally {
			m_inAction = false;
		}
	}

	public void getPictureFromCamera(View view) throws Exception {
		if (m_inAction) {
			return;
		}
		m_inAction = true;
		try {
			getPictureFromCameraWorker();
		} finally {
			m_inAction = false;
		}
	}

	public void startPuzzle(View view) {
		if (m_inAction) {
			return;
		}
		m_inAction = true;
		try {
			startPuzzleWorker();
		} finally {
			m_inAction = false;
		}
	}

	private void getPictureFromGalleryWorker() {
		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		photoPickerIntent.setType("image/*");
		startActivityForResult(photoPickerIntent, SELECT_PHOTO);
	}

	private void getPictureFromCameraWorker() throws Exception {

		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (takePictureIntent.resolveActivity(getPackageManager()) == null) {
			return;
		}
		createImageFile();
		takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, m_cameraOutputUri);
		startActivityForResult(takePictureIntent, TAKE_PHOTO);
	}

	private void startPuzzleWorker() {
		m_utils.playSound(R.raw.start);

		SeekBar seek = (SeekBar) findViewById(R.id.seekBarPieces);
		int sidePieces = seek.getProgress() + 2;

		Intent intent = new Intent(this, PuzzleActivity.class);
		intent.putExtra(PuzzleActivity.URI, m_uri.toString());
		intent.putExtra(PuzzleActivity.SIDE_PIECES, sidePieces);
		startActivity(intent);
	}

	private void createImageFile() throws Exception {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
				.format(new Date());
		String imageFileName = "JPEG_" + timeStamp + "_";
		File storageDir = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		File file = File.createTempFile(imageFileName, ".jpg", storageDir);
		m_cameraOutputUri = Uri.fromFile(file);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intentData) {

		try {
			onActivityResultWorker(requestCode, resultCode, intentData);
		} catch (Exception e) {
			m_utils.handleError(e);
		}
	}

	private void onActivityResultWorker(int requestCode, int resultCode,
			Intent intentData) throws Exception {
		super.onActivityResult(requestCode, resultCode, intentData);

		switch (requestCode) {
		case SELECT_PHOTO:
			if (resultCode != RESULT_OK) {
				return;
			}
			m_uri = intentData.getData();
			m_loadImageRequired = true;
			break;
		case TAKE_PHOTO:
			if (resultCode != RESULT_OK) {
				return;
			}
			saveToGallery();
			m_uri = m_cameraOutputUri;
			m_loadImageRequired = true;
			break;
		}
	}

	private void saveToGallery() {
		Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		intent.setData(m_cameraOutputUri);
		this.sendBroadcast(intent);
	}

	private void loadImageFromUri() throws Exception {
		if (!m_loadImageRequired) {
			return;
		}
		m_loadImageRequired = false;

		resizePreview();

		ImageView imageView = (ImageView) findViewById(R.id.imagePreview);
		Bitmap bitmap = m_utils.decodeSampledBitmapFromUri(m_uri,
				imageView.getWidth(), imageView.getHeight());

		imageView.setImageBitmap(bitmap);

		Button button = (Button) findViewById(R.id.btnStart);
		button.setEnabled(true);
	}

}
