package com.alon.android.puzzle.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.alon.android.puzzle.ActivityDeveloper;
import com.alon.android.puzzle.ActivityMain;
import com.alon.android.puzzle.R;
import com.google.android.gms.games.Games;

public class FragmentWall extends FragmentBase implements OnClickListener {

	private static final int REQUEST_LEADERBOARD = 101;
	private static final int REQUEST_ACHIEVEMENTS = 102;
	private View m_topView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		try {
			return onCreateViewWorker(inflater, container);
		} catch (Exception e) {
			getUtils().handleError(e);
			return null;
		}
	}

	public View onCreateViewWorker(LayoutInflater inflater, ViewGroup container)
			throws Exception {
		getUtils().loadSound(R.raw.click);

		m_topView = inflater.inflate(R.layout.fragment_wall, container, false);

		m_topView.findViewById(R.id.btnLeaders).setOnClickListener(this);
		m_topView.findViewById(R.id.btnAchievements).setOnClickListener(this);
		m_topView.findViewById(R.id.btnCredits).setOnClickListener(this);
		m_topView.findViewById(R.id.btnLog).setOnClickListener(this);

		Button buttonLog = (Button) m_topView.findViewById(R.id.btnLog);
		buttonLog.setOnClickListener(this);
		if (ActivityMain.INTERNAL_LOGS) {
			buttonLog.setVisibility(View.VISIBLE);
		}

		updateButtons();

		return m_topView;
	}

	@Override
	public void onSignInFailed() {
		try {
			updateButtons();
		} catch (Exception e) {
			getUtils().handleError(e);
		}
	}

	@Override
	public void onSignInSucceeded() {
		try {
			updateButtons();
		} catch (Exception e) {
			getUtils().handleError(e);
		}
	}

	public void updateButtons() throws Exception {
		int postSign;
		if (getMainActivity().isSignedIn()) {
			postSign = View.VISIBLE;
		} else {
			postSign = View.GONE;
		}

		m_topView.findViewById(R.id.btnLeaders).setVisibility(postSign);
		m_topView.findViewById(R.id.btnAchievements).setVisibility(postSign);
	}

	@Override
	public void onClick(View view) {
		try {
			onClickWorker(view);
		} catch (Exception e) {
			getUtils().handleError(e);
		}
	}

	public void onClickWorker(View view) throws Exception {

		switch (view.getId()) {
		case R.id.btnLeaders:
			getUtils().playSound(R.raw.click);
			showLeaders();
			break;
		case R.id.btnAchievements:
			getUtils().playSound(R.raw.click);
			showAchievements();
			break;
		case R.id.btnCredits:
			getUtils().playSound(R.raw.click);
			getMainActivity().setFragmentCredits();
			break;
		case R.id.btnLog:
			getUtils().playSound(R.raw.click);
			startLogActivity();
			break;
		}

	}

	public void startLogActivity() {
		Intent intent = new Intent(getMainActivity(), ActivityDeveloper.class);
		startActivity(intent);
	}

	public void showAchievements() throws Exception {
		Intent intent = Games.Achievements
				.getAchievementsIntent(getApiClient());
		startActivityForResult(intent, REQUEST_ACHIEVEMENTS);
	}

	public void showLeaders() throws Exception {
		String boardId = getString(R.string.leaderboard_id);
		Intent intent = Games.Leaderboards.getLeaderboardIntent(getApiClient(),
				boardId);
		startActivityForResult(intent, REQUEST_LEADERBOARD);
	}

}
