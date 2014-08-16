package com.alon.android.puzzle;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;

import com.alon.android.puzzle.fragments.FragmentBase;
import com.alon.android.puzzle.fragments.FragmentCredits;
import com.alon.android.puzzle.fragments.FragmentDownload;
import com.alon.android.puzzle.fragments.FragmentMain;
import com.alon.android.puzzle.fragments.FragmentNetworkGame;
import com.alon.android.puzzle.fragments.FragmentNewGame;
import com.alon.android.puzzle.fragments.FragmentPieces;
import com.alon.android.puzzle.fragments.FragmentPuzzle;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.example.games.basegameutils.BaseGameActivity;

public class ActivityMain extends BaseGameActivity implements
		OnInvitationReceivedListener {

	public static final boolean INTERNAL_LOGS = true;

	private Utils m_utils;
	private FragmentBase m_activeFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		m_utils = new Utils(this);
		m_utils.setFullScreen();

		m_utils.debug("\n=================\napplication starting\n=================\n");

		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			setFragmentMain();
		}

		setContentView(R.layout.activity_main);
	}

	private void setFragment(FragmentBase fragment) {
		if (m_activeFragment != null) {
			m_activeFragment.cleanup();
		}
		m_activeFragment = fragment;

		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		fragmentTransaction.replace(R.id.top, fragment);
		fragmentTransaction.commitAllowingStateLoss();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {

		try {
			m_activeFragment.saveInstanceState(outState);
		} catch (Exception e) {
			m_utils.handleError(e);
		}

		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {

		super.onRestoreInstanceState(savedInstanceState);

		m_activeFragment = (FragmentBase) getFragmentManager()
				.findFragmentById(R.id.top);
		try {
			m_activeFragment.restoreInstanceState(savedInstanceState);
		} catch (Exception e) {
			m_utils.handleError(e);
		}
	}

	public void setFragmentMain() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		FragmentMain fragment = new FragmentMain();
		setFragment(fragment);
	}

	public void setFragmentNewGame() {
		FragmentNewGame fragment = new FragmentNewGame();
		setFragment(fragment);
	}

	public void setFragmentNetworkGame() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

		FragmentNetworkGame fragment = new FragmentNetworkGame();
		setFragment(fragment);
	}

	public void setFragmentDownload() {
		FragmentDownload fragment = new FragmentDownload();
		setFragment(fragment);
	}

	public void setFragmentPieces(boolean isNetwork) {
		FragmentPieces fragment = new FragmentPieces();
		fragment.setNetwork(isNetwork);
		setFragment(fragment);
	}

	public void setFragmentPuzzle(FragmentNetworkGame networkGame) {
		FragmentPuzzle fragment = new FragmentPuzzle();
		fragment.setNetwork(networkGame);
		setFragment(fragment);
	}

	public void setFragmentCredits() {
		FragmentCredits fragment = new FragmentCredits();
		setFragment(fragment);
	}

	// all: main, newGame, newNetworkGame, download, pieces, credits, puzzle
	@Override
	public void onBackPressed() {
		if (m_activeFragment instanceof FragmentPieces) {
			setFragmentNewGame();
		} else if (m_activeFragment instanceof FragmentPuzzle) {
			setFragmentMain();
		} else if (m_activeFragment instanceof FragmentNewGame) {
			setFragmentMain();
		} else if (m_activeFragment instanceof FragmentNetworkGame) {
			setFragmentMain();
		} else if (m_activeFragment instanceof FragmentCredits) {
			setFragmentMain();
		} else if (m_activeFragment instanceof FragmentDownload) {
			setFragmentNewGame();
		} else {
			super.onBackPressed();
		}
	}

	// expose
	@Override
	public void beginUserInitiatedSignIn() {
		super.beginUserInitiatedSignIn();
	}

	// expose
	@Override
	public boolean isSignedIn() {
		return super.isSignedIn();
	}

	// expose
	@Override
	public void signOut() {
		super.signOut();
	}

	// expose
	@Override
	public GoogleApiClient getApiClient() {
		return super.getApiClient();
	}

	@Override
	public void onSignInFailed() {
		m_activeFragment.onSignInFailed();
	}

	@Override
	public void onSignInSucceeded() {
		View view = findViewById(R.id.top);
		Games.setViewForPopups(getApiClient(), view);
		m_activeFragment.onSignInSucceeded();
		Games.Invitations.registerInvitationListener(getApiClient(), this);
	}

	@Override
	public void onInvitationReceived(Invitation invitation) {
		try {
			GameSettings settings = new GameSettings(this);
			settings.getInvitations().add(invitation.getInvitationId());
			settings.save();
			m_activeFragment.updateInvitations();
		} catch (Exception e) {
			m_utils.handleError(e);
		}
	}

	@Override
	public void onInvitationRemoved(String id) {
		try {
			GameSettings settings = new GameSettings(this);
			settings.getInvitations().remove(id);
			settings.save();
			m_activeFragment.updateInvitations();
		} catch (Exception e) {
			m_utils.handleError(e);
		}
	}

}
