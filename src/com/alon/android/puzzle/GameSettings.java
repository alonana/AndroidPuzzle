package com.alon.android.puzzle;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;

public class GameSettings {

	private static final String SETTING_IMAGE = "image";
	private static final String SETTING_PIECES = "pieces";
	private static final String SETTING_EULA = "eula";
	private static final String SETTING_SCORE = "score";

	private transient Context m_context;

	private String m_image;
	private int m_pieces;
	private int m_score;
	private boolean m_eulaAccepted;

	public GameSettings(Context activity) {
		m_context = activity;
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

	public int addScore(int score) {
		m_score += score;
		save();
		return score;
	}

	public boolean isEulaAccepted() {
		return m_eulaAccepted;
	}

	public void setEulaAccepted(boolean accepted) {
		m_eulaAccepted = accepted;
		save();
	}

	public void load() {
		String key = m_context.getString(R.string.preference_file_key);
		SharedPreferences preferences = m_context.getSharedPreferences(key,
				Activity.MODE_PRIVATE);

		m_image = preferences.getString(SETTING_IMAGE, null);
		m_pieces = preferences.getInt(SETTING_PIECES, 2);
		m_score = preferences.getInt(SETTING_SCORE, 0);
		String eulaAccepted = preferences.getString(SETTING_EULA, "false");
		m_eulaAccepted = Boolean.parseBoolean(eulaAccepted);
	}

	private void save() {
		String key = m_context.getString(R.string.preference_file_key);
		SharedPreferences preferences = m_context.getSharedPreferences(key,
				Activity.MODE_PRIVATE);
		Editor editor = preferences.edit();

		editor.putString(SETTING_IMAGE, m_image);
		editor.putString(SETTING_EULA, Boolean.toString(m_eulaAccepted));
		editor.putInt(SETTING_PIECES, m_pieces);
		editor.putInt(SETTING_SCORE, m_score);

		editor.commit();
	}

}
