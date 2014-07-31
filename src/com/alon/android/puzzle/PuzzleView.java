package com.alon.android.puzzle;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;

public class PuzzleView extends View implements View.OnTouchListener {

	private Utils m_utils;
	private LinkedList<PuzzlePart> m_parts;
	private PuzzlePart m_selectedPart;
	private boolean m_isMove;
	private boolean m_isDone;
	private long m_doneTime;
	private int m_grace;
	private PuzzleActivity m_activity;

	private Bitmap m_original;
	private Rect m_allClip;

	public PuzzleView(PuzzleActivity context, Utils utils) {
		super(context);
		m_utils = utils;
		m_isDone = false;
		setBackgroundColor(Color.BLACK);
		this.setOnTouchListener(this);
		m_activity = context;

		m_utils.loadSound(R.raw.done);
		m_utils.loadSound(R.raw.join);
		m_utils.loadSound(R.raw.pick);
		m_utils.loadSound(R.raw.release);
		m_utils.loadSound(R.raw.rotate);
	}

	public void setImage(Bitmap bitmap, int amount) {
		m_allClip = new Rect(0, 0, getWidth(), getHeight());
		m_original = bitmap;
		m_grace = getHeight() / 50;
		LinkedList<PuzzlePart> parts = createParts(bitmap, amount);
		updateNeighbours(parts, amount);
		shuffleParts(parts);
		m_parts = parts;
	}

	@Override
	public boolean performClick() {
		return super.performClick();
	}

	private void updateNeighbours(LinkedList<PuzzlePart> parts, int amount) {
		for (int partIndex = 0; partIndex < parts.size(); partIndex++) {

			PuzzlePart left = null;
			if (partIndex % amount != 0) {
				left = parts.get(partIndex - 1);
			}
			PuzzlePart right = null;
			if (partIndex % amount != amount - 1) {
				right = parts.get(partIndex + 1);
			}
			PuzzlePart up = null;
			if (partIndex >= amount) {
				up = parts.get(partIndex - amount);
			}
			PuzzlePart down = null;
			if (partIndex < parts.size() - amount) {
				down = parts.get(partIndex + amount);
			}

			PuzzlePart part = parts.get(partIndex);
			part.setNeighbours(left, up, right, down);
		}
	}

	private void shuffleParts(LinkedList<PuzzlePart> parts) {
		Collections.shuffle(parts);
		Random random = new Random();
		for (PuzzlePart part : parts) {
			int times = random.nextInt(4);
			for (int rotate = 0; rotate < times; rotate++) {
				part.rotate();
			}
		}
	}

	private LinkedList<PuzzlePart> createParts(Bitmap bitmap, int amount) {
		int partSourceWidth = bitmap.getWidth() / amount;
		int partDestinationWidth = getWidth() / amount;
		int partSourceHeight = bitmap.getHeight() / amount;
		int partDestinationHeight = getHeight() / amount;
		int partDestinationAdvance = m_grace * 2;
		int partDestinationOffset = 0;
		int partSequence = 0;

		LinkedList<PuzzlePart> parts = new LinkedList<PuzzlePart>();
		for (int row = 0; row < amount; row++) {
			for (int col = 0; col < amount; col++) {

				Rect source = new Rect(col * partSourceWidth, row
						* partSourceHeight, (col + 1) * partSourceWidth,
						(row + 1) * partSourceHeight);

				int startPointY = partSequence * partDestinationAdvance;
				int startPointX = startPointY + partDestinationOffset;
				Rect destination = new Rect(
						partDestinationOffset + startPointX, startPointY,
						partDestinationOffset + startPointX
								+ partDestinationWidth, startPointY
								+ partDestinationHeight);

				PuzzlePart part = new PuzzlePart(this, bitmap, source,
						destination);
				parts.add(part);
				partSequence++;
				if (partSequence > 10) {
					partSequence = 0;
					partDestinationOffset += partDestinationWidth / 2;
				}
			}
		}
		return parts;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (m_parts == null) {
			return;
		}

		if (m_isDone) {
			canvas.drawBitmap(m_original, null, m_allClip, null);
			return;
		}

		Iterator<PuzzlePart> iterator = m_parts.descendingIterator();
		while (iterator.hasNext()) {
			PuzzlePart part = iterator.next();
			part.draw(canvas);
		}
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		view.performClick();

		if (m_isDone) {
			if (System.currentTimeMillis() - m_doneTime < 3000) {
				return true;
			}
			m_activity.finish();
			return true;
		}
		int eventX = (int) event.getX();
		int eventY = (int) event.getY();

		switch (event.getAction()) {

		case MotionEvent.ACTION_DOWN:
			return handleDown(eventX, eventY);

		case MotionEvent.ACTION_UP:
			return handleUp();

		case MotionEvent.ACTION_MOVE:
			return handleMove(eventX, eventY);
		}

		return false;
	}

	private boolean handleDown(int eventX, int eventY) {
		m_isMove = false;
		m_selectedPart = findPart(eventX, eventY);
		if (m_selectedPart == null) {
			return false;
		}

		m_utils.playSound(R.raw.pick);
		moveToTop();
		m_selectedPart.setStart(eventX, eventY);
		return true;
	}

	private void moveToTop() {
		Collection<PuzzlePart> moved = m_selectedPart.getGlued();
		m_parts.removeAll(moved);
		for (PuzzlePart part : moved) {
			m_parts.addFirst(part);
		}
	}

	private boolean handleMove(int eventX, int eventY) {
		if (m_selectedPart == null) {
			return false;
		}

		m_selectedPart.move(eventX, eventY);
		m_isMove = true;
		invalidate();
		return true;
	}

	private boolean handleUp() {
		if (m_selectedPart == null) {
			return false;
		}

		if (m_isMove) {
			m_utils.playSound(R.raw.release);
		} else {
			m_utils.playSound(R.raw.rotate);
			m_selectedPart.rotate();
		}

		if (m_selectedPart.matchNeighbours()) {
			if (m_selectedPart.getGlued().size() == m_parts.size()) {
				m_utils.playSound(R.raw.done);
				m_isDone = true;
				m_doneTime = System.currentTimeMillis();
			} else {
				m_utils.playSound(R.raw.join);
			}
		}
		invalidate();
		m_selectedPart = null;
		return true;
	}

	private PuzzlePart findPart(int x, int y) {
		for (PuzzlePart part : m_parts) {
			if (part.isDestination(x, y)) {
				return part;
			}
		}
		return null;
	}

	public int getGrace() {
		return m_grace;
	}
}
