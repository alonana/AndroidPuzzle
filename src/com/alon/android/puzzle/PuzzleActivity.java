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
		m_view = new PuzzleView(this, m_utils);
		setContentView(m_view);
		ViewTreeObserver observer = m_view.getViewTreeObserver();
		if (observer.isAlive()) {
			observer.addOnGlobalLayoutListener(this);
		}
	}

	@Override
	public void onGlobalLayout() {
		Intent intent = getIntent();
		int sidePieces = intent.getIntExtra(SIDE_PIECES, 2);
		String uriData = intent.getStringExtra(URI);
		Uri uri = Uri.parse(uriData);
		Bitmap image = m_utils.decodeSampledBitmapFromUri(uri,
				m_view.getWidth(), m_view.getHeight());
		m_view.setImage(image, sidePieces);
	}
}
