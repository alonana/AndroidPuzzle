package com.alon.android.puzzle;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class FragmentCredits extends FragmentBase implements OnClickListener {

	private View m_topView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		getUtils().loadSound(R.raw.click);

		m_topView = inflater.inflate(R.layout.fragment_credits, container,
				false);

		m_topView.findViewById(R.id.btnCreditsOk).setOnClickListener(this);

		TextView text = (TextView) m_topView.findViewById(R.id.textCredits);
		try {
			text.setText(getUtils().getResourceText(R.raw.credits));
		} catch (Exception e) {
			getUtils().handleError(e);
		}
		return m_topView;
	}

	@Override
	public void onClick(View view) {

		switch (view.getId()) {
		case R.id.btnCreditsOk:
			getMainActivity().setFragmentMain();
			break;
		}

	}

}
