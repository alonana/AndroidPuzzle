package com.alon.android.puzzle;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.Button;
import android.widget.ImageView;

public class FragmentNewGame extends FragmentBase implements OnPreDrawListener,
		OnClickListener {

	private static final int SELECT_PHOTO = 100;
	private static final int TAKE_PHOTO = 101;

	private View m_topView;
	private boolean m_inAction;
	private boolean m_loadImageRequired;
	private Uri m_cameraOutputUri;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		m_inAction = false;

		getUtils().loadSound(R.raw.start);
		getUtils().loadSound(R.raw.click);

		m_topView = inflater.inflate(R.layout.fragment_new_game, container,
				false);
		setPiecesButtonText();
		Button button = (Button) m_topView.findViewById(R.id.btnStart);
		button.setEnabled(false);

		if (!getActivity().getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			Button camera = (Button) m_topView.findViewById(R.id.btnCamera);
			camera.setEnabled(false);
		}

		m_loadImageRequired = true;
		ImageView imageView = (ImageView) m_topView
				.findViewById(R.id.imagePreview);
		ViewTreeObserver observer = imageView.getViewTreeObserver();
		observer.addOnPreDrawListener(this);

		m_topView.findViewById(R.id.btnCamera).setOnClickListener(this);
		m_topView.findViewById(R.id.btnGallery).setOnClickListener(this);
		m_topView.findViewById(R.id.btnStart).setOnClickListener(this);
		m_topView.findViewById(R.id.btnPieces).setOnClickListener(this);
		m_topView.findViewById(R.id.btnDownload).setOnClickListener(this);

		updateButtons();

		return m_topView;
	}

	public void updateButtons() {
		int postSign;
		if (getMainActivity().isSignedIn()) {
			postSign = View.VISIBLE;
		} else {
			postSign = View.GONE;
		}

		m_topView.findViewById(R.id.btnDownload).setVisibility(postSign);
	}

	private void setPiecesButtonText() {
		Button button = (Button) m_topView.findViewById(R.id.btnPieces);
		button.setText(getGameSettings().getPieces() + " x "
				+ getGameSettings().getPieces());
		button.invalidate();
	}

	@Override
	public boolean onPreDraw() {
		try {
			loadImageFromUri();
		} catch (Exception e) {
			getUtils().handleError(e);
		}
		return true;
	}

	private void loadImageFromUri() throws Exception {
		if (!m_loadImageRequired) {
			return;
		}
		if (getGameSettings().getImage() == null) {
			return;
		}
		m_loadImageRequired = false;

		resizePreview();

		ImageView imageView = (ImageView) m_topView
				.findViewById(R.id.imagePreview);
		Bitmap bitmap = getUtils().decodeSampledBitmapFromUri(
				getGameSettings().getImageAsUri(), imageView.getWidth(),
				imageView.getHeight());

		imageView.setImageBitmap(bitmap);

		Button button = (Button) m_topView.findViewById(R.id.btnStart);
		button.setEnabled(true);
	}

	private void resizePreview() {
		ImageView imageView = (ImageView) m_topView
				.findViewById(R.id.imagePreview);
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

	@Override
	public void restoreInstanceState(Bundle savedInstanceState) {
		if (getGameSettings().getImage() != null) {
			m_loadImageRequired = true;
		}
	}

	@Override
	public void onClick(View view) {
		if (m_inAction) {
			return;
		}
		m_inAction = true;
		try {
			handleClick(view);
		} catch (Exception e) {
			getUtils().handleError(e);
		} finally {
			m_inAction = false;
		}
	}

	private void handleClick(View view) throws Exception {

		getUtils().playSound(R.raw.click);

		switch (view.getId()) {
		case R.id.btnCamera:
			getPictureFromCamera();
			break;
		case R.id.btnGallery:
			getPictureFromGallery();
			break;
		case R.id.btnDownload:
			getMainActivity().setFragmentDownload();
			break;
		case R.id.btnStart:
			startPuzzle();
			break;
		case R.id.btnPieces:
			setPieces();
			break;
		}
	}

	private void getPictureFromGallery() {
		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		photoPickerIntent.setType("image/*");
		startActivityForResult(photoPickerIntent, SELECT_PHOTO);
	}

	private void getPictureFromCamera() throws Exception {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (takePictureIntent
				.resolveActivity(getActivity().getPackageManager()) == null) {
			return;
		}
		createImageFile();
		takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, m_cameraOutputUri);
		startActivityForResult(takePictureIntent, TAKE_PHOTO);
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

	private void setPieces() {
		getMainActivity().setFragmentPieces();
	}

	private void startPuzzle() {

		getUtils().playSound(R.raw.start);

		getMainActivity().setFragmentPuzzle();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		try {
			onActivityResultWorker(requestCode, resultCode, data);
		} catch (Exception e) {
			getUtils().handleError(e);
		}
	}

	private void onActivityResultWorker(int requestCode, int resultCode,
			Intent intentData) throws Exception {
		super.onActivityResult(requestCode, resultCode, intentData);

		if (resultCode != Activity.RESULT_OK) {
			return;
		}

		switch (requestCode) {

		case SELECT_PHOTO:
			getGameSettings().setImage(intentData.getData());
			m_loadImageRequired = true;
			break;

		case TAKE_PHOTO:
			saveToGallery();
			getGameSettings().setImage(m_cameraOutputUri);
			m_loadImageRequired = true;
			break;

		}
	}

	private void saveToGallery() {
		Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		intent.setData(m_cameraOutputUri);
		getActivity().sendBroadcast(intent);
	}
}
