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

import com.alon.android.puzzle.play.PuzzleActivity;
import com.alon.android.puzzle.play.PuzzleView;

public class NewGameActivity extends ActionBarActivity implements
		OnPreDrawListener {

	private static final String SETTINGS = NewGameActivity.class
			.getSimpleName() + "settings";

	private static final int SELECT_PHOTO = 100;
	private static final int TAKE_PHOTO = 101;
	private static final int GET_PIECES = 102;
	private static final int START_PUZZLE = 103;

	private Utils m_utils;
	private boolean m_inAction;
	private boolean m_loadImageRequired;
	private Uri m_cameraOutputUri;
	private GameSettings m_settings;

	public NewGameActivity() {
		m_utils = new Utils(this);
		m_inAction = false;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		m_settings = new GameSettings(this);
		m_utils.setFullScreen();
		setContentView(R.layout.activity_new_game);

		setPiecesButtonText();
		Button button = (Button) findViewById(R.id.btnStart);
		button.setEnabled(false);

		m_utils.loadSound(R.raw.start);
		m_utils.loadSound(R.raw.click);

		if (!getPackageManager()
				.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			Button camera = (Button) findViewById(R.id.btnCamera);
			camera.setEnabled(false);
		}

		m_loadImageRequired = true;
		ImageView imageView = (ImageView) findViewById(R.id.imagePreview);
		ViewTreeObserver observer = imageView.getViewTreeObserver();
		observer.addOnPreDrawListener(this);
	}

	private void setPiecesButtonText() {
		Button button = (Button) findViewById(R.id.btnPieces);
		button.setText(m_settings.getPieces() + " x " + m_settings.getPieces());
		button.invalidate();
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

		outState.putSerializable(SETTINGS, m_settings);

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

		m_settings = (GameSettings) savedInstanceState
				.getSerializable(SETTINGS);
		if (m_settings.getImage() != null) {
			m_loadImageRequired = true;
		}
	}

	private void resizePreview() {
		ImageView imageView = (ImageView) findViewById(R.id.imagePreview);
		int height = imageView.getMeasuredHeight();
		int width = imageView.getMeasuredWidth();
		int min = Math.min(height, width);
		if (min == 0) {
			return;
		}

		LayoutParams params = imageView.getLayoutParams();
		params.width = min;
		imageView.setLayoutParams(params);
	}

	public void getPictureFromGallery(View view) {
		if (m_inAction) {
			return;
		}
		m_inAction = true;
		try {
			getPictureFromGalleryWorker();
		} catch (Exception e) {
			m_utils.handleError(e);
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
		} catch (Exception e) {
			m_utils.handleError(e);
		} finally {
			m_inAction = false;
		}
	}

	public void setPieces(View view) {
		if (m_inAction) {
			return;
		}
		m_inAction = true;
		try {
			setPiecesWorker();
		} finally {
			m_inAction = false;
		}
	}

	private void setPiecesWorker() {
		m_utils.playSound(R.raw.click);
		Intent intent = new Intent(this, PiecesActivity.class);
		startActivityForResult(intent, GET_PIECES);
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
		m_utils.playSound(R.raw.click);
		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		photoPickerIntent.setType("image/*");
		startActivityForResult(photoPickerIntent, SELECT_PHOTO);
	}

	private void getPictureFromCameraWorker() throws Exception {
		m_utils.playSound(R.raw.click);
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

		Intent intent = new Intent(this, PuzzleActivity.class);
		intent.putExtra(PuzzleActivity.URI, m_settings.getImage());
		intent.putExtra(PuzzleActivity.SIDE_PIECES, m_settings.getPieces());
		startActivityForResult(intent, START_PUZZLE);
	}

	private void createImageFile() throws Exception {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
				.format(new Date());
		String imageFileName = "JPEG_" + timeStamp + "_";
		File storageDir = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		if (!storageDir.exists()) {
			storageDir.mkdirs();
		}
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

		if (resultCode != RESULT_OK) {
			return;
		}

		switch (requestCode) {

		case SELECT_PHOTO:
			m_settings.setImage(intentData.getData());
			m_loadImageRequired = true;
			break;

		case TAKE_PHOTO:
			saveToGallery();
			m_settings.setImage(m_cameraOutputUri);
			m_loadImageRequired = true;
			break;

		case GET_PIECES:
			m_settings.setPieces(intentData.getIntExtra(PiecesActivity.AMOUNT,
					2));
			setPiecesButtonText();
			break;

		case START_PUZZLE:
			int score = intentData.getIntExtra(PuzzleView.SCORE, 0);
			m_settings.addScore(score);
			finish();
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
		if (m_settings.getImage() == null) {
			return;
		}
		m_loadImageRequired = false;

		resizePreview();

		ImageView imageView = (ImageView) findViewById(R.id.imagePreview);
		Bitmap bitmap = m_utils.decodeSampledBitmapFromUri(
				m_settings.getImageAsUri(), imageView.getWidth(),
				imageView.getHeight());

		imageView.setImageBitmap(bitmap);

		Button button = (Button) findViewById(R.id.btnStart);
		button.setEnabled(true);
	}

}
