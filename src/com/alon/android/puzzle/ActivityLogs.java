package com.alon.android.puzzle;

import java.io.File;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.widget.TextView;

public class ActivityLogs extends ActionBarActivity implements OnClickListener {

	private Utils m_utils;
	private TextView m_textLog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			onCreateWorker(savedInstanceState);
		} catch (Exception e) {
			new Utils(this).handleError(e);
		}
	}

	public void onCreateWorker(Bundle savedInstanceState) throws Exception {
		m_utils = new Utils(this);
		m_utils.setFullScreen();

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_logs);

		m_utils.loadSound(R.raw.click);

		findViewById(R.id.btnCleanLog).setOnClickListener(this);
		findViewById(R.id.btnEmailLog).setOnClickListener(this);

		m_textLog = (TextView) findViewById(R.id.textLog);
		m_textLog.setMovementMethod(new ScrollingMovementMethod());
		ViewTreeObserver observer = m_textLog.getViewTreeObserver();
		observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

			@Override
			public void onGlobalLayout() {
				Layout layout = m_textLog.getLayout();
				int scrollAmount = layout.getLineTop(m_textLog.getLineCount())
						- m_textLog.getHeight();

				if (scrollAmount > 0) {
					m_textLog.scrollTo(0, scrollAmount);
				}
			}
		});

		loadLog();
	}

	public void loadLog() throws Exception {
		File log = m_utils.getLogFile();
		if (!log.exists()) {
			m_textLog.setText("log is empty");
			return;
		}

		String data = Utils.loadFile(log);
		m_textLog.setText(data);
	}

	@Override
	public void onClick(View view) {
		try {
			onClickWorker(view);
		} catch (Exception e) {
			m_utils.handleError(e);
		}

	}

	private void onClickWorker(View view) throws Exception {
		switch (view.getId()) {
		case R.id.btnCleanLog:
			m_utils.playSound(R.raw.click);
			cleanLog();
			break;
		case R.id.btnEmailLog:
			m_utils.playSound(R.raw.click);
			emailLog();
			break;
		}
	}

	public void cleanLog() throws Exception {
		File log = m_utils.getLogFile();
		if (log.exists()) {
			log.delete();
		}
		loadLog();
	}

	public void emailLog() throws Exception {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_EMAIL,
				new String[] { "alonana@gmail.com" });
		intent.putExtra(Intent.EXTRA_SUBJECT, "PuzzleMe Log");
		intent.putExtra(Intent.EXTRA_TEXT, m_textLog.getText());
		startActivity(Intent.createChooser(intent, "Email log..."));
	}
}
