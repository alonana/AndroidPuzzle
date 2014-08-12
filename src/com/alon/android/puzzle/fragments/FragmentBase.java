package com.alon.android.puzzle.fragments;

import com.alon.android.puzzle.GameSettings;
import com.alon.android.puzzle.MainActivity;
import com.alon.android.puzzle.Utils;

import android.app.Fragment;
import android.os.Bundle;

abstract public class FragmentBase extends Fragment {

	private MainActivity m_mainActivity;
	private Utils m_utils;
	private GameSettings m_settings;

	public MainActivity getMainActivity() {
		if (m_mainActivity == null) {
			m_mainActivity = (MainActivity) getActivity();
		}
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

	public void saveInstanceState(Bundle outState) {
	}

	public void restoreInstanceState(Bundle savedInstanceState) {
	}

	public void onSignInFailed() {
	}

	public void onSignInSucceeded() {
	}

}
