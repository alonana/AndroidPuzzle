package com.alon.android.puzzle.play;

import java.util.Iterator;
import java.util.LinkedList;

import com.alon.android.puzzle.Utils;

import android.graphics.Canvas;

public class ScoresDraw implements Runnable {

	private int m_scores;
	private LinkedList<ScoresDrawSingle> m_drawings;
	private boolean m_stop;
	private Object m_monitor;
	private PuzzleView m_view;

	public ScoresDraw(PuzzleView view) {
		m_scores = 0;
		m_drawings = new LinkedList<ScoresDrawSingle>();
		m_stop = false;
		m_monitor = new Object();
		m_view = view;
		Thread thread = new Thread(this);
		thread.start();
	}

	synchronized public void addScore(int matching, int partsAmount,
			PuzzlePart part) {
		int score = getScore(matching, partsAmount);
		m_scores += score;

		ScoresDrawSingle single = new ScoresDrawSingle(score, part);
		m_drawings.add(single);
		synchronized (m_monitor) {
			m_monitor.notify();
		}
	}

	private int getScore(int matching, int partsAmount) {

		return matching * partsAmount;
	}

	public void draw(Canvas canvas) {
		if (m_drawings.size() == 0) {
			return;
		}
		drawScores(canvas);
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
