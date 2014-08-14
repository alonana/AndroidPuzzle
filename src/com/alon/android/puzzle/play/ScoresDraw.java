package com.alon.android.puzzle.play;

import java.util.Iterator;
import java.util.LinkedList;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.alon.android.puzzle.Utils;

public class ScoresDraw implements Runnable {

	private int m_scores;
	private String m_scoresText;
	private LinkedList<ScoresDrawSingle> m_drawings;
	private boolean m_stop;
	private Object m_monitor;
	private PuzzleView m_view;
	private Paint m_textPaint;

	public ScoresDraw(PuzzleView view) {

		setScores(0);
		m_drawings = new LinkedList<ScoresDrawSingle>();
		m_stop = false;
		m_monitor = new Object();
		m_view = view;

		m_textPaint = new Paint();
		m_textPaint.setAntiAlias(true);
		m_textPaint.setTextSize(24);
		m_textPaint.setColor(Color.parseColor("#f3b329"));
		m_textPaint.setStyle(Paint.Style.FILL);

		Thread thread = new Thread(this);
		thread.start();
	}

	private void setScores(int score) {
		m_scores = score;
		m_scoresText = "Score: " + m_scores;
	}

	synchronized public void addScore(int matchingParts, PuzzlePart part,
			boolean isNetwork) {
		int score = matchingParts * m_view.getTotalPartsAmount();
		if (isNetwork) {
			score = (int) (0.75 * score);
		}
		setScores(m_scores + score);

		ScoresDrawSingle single = new ScoresDrawSingle(score, part, isNetwork);
		m_drawings.add(single);
		synchronized (m_monitor) {
			m_monitor.notify();
		}
	}

	public void draw(Canvas canvas) {
		drawTotal(canvas);
		drawScores(canvas);
	}

	private void drawTotal(Canvas canvas) {
		canvas.drawText(m_scoresText, 20, 20, m_textPaint);
	}

	synchronized private void drawScores(Canvas canvas) {
		Iterator<ScoresDrawSingle> iterator = m_drawings.iterator();
		while (iterator.hasNext()) {
			ScoresDrawSingle single = iterator.next();
			if (single.isExpired()) {
				iterator.remove();
			}
			single.draw(canvas);
		}
	}

	public int getScoresAndStop() {
		m_stop = true;
		return m_scores;
	}

	@Override
	public void run() {
		while (!m_stop) {

			waitForScores();
			refreshCanvas();
		}
	}

	private void refreshCanvas() {
		while (m_drawings.size() > 0) {
			m_view.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					m_view.invalidate();
				}
			});
			Utils.sleep(50);
		}
	}

	private void waitForScores() {
		if (m_drawings.size() > 0) {
			return;
		}

		synchronized (m_monitor) {
			try {
				m_monitor.wait();
			} catch (InterruptedException e) {
			}
		}
	}
}
