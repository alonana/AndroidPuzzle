package com.alon.android.puzzle;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.example.games.basegameutils.BaseGameActivity;

public class MainActivity extends BaseGameActivity implements
		View.OnClickListener {

	private static final int NEW_GAME = 100;

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
			break;
		}
	}

	private void updateButtons(boolean isSignedIn) {
		if (isSignedIn) {
			findViewById(R.id.textSignDescription).setVisibility(View.GONE);
			findViewById(R.id.sign_in_button).setVisibility(View.GONE);
			findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.textSignDescription).setVisibility(View.VISIBLE);
			findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
			findViewById(R.id.sign_out_button).setVisibility(View.GONE);
		}
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
