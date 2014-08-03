package com.alon.android.puzzle;

import java.io.Serializable;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;

public class GameSettings implements Serializable {

	private static final long serialVersionUID = 1835146604040891030L;

	private static final String SETTING_IMAGE = "image";
	private static final String SETTING_PIECES = "pieces";
	private static final String SETTING_SCORE = "score";

	private transient Activity m_activity;

	private String m_image;
	private int m_pieces;
	private int m_score;

	public GameSettings(Activity activity) {
		m_activity = activity;
		load();
	}

	public int getPieces() {
		return m_pieces;
	}

	public int getScore() {
		return m_score;
	}

	public Uri getImageAsUri() {
		return Uri.parse(m_image);
	}

	public String getImage() {
		return m_image;
	}

	public void setImage(Uri uri) {
		m_image = uri.toString();
		save();
	}

	public void setPieces(int amount) {
		m_pieces = amount;
		save();
	}

	public void addScore(int score) {
		m_score += score;
		save();
	}

	public void load() {
		SharedPreferences preferences = m_activity
				.getPreferences(Activity.MODE_PRIVATE);

		m_image = preferences.getString(SETTING_IMAGE, null);
		m_pieces = preferences.getInt(SETTING_PIECES, 2);
		m_score = preferences.getInt(SETTING_SCORE, 0);
	}

	private void save() {
		SharedPreferences preferences = m_activity
				.getPreferences(Activity.MODE_PRIVATE);
		Editor editor = preferences.edit();

		editor.putString(SETTING_IMAGE, m_image);
		editor.putInt(SETTING_PIECES, m_pieces);
		editor.putInt(SETTING_SCORE, m_score);

		editor.commit();
	}
}
