package com.alon.android.puzzle.fragments;

import java.io.File;
import java.io.FileOutputStream;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
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

import com.alon.android.puzzle.GoogleDriveHandler;
import com.alon.android.puzzle.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.plus.PlusShare;

public class FragmentNewGame extends FragmentBase implements OnPreDrawListener,
		OnClickListener {

	private static final int SEND_WIDTH = 1024;
	private static final int SEND_HEIGHT = 1024;

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
		File file = getUtils().getNewImage();
		m_cameraOutputUri = Uri.fromFile(file);
		takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, m_cameraOutputUri);
		startActivityForResult(takePictureIntent, TAKE_PHOTO);
	}

	private void startPuzzle() throws Exception {
		if (getMainActivity().isSendToFriend()) {
			getUtils().playSound(R.raw.click);
			sendPuzzle();
		} else {
			getUtils().playSound(R.raw.start);
			getMainActivity().setFragmentPuzzle(null);
		}
	}

	@SuppressWarnings("unused")
	private void sendPuzzle() throws Exception {

		Uri scrabbled = scrabble();
		new GoogleDriveHandler(this).createFile(scrabbled);

		if (false) {
			descrabble(scrabbled);
			share();
		}
	}

	private Uri scrabble() throws Exception {

		Bitmap source = getUtils().decodeSampledBitmapFromUri(
				getGameSettings().getImageAsUri(), SEND_WIDTH, SEND_HEIGHT);
		Bitmap target = source.copy(Bitmap.Config.ARGB_8888, true);
		Canvas canvas = new Canvas(target);
		scrabble(source, canvas);
		File puzzledFile = getUtils().getNewImage();
		FileOutputStream out = new FileOutputStream(puzzledFile.getPath());
		target.compress(Bitmap.CompressFormat.PNG, 100, out);
		out.flush();
		out.close();
		return Uri.fromFile(puzzledFile);
	}

	private Uri descrabble(Uri scrabbled) throws Exception {
		Bitmap source = getUtils().decodeSampledBitmapFromUri(scrabbled,
				SEND_WIDTH, SEND_HEIGHT);
		Bitmap target = source.copy(Bitmap.Config.ARGB_8888, true);
		Canvas canvas = new Canvas(target);
		descrabble(source, canvas);
		File depuzzledFile = getUtils().getNewImage();
		FileOutputStream out = new FileOutputStream(depuzzledFile.getPath());
		target.compress(Bitmap.CompressFormat.JPEG, 100, out);
		out.flush();
		out.close();
		return Uri.fromFile(depuzzledFile);
	}

	private void scrabble(Bitmap source, Canvas canvas) {
		int width = source.getWidth();
		int height = source.getHeight();
		int midWidth = width / 2;
		int midHeight = height / 2;
		scrabble(source, canvas, 0, midWidth, 0, midHeight);
		scrabble(source, canvas, 0, midWidth, midHeight, height);
		scrabble(source, canvas, midWidth, width, 0, midHeight);
		scrabble(source, canvas, midWidth, width, midHeight, height);
	}

	private void descrabble(Bitmap source, Canvas canvas) {
		int width = source.getWidth();
		int height = source.getHeight();
		int midWidth = width / 2;
		int midHeight = height / 2;
		descrabble(source, canvas, 0, midWidth, 0, midHeight);
		descrabble(source, canvas, 0, midWidth, midHeight, height);
		descrabble(source, canvas, midWidth, width, 0, midHeight);
		descrabble(source, canvas, midWidth, width, midHeight, height);
	}

	private void scrabble(Bitmap source, Canvas canvas, int startX, int endX,
			int startY, int endY) {
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		int blockHeight = endY - startY;

		int sequence = 0;
		for (int x = startX; x < endX; x++) {
			for (int y = startY; y < endY; y++) {

				int color = source.getPixel(x, y);
				int a = Color.alpha(color);
				int r = scrabbleColor(Color.red(color), sequence);
				int g = scrabbleColor(Color.green(color), sequence);
				int b = scrabbleColor(Color.blue(color), sequence);
				int newColor = Color.argb(a, r, g, b);
				paint.setColor(newColor);

				int targetX = x;
				int targetY = startY + (y + x) % blockHeight;
				if ((targetX == 0) && (targetY == 0)) {
					System.out.println(newColor);
				}
				canvas.drawPoint(targetX, targetY, paint);
				sequence++;
			}
		}
	}

	private void descrabble(Bitmap source, Canvas canvas, int startX, int endX,
			int startY, int endY) {
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		int blockHeight = endY - startY;

		int sequence = 0;
		for (int x = startX; x < endX; x++) {
			for (int y = startY; y < endY; y++) {

				int sourceX = x;
				int sourceY = startY + (y + x) % blockHeight;

				int color = source.getPixel(sourceX, sourceY);
				if ((x == 0) && (y == 0)) {
					System.out.println(color);
				}

				int a = Color.alpha(color);
				int r = descrabbleColor(Color.red(color), sequence);
				int g = descrabbleColor(Color.green(color), sequence);
				int b = descrabbleColor(Color.blue(color), sequence);
				int newColor = Color.argb(a, r, g, b);
				paint.setColor(newColor);

				canvas.drawPoint(x, y, paint);
				sequence++;
			}
		}
	}

	private int scrabbleColor(int color, int sequence) {
		int result = (color + sequence) % 256;
		return result;
	}

	private int descrabbleColor(int color, int sequence) {
		int result = (color - sequence) % 256;
		if (result < 0) {
			result += 256;
		}
		return result;
	}

	private void share() {
		if (!isGooglePlusInstalled()) {
			getUtils().message(
					"Download and install Google+ application to share puzzle");
			return;
		}

		PlusShare.Builder share = new PlusShare.Builder(getMainActivity());
		share.setText("Use an Android device to solve my puzzle");
		share.setType("text/plain");
		// url on the web
		share.setContentUrl(Uri
				.parse("http://play.google.com/store/apps/details?id=com.alon.android.puzzle"));
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
