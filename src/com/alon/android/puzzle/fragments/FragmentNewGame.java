package com.alon.android.puzzle.fragments;

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

import com.alon.android.puzzle.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.plus.PlusShare;

public class FragmentNewGame extends FragmentBase implements OnPreDrawListener,
		OnClickListener {

	private static final int SELECT_PHOTO = 100;
	private static final int TAKE_PHOTO = 101;
	private static final int START_SHARE = 102;

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
		getUtils().setPiecesButtonText(m_topView, R.id.btnPieces);
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

		Button buttonStart = (Button) m_topView.findViewById(R.id.btnStart);
		int id;
		if (getMainActivity().isSendToFriend()) {
			id = R.string.send;
		} else {
			id = R.string.start;
		}
		buttonStart.setText(getString(id));
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
		if (bitmap == null) {
			getUtils().message(
					"Unable to load image " + getGameSettings().getImage());
			button.setEnabled(false);
		} else {
			button.setEnabled(true);
		}
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
		switch (view.getId()) {
		case R.id.btnCamera:
			getUtils().playSound(R.raw.click);
			getPictureFromCamera();
			break;
		case R.id.btnGallery:
			getUtils().playSound(R.raw.click);
			getPictureFromGallery();
			break;
		case R.id.btnDownload:
			getUtils().playSound(R.raw.click);
			getMainActivity().setFragmentDownload();
			break;
		case R.id.btnStart:
			// not using the default click
			// getUtils().playSound(R.raw.click);
			startPuzzle();
			break;
		case R.id.btnPieces:
			getUtils().playSound(R.raw.click);
			getMainActivity().setFragmentPieces(false);
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

	private void startPuzzle() {
		if (getMainActivity().isSendToFriend()) {
			getUtils().playSound(R.raw.click);
			share();
		} else {
			getUtils().playSound(R.raw.start);
			getMainActivity().setFragmentPuzzle(null);
		}
	}

	private void share() {

		if (!isGooglePlusInstalled()) {
			getUtils().message(
					"Download and install Google+ application to share puzzle");
			return;
		}

		PlusShare.Builder share = new PlusShare.Builder(getMainActivity());
		share.setText("Use Android device to solve my puzzle");
		share.setType("text/plain");
		// url on the web
		share.setContentUrl(Uri
				.parse("puzzleme://play.google.com/store/apps/details?id=com.alon.android.puzzle&a=2"));
		// actual deep link
		share.setContentDeepLinkId("puzzleme://play.google.com/store/apps/details?id=com.alon.android.puzzle&a=3");

		// Uri selectedImage = getGameSettings().getImageAsUri();
		// ContentResolver resolver = getMainActivity().getContentResolver();
		// String mime = resolver.getType(selectedImage);
		// share.addStream(selectedImage).getIntent();

		startActivityForResult(share.getIntent(), START_SHARE);
	}

	private boolean isGooglePlusInstalled() {
		int errorCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(getMainActivity());
		if (errorCode != ConnectionResult.SUCCESS) {
			return false;
		}

		try {
			getMainActivity().getPackageManager().getApplicationInfo(
					"com.google.android.apps.plus", 0);
			return true;
		} catch (PackageManager.NameNotFoundException e) {
			return false;
		}
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
