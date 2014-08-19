package com.alon.android.puzzle.fragments;

import java.util.HashSet;

import android.app.Dialog;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alon.android.puzzle.R;
import com.google.android.gms.plus.PlusOneButton;

public class FragmentMain extends FragmentBase implements OnClickListener {

	private static HashSet<String> m_notifiedInvitations = new HashSet<String>();

	private View m_topView;
	private PlusOneButton m_plusOneButton;

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

	private View onCreateViewWorker(LayoutInflater inflater, ViewGroup container)
			throws Exception {
		getUtils().loadSound(R.raw.click);

		m_topView = inflater.inflate(R.layout.fragment_main, container, false);

		setScoresText();

		m_topView.findViewById(R.id.sign_in_button).setOnClickListener(this);
		m_topView.findViewById(R.id.sign_out_button).setOnClickListener(this);
		m_topView.findViewById(R.id.btnNewGame).setOnClickListener(this);
		m_topView.findViewById(R.id.btnNewNetworkGame).setOnClickListener(this);
		m_topView.findViewById(R.id.btnWall).setOnClickListener(this);

		m_plusOneButton = (PlusOneButton) m_topView
				.findViewById(R.id.btnGooglePlusOne);

		updateButtons();

		try {
			showEula();
		} catch (Exception e) {
			getUtils().handleError(e);
		}

		getMainActivity().reloadInvitations();
		return m_topView;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (m_plusOneButton != null) {
			// m_plusOneButton.initialize(URL, PLUS_ONE_REQUEST_CODE);
		}
	}

	private void showEula() throws Exception {
		if (getGameSettings().isEulaAccepted()) {
			return;
		}

		final Dialog dialog = new Dialog(getMainActivity());
		dialog.setContentView(R.layout.dialog_eula);
		dialog.setTitle("License Agreement");
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
						try {
							getMainActivity().finish();
						} catch (Exception e) {
							getUtils().handleError(e);
						}
					}
				});

		dialog.show();
	}

	@Override
	public void onClick(View view) {
		try {
			onClickWorker(view);
		} catch (Exception e) {
			getUtils().handleError(e);
		}

	}

	private void onClickWorker(View view) throws Exception {
		switch (view.getId()) {
		case R.id.btnNewGame:
			getUtils().playSound(R.raw.click);
			getMainActivity().setFragmentNewGame();
			break;
		case R.id.btnNewNetworkGame:
			getUtils().playSound(R.raw.click);
			getMainActivity().setFragmentNetworkGame();
			break;
		case R.id.sign_in_button:
			getUtils().playSound(R.raw.click);
			getMainActivity().beginUserInitiatedSignIn();
			break;
		case R.id.sign_out_button:
			getUtils().playSound(R.raw.click);
			getMainActivity().signOut();
			updateButtons();
			break;
		case R.id.btnWall:
			getUtils().playSound(R.raw.click);
			getMainActivity().setFragmentWall();
			break;
		}
	}

	private void setScoresText() {
		TextView text = (TextView) m_topView.findViewById(R.id.txtScore);
		text.setText(Integer.toString(getGameSettings().getScore()));
		text.invalidate();
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
		m_topView.findViewById(R.id.btnNewNetworkGame).setVisibility(postSign);
		// TODO: handle g+1
		// m_topView.findViewById(R.id.btnGooglePlusOne).setVisibility(postSign);
	}

	@Override
	public void updateInvitations() throws Exception {
		if (getGameSettings().getInvitations().size() == 0) {
			return;
		}

		boolean newInvitation = false;
		for (String id : getGameSettings().getInvitations()) {
			if (!m_notifiedInvitations.contains(id)) {
				m_notifiedInvitations.add(id);
				newInvitation = true;
			}
		}

		if (!newInvitation) {
			return;
		}

		String message = "You have pending invitations.\nClick on 'Play with Others' to view the invitation.";
		getUtils().message(message);
	}

}
