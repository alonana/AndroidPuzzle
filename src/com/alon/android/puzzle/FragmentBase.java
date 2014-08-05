package com.alon.android.puzzle;

import android.app.Fragment;
import android.os.Bundle;

abstract public class FragmentBase extends Fragment {

	private MainActivity m_mainActivity;
	private Utils m_utils;
	private GameSettings m_settings;

	public FragmentBase(MainActivity activity) {
		m_mainActivity = activity;
	}

	public MainActivity getMainActivity() {
		return m_mainActivity;
	}

	public Utils getUtils() {
		if (m_utils == null) {
			m_utils = new Utils(getActivity());
		}
		return m_utils;
	}

	public GameSettings getGameSettings() {
		if (m_settings == null) {
			m_settings = new GameSettings(getActivity());
		}
		return m_settings;
	}

	public void setGameSettings(GameSettings settings) {
		m_settings = settings;
	}

	public void saveInstanceState(Bundle outState) {

	}

	public void restoreInstanceState(Bundle savedInstanceState) {

	}

}
