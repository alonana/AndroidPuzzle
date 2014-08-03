package com.alon.android.puzzle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

public class PiecesActivity extends ActionBarActivity {

	public static final String AMOUNT = "amount";

	private Utils m_utils;

	public PiecesActivity() {
		m_utils = new Utils(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		m_utils.setFullScreen();
		setContentView(R.layout.activity_pieces);
		m_utils.loadSound(R.raw.click);
	}

	public void setSize2(View view) {
		setSize(2);
	}

	public void setSize3(View view) {
		setSize(3);
	}

	public void setSize4(View view) {
		setSize(4);
	}

	public void setSize5(View view) {
		setSize(5);
	}

	public void setSize6(View view) {
		setSize(6);
	}

	private void setSize(int size) {
		m_utils.playSound(R.raw.click);
		Intent result = new Intent();
		result.putExtra(AMOUNT, size);
		setResult(Activity.RESULT_OK, result);
		finish();
	}

}
