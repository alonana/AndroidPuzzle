package com.alon.android.puzzle;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.example.games.basegameutils.BaseGameActivity;

public class MainActivity extends BaseGameActivity {

	private static final String SAVED_ACTIVITY = MainActivity.class
			.getSimpleName() + "activity";

	private Utils m_utils;
	private FragmentMain m_fragmentMain;
	private FragmentBase m_activeFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		m_utils = new Utils(this);
		m_utils.setFullScreen();

		super.onCreate(savedInstanceState);

		setRequiredFragment(savedInstanceState);

		setContentView(R.layout.activity_main);
	}

	private void setRequiredFragment(Bundle savedInstanceState) {
		if (savedInstanceState == null) {
			setFragmentMain();
			return;
		}

		String savedFragment = savedInstanceState.getString(SAVED_ACTIVITY);
		if (savedFragment == null) {
			setFragmentMain();
			return;
		}

		if (savedFragment.equals(FragmentNewGame.class.getSimpleName())) {
			setFragmentNewGame();
			return;
		}
		if (savedFragment.equals(FragmentPieces.class.getSimpleName())) {
			setFragmentPieces();
			return;
		}
		if (savedFragment.equals(FragmentPuzzle.class.getSimpleName())) {
			setFragmentPuzzle();
			return;
		}
		setFragmentMain();
	}

	private void setFragment(FragmentBase fragment) {
		m_activeFragment = fragment;

		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		fragmentTransaction.replace(R.id.top, fragment);
		fragmentTransaction.commit();
	}

	public void setFragmentMain() {
		m_fragmentMain = new FragmentMain();
		setFragment(m_fragmentMain);
	}

	public void setFragmentNewGame() {
		FragmentNewGame fragment = new FragmentNewGame();
		setFragment(fragment);
	}

	public void setFragmentDownload() {
		FragmentDownload fragment = new FragmentDownload();
		setFragment(fragment);
	}

	public void setFragmentPieces() {
		FragmentPieces fragment = new FragmentPieces();
		setFragment(fragment);
	}

	public void setFragmentPuzzle() {
		FragmentPuzzle fragment = new FragmentPuzzle();
		setFragment(fragment);
	}

	public void setFragmentCredits() {
		FragmentCredits fragment = new FragmentCredits();
		setFragment(fragment);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {

		try {
			outState.putString(SAVED_ACTIVITY, m_activeFragment.getClass()
					.getSimpleName());
			m_activeFragment.saveInstanceState(outState);
		} catch (Exception e) {
			m_utils.handleError(e);
		}

		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {

		super.onRestoreInstanceState(savedInstanceState);

		try {
			m_activeFragment.restoreInstanceState(savedInstanceState);
		} catch (Exception e) {
			m_utils.handleError(e);
		}
	}

	@Override
	public void onBackPressed() {
		if (m_activeFragment instanceof FragmentPieces) {
			setFragmentNewGame();
		} else if (m_activeFragment instanceof FragmentPuzzle) {
			setFragmentMain();
		} else if (m_activeFragment instanceof FragmentNewGame) {
			setFragmentMain();
		} else if (m_activeFragment instanceof FragmentCredits) {
			setFragmentMain();
		} else if (m_activeFragment instanceof FragmentDownload) {
			setFragmentNewGame();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public void beginUserInitiatedSignIn() {
		super.beginUserInitiatedSignIn();
	}

	@Override
	public boolean isSignedIn() {
		return super.isSignedIn();
	}

	@Override
	public void signOut() {
		super.signOut();
	}

	@Override
	public GoogleApiClient getApiClient() {
		return super.getApiClient();
	}

	@Override
	public void onSignInFailed() {
		m_fragmentMain.updateButtons();
	}

	@Override
	public void onSignInSucceeded() {
		m_fragmentMain.updateButtons();
	}

}
