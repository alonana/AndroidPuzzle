package com.alon.android.puzzle;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {

	private static final int NEW_GAME = 100;

	private Utils m_utils;
	private GameSettings m_settings;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		m_utils = new Utils(this);
		m_utils.setFullScreen();
		m_utils.loadSound(R.raw.click);
		setContentView(R.layout.activity_main);
		m_settings = new GameSettings(this);
		setScoresText();
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

		if (resultCode != RESULT_OK) {
			return;
		}

		switch (requestCode) {

		case NEW_GAME:
			m_settings.load();
			setScoresText();
			break;
		}
	}

}
