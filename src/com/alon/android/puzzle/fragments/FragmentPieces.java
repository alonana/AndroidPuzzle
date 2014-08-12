package com.alon.android.puzzle.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.alon.android.puzzle.R;

public class FragmentPieces extends FragmentBase implements OnClickListener {

	private View m_topView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		getUtils().loadSound(R.raw.click);

		m_topView = inflater
				.inflate(R.layout.fragment_pieces, container, false);

		m_topView.findViewById(R.id.btnSize2).setOnClickListener(this);
		m_topView.findViewById(R.id.btnSize3).setOnClickListener(this);
		m_topView.findViewById(R.id.btnSize4).setOnClickListener(this);
		m_topView.findViewById(R.id.btnSize5).setOnClickListener(this);
		m_topView.findViewById(R.id.btnSize6).setOnClickListener(this);
		return m_topView;
	}

	@Override
	public void onClick(View view) {

		switch (view.getId()) {
		case R.id.btnSize2:
			setSize(2);
			break;
		case R.id.btnSize3:
			setSize(3);
			break;
		case R.id.btnSize4:
			setSize(4);
			break;
		case R.id.btnSize5:
			setSize(5);
			break;
		case R.id.btnSize6:
			setSize(6);
			break;
		}

	}

	private void setSize(int size) {
		getUtils().playSound(R.raw.click);
		getGameSettings().setPieces(size);
		getMainActivity().setFragmentNewGame();
	}

}
