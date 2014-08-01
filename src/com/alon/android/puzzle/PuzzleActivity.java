package com.alon.android.puzzle;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

public class PuzzleActivity extends Activity implements OnGlobalLayoutListener {

	public static final String URI = "uri";
	public static final String SIDE_PIECES = "side_pieces";

	private Utils m_utils;
	private PuzzleView m_view;

	public PuzzleActivity() {
		m_utils = new Utils(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		m_utils.setFullScreen(this);
		m_view = new PuzzleView(this, m_utils, savedInstanceState);
		setContentView(m_view);
		ViewTreeObserver observer = m_view.getViewTreeObserver();
		if (observer.isAlive()) {
			observer.addOnGlobalLayoutListener(this);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		m_view.saveInstanceState(outState);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onGlobalLayout() {
		try {
			onGlobalLayoutWorker();
		} catch (Exception e) {
			m_utils.handleError(e);
		}
	}

	private void onGlobalLayoutWorker() throws Exception {
		Intent intent = getIntent();
		int sidePieces = intent.getIntExtra(SIDE_PIECES, 2);
		String uriData = intent.getStringExtra(URI);
		Uri uri = Uri.parse(uriData);
		Bitmap image = m_utils.decodeSampledBitmapFromUri(uri,
				m_view.getWidth(), m_view.getHeight());
		m_view.setImage(image, sidePieces);
	}
}
