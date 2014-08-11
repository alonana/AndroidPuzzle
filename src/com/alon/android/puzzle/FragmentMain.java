package com.alon.android.puzzle;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.games.Games;

public class FragmentMain extends FragmentBase implements OnClickListener {

	private static final int REQUEST_LEADERBOARD = 101;
	private static final int REQUEST_ACHIEVEMENTS = 102;

	private View m_topView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		getUtils().loadSound(R.raw.click);

		m_topView = inflater.inflate(R.layout.fragment_main, container, false);

		setScoresText();

		m_topView.findViewById(R.id.sign_in_button).setOnClickListener(this);
		m_topView.findViewById(R.id.sign_out_button).setOnClickListener(this);
		m_topView.findViewById(R.id.btnNewGame).setOnClickListener(this);
		m_topView.findViewById(R.id.btnLeaders).setOnClickListener(this);
		m_topView.findViewById(R.id.btnAchievements).setOnClickListener(this);
		m_topView.findViewById(R.id.btnCredits).setOnClickListener(this);

		updateButtons();

		try {
			showEula();
		} catch (Exception e) {
			getUtils().handleError(e);
		}
		return m_topView;
	}

	private void showEula() throws Exception {
		if (getGameSettings().isEulaAccepted()) {
			return;
		}

		final Dialog dialog = new Dialog(getMainActivity());
		dialog.setContentView(R.layout.dialog_eula);
		dialog.setTitle("end user license agreement");
		TextView text = (TextView) dialog.findViewById(R.id.textEula);
		text.setText(getUtils().getResourceText(R.raw.eula));
		text.setMovementMethod(new ScrollingMovementMethod());

		dialog.findViewById(R.id.btnAccept).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View view) {
						getGameSettings().setEulaAccepted(true);
						dialog.dismiss();
					}
				});
		dialog.findViewById(R.id.btnDecline).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View view) {
						getMainActivity().finish();
					}
				});

		dialog.show();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.btnLeaders:
			showLeaders();
			break;
		case R.id.btnAchievements:
			showAchievements();
			break;
		case R.id.btnNewGame:
			getUtils().playSound(R.raw.click);
			getMainActivity().setFragmentNewGame();
			break;
		case R.id.sign_in_button:
			getMainActivity().beginUserInitiatedSignIn();
			break;
		case R.id.sign_out_button:
			getMainActivity().signOut();
			updateButtons();
			break;
		case R.id.btnCredits:
			getMainActivity().setFragmentCredits();
			break;
		}
	}

	private void setScoresText() {
		TextView text = (TextView) m_topView.findViewById(R.id.txtScore);
		text.setText(Integer.toString(getGameSettings().getScore()));
		text.invalidate();
	}

	public void showAchievements() {
		getUtils().playSound(R.raw.click);
		Intent intent = Games.Achievements
				.getAchievementsIntent(getMainActivity().getApiClient());
		startActivityForResult(intent, REQUEST_ACHIEVEMENTS);
	}

	public void showLeaders() {
		getUtils().playSound(R.raw.click);
		String boardId = getString(R.string.leaderboard_id);
		Intent intent = Games.Leaderboards.getLeaderboardIntent(
				getMainActivity().getApiClient(), boardId);
		startActivityForResult(intent, REQUEST_LEADERBOARD);
	}

	public void updateButtons() {
		int postSign;
		int preSign;
		if (getMainActivity().isSignedIn()) {
			postSign = View.VISIBLE;
			preSign = View.GONE;
		} else {
			postSign = View.GONE;
			preSign = View.VISIBLE;
		}

		m_topView.findViewById(R.id.textSignDescription).setVisibility(preSign);
		m_topView.findViewById(R.id.sign_in_button).setVisibility(preSign);
		m_topView.findViewById(R.id.sign_out_button).setVisibility(postSign);
		m_topView.findViewById(R.id.btnLeaders).setVisibility(postSign);
		m_topView.findViewById(R.id.btnAchievements).setVisibility(postSign);
	}

}
