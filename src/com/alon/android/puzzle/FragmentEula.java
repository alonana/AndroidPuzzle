package com.alon.android.puzzle;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class FragmentEula extends FragmentBase implements OnClickListener {

	private View m_topView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		getUtils().loadSound(R.raw.click);

		m_topView = inflater.inflate(R.layout.dialog_eula, container, false);

		m_topView.findViewById(R.id.btnAccept).setOnClickListener(this);
		m_topView.findViewById(R.id.btnDecline).setOnClickListener(this);

		if (getGameSettings().isEulaAccepted()) {
			getMainActivity().setFragmentMain();
		}

		return m_topView;
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.btnAccept:
			getGameSettings().setEulaAccepted(true);
			getMainActivity().setFragmentMain();
			break;
		case R.id.btnDecline:
			getMainActivity().finish();
			break;
		}
	}

}
