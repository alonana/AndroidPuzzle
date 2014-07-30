package com.example.alon1;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;

public class PuzzleActivity extends Activity implements OnGlobalLayoutListener {

	public static final String URI = "uri";

	private Utils m_utils;
	private PuzzleView m_view;

	public PuzzleActivity() {
		m_utils = new Utils(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (Build.VERSION.SDK_INT < 16) {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		} else {
			View decorView = getWindow().getDecorView();
			// Hide the status bar.
			int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
			decorView.setSystemUiVisibility(uiOptions);
			// Remember that you should never show the action bar if the
			// status bar is hidden, so hide that too if necessary.
			ActionBar actionBar = getActionBar();
			actionBar.hide();
		}

		m_view = new PuzzleView(this);
		setContentView(m_view);
		ViewTreeObserver observer = m_view.getViewTreeObserver();
		if (observer.isAlive()) {
			observer.addOnGlobalLayoutListener(this);
		}
	}

	@Override
	public void onGlobalLayout() {
		Intent intent = getIntent();
		String uriData = intent.getStringExtra(URI);
		Uri uri = Uri.parse(uriData);
		Bitmap image = m_utils.decodeSampledBitmapFromUri(uri,
				m_view.getWidth(), m_view.getHeight());
		m_view.setImage(image, 2);
	}
}
