package com.alon.android.puzzle.play;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class ScoresDrawSingle {

	static private long TTL = 5000;

	private String m_score;
	private long m_startTime;
	private int m_x;
	private int m_y;
	private Paint m_textPaint;
	private Paint m_textPaintOutline;

	public ScoresDrawSingle(int score, PuzzlePart part) {
		m_score = "+" + score;
		m_startTime = System.currentTimeMillis();
		m_x = part.getLocation().centerX();
		m_y = part.getLocation().centerY();

		m_textPaintOutline = new Paint();
		m_textPaintOutline.setAntiAlias(true);
		m_textPaintOutline.setTextSize(24);
		m_textPaintOutline.setColor(Color.parseColor("#14b7d3"));
		m_textPaintOutline.setStyle(Paint.Style.STROKE);
		m_textPaintOutline.setStrokeWidth(4);

		m_textPaint = new Paint();
		m_textPaint.setAntiAlias(true);
		m_textPaint.setTextSize(24);
		m_textPaint.setColor(Color.parseColor("#d6272d"));
		m_textPaint.setStyle(Paint.Style.FILL);
	}

	public boolean isExpired() {
		if (System.currentTimeMillis() > m_startTime + TTL) {
			return true;
		}
		return false;
	}

	public void draw(Canvas canvas) {

		long diff = System.currentTimeMillis() - m_startTime;
		if (diff >= TTL) {
			return;
		}
		int percentTimePassed = (int) ((diff * 100) / TTL);
		int alpha = 255 * (100 - percentTimePassed) / 100;
		m_textPaint.setAlpha(alpha);
		m_textPaintOutline.setAlpha(alpha);
		canvas.drawText(m_score, m_x, m_y, m_textPaintOutline);
		canvas.drawText(m_score, m_x, m_y, m_textPaint);
	}
}
