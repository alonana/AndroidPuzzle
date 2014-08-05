package com.alon.android.puzzle;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameActivity;

public class MainActivity extends BaseGameActivity implements
		View.OnClickListener {

	private static final int NEW_GAME = 100;
	private static final int REQUEST_LEADERBOARD = 101;

	private Utils m_utils;
	private GameSettings m_settings;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		m_utils = new Utils(this);
		m_utils.setFullScreen();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		m_utils.loadSound(R.raw.click);
		m_settings = new GameSettings(this);
		setScoresText();

		findViewById(R.id.sign_in_button).setOnClickListener(this);
		findViewById(R.id.sign_out_button).setOnClickListener(this);
	}

	private void setScoresText() {
		TextView text = (TextView) findViewById(R.id.txtScore);
		text.setText(Integer.toString(m_settings.getScore()));
		text.invalidate();
	}

	public void newGame(View view) {
		m_utils.playSound(R.raw.click);
		Intent intent = new Intent(this, NewGameActivity.class);
		startActivityForResult(intent, NEW_GAME);

	}

	public void showLeaders(View view) {
		m_utils.playSound(R.raw.click);
		String boardId = getString(R.string.leaderboard_id);
		startActivityForResult(Games.Leaderboards.getLeaderboardIntent(
				getApiClient(), boardId), REQUEST_LEADERBOARD);
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

		switch (requestCode) {

		case NEW_GAME:
			m_settings.load();
			setScoresText();
			if (isSignedIn()) {
				String boardId = getString(R.string.leaderboard_id);
				Games.Leaderboards.submitScore(getApiClient(), boardId,
						m_settings.getScore());
			}
			break;
		}
	}

	private void updateButtons(boolean isSignedIn) {

		int requireSign;
		int preSign;
		if (isSignedIn) {
			requireSign = View.VISIBLE;
			preSign = View.GONE;
		} else {
			requireSign = View.GONE;
			preSign = View.VISIBLE;
		}

		findViewById(R.id.textSignDescription).setVisibility(preSign);
		findViewById(R.id.sign_in_button).setVisibility(preSign);
		findViewById(R.id.sign_out_button).setVisibility(requireSign);
		findViewById(R.id.btnLeaders).setVisibility(requireSign);
	}

	@Override
	public void onSignInFailed() {
		updateButtons(false);
	}

	@Override
	public void onSignInSucceeded() {
		updateButtons(true);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.sign_in_button) {
			beginUserInitiatedSignIn();
		} else if (view.getId() == R.id.sign_out_button) {
			signOut();
			updateButtons(false);
		}
	}

}
