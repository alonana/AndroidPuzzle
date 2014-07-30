package com.example.alon1;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;

public class PuzzleView extends View implements View.OnTouchListener {

	private LinkedList<PuzzlePart> m_parts;
	private PuzzlePart m_selectedPart;
	private boolean m_isMove;
	private int m_grace;

	public PuzzleView(Context context) {
		super(context);
		setBackgroundColor(Color.BLACK);
		this.setOnTouchListener(this);
	}

	public void setImage(Bitmap bitmap, int amount) {
		m_grace = getHeight() / 50;
		LinkedList<PuzzlePart> parts = createParts(bitmap, amount);
		updateNeighbours(parts, amount);
		shuffleParts(parts);
		m_parts = parts;
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
		int partDestinationAdvance = m_grace * 3;
		int partSequence = 0;

		LinkedList<PuzzlePart> parts = new LinkedList<PuzzlePart>();
		for (int row = 0; row < amount; row++) {
			for (int col = 0; col < amount; col++) {

				Rect source = new Rect(col * partSourceWidth, row
						* partSourceHeight, (col + 1) * partSourceWidth,
						(row + 1) * partSourceHeight);

				Rect destination = new Rect(partSequence
						* partDestinationAdvance, partSequence
						* partDestinationAdvance, partSequence
						* partDestinationAdvance + partDestinationWidth,
						partSequence * partDestinationAdvance
								+ partDestinationHeight);

				PuzzlePart part = new PuzzlePart(this, bitmap, source,
						destination);
				parts.add(part);
				partSequence++;
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

		Iterator<PuzzlePart> iterator = m_parts.descendingIterator();
		while (iterator.hasNext()) {
			PuzzlePart part = iterator.next();
			part.draw(canvas);
		}
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {

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

		if (!m_isMove) {
			m_selectedPart.rotate();
		}
		m_selectedPart.matchNeighbours();
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
