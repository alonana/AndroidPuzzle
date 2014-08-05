package com.alon.android.puzzle;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.games.Games;

public class FragmentMain extends FragmentBase implements OnClickListener {

	private static final int REQUEST_LEADERBOARD = 101;

	private View m_topView;

	public FragmentMain(MainActivity activity) {
		super(activity);
	}

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

		updateButtons();
		return m_topView;
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.btnLeaders:
			showLeaders();
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
		}
	}

	private void setScoresText() {
		TextView text = (TextView) m_topView.findViewById(R.id.txtScore);
		text.setText(Integer.toString(getGameSettings().getScore()));
		text.invalidate();
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
	}

}
