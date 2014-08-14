package com.alon.android.puzzle.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.alon.android.puzzle.ActivityLogs;
import com.alon.android.puzzle.ActivityMain;
import com.alon.android.puzzle.R;

public class FragmentCredits extends FragmentBase implements OnClickListener {

	private View m_topView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		getUtils().loadSound(R.raw.click);

		m_topView = inflater.inflate(R.layout.fragment_credits, container,
				false);

		m_topView.findViewById(R.id.btnCreditsOk).setOnClickListener(this);

		Button buttonLog = (Button) m_topView.findViewById(R.id.btnLog);
		buttonLog.setOnClickListener(this);
		if (ActivityMain.INTERNAL_LOGS) {
			buttonLog.setVisibility(View.VISIBLE);
		}

		TextView text = (TextView) m_topView.findViewById(R.id.textCredits);
		text.setMovementMethod(new ScrollingMovementMethod());
		try {
			text.setText(getUtils().getResourceText(R.raw.credits));
		} catch (Exception e) {
			getUtils().handleError(e);
		}
		return m_topView;
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
		case R.id.btnCreditsOk:
			getUtils().playSound(R.raw.click);
			getMainActivity().setFragmentMain();
			break;
		case R.id.btnLog:
			getUtils().playSound(R.raw.click);
			Intent intent = new Intent(getMainActivity(), ActivityLogs.class);
			startActivity(intent);
			break;
		}
	}

}
