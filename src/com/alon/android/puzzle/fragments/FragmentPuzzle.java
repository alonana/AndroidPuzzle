package com.alon.android.puzzle.fragments;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

import com.alon.android.puzzle.play.PuzzleView;

public class FragmentPuzzle extends FragmentBase implements
		OnGlobalLayoutListener {

	private PuzzleView m_view;
	private FragmentNetworkGame m_network;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		m_view = new PuzzleView(this, getUtils(), savedInstanceState, m_network);

		ViewTreeObserver observer = m_view.getViewTreeObserver();
		if (observer.isAlive()) {
			observer.addOnGlobalLayoutListener(this);
		}

		return m_view;
	}

	@Override
	public void saveInstanceState(Bundle outState) {
		m_view.saveInstanceState(outState);
	}

	@Override
	public void restoreInstanceState(Bundle savedInstanceState) {
		m_view.restoreInstanceState(savedInstanceState);
	}

	@Override
	public void onGlobalLayout() {
		try {
			onGlobalLayoutWorker();
		} catch (Exception e) {
			getUtils().handleError(e);
		}
	}

	private void onGlobalLayoutWorker() throws Exception {
		int sidePieces = getGameSettings().getPieces();
		String uriData = getGameSettings().getImage();
		if (uriData == null) {
			return;
		}
		Uri uri = Uri.parse(uriData);
		Bitmap image = getUtils().decodeSampledBitmapFromUri(uri,
				m_view.getWidth(), m_view.getHeight());
		m_view.setImage(image, sidePieces);
	}

	public void setNetwork(FragmentNetworkGame networkGame) {
		m_network = networkGame;
	}

	@Override
	public void cleanup() {
		if (m_network != null) {
			m_network.leaveRoom(false);
		}
	}
}
