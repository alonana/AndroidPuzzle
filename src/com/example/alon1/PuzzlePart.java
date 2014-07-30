package com.example.alon1;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;

public class PuzzlePart {

	private static final int LEFT = 0;
	private static final int UP = 1;
	private static final int RIGHT = 2;
	private static final int DOWN = 3;

	private PuzzleView m_view;
	private Bitmap m_bitmap;
	private Rect m_location;
	private Paint m_paint;
	private int m_rotation;
	private LinkedList<PuzzlePart> m_neighbours;
	private HashSet<PuzzlePart> m_glued;

	private Point m_startClick;
	private Rect m_startDestination;

	public PuzzlePart(PuzzleView puzzleView, Bitmap sourceImage, Rect source,
			Rect destination) {

		m_view = puzzleView;

		m_bitmap = Bitmap.createBitmap(sourceImage, source.left, source.top,
				source.width(), source.height());

		m_rotation = 0;
		m_glued = new HashSet<PuzzlePart>();
		m_glued.add(this);

		m_location = destination;
		m_paint = new Paint();
		m_paint.setColor(Color.parseColor("#3abcc4"));
		m_paint.setStyle(Style.STROKE);
		m_paint.setStrokeWidth(3);
	}

	public void draw(Canvas canvas) {
		canvas.drawBitmap(m_bitmap, null, m_location, null);

		for (int side = 0; side < 4; side++) {
			drawSideFrame(canvas, side);
		}
	}

	private void drawSideFrame(Canvas canvas, int side) {
		PuzzlePart neighbour = m_neighbours.get(side);
		if (neighbour != null) {
			if (m_glued.contains(neighbour)) {
				return;
			}
		}

		switch (side) {
		case LEFT:
			canvas.drawLine(m_location.left, m_location.top, m_location.left,
					m_location.bottom, m_paint);
			break;
		case UP:
			canvas.drawLine(m_location.left, m_location.top, m_location.right,
					m_location.top, m_paint);
			break;
		case RIGHT:
			canvas.drawLine(m_location.right, m_location.top, m_location.right,
					m_location.bottom, m_paint);
			break;
		case DOWN:
			canvas.drawLine(m_location.right, m_location.bottom,
					m_location.left, m_location.bottom, m_paint);
			break;
		}
	}

	public boolean isDestination(int x, int y) {
		if (m_location.contains(x, y)) {
			return true;
		}
		return false;
	}

	public void setStart(int x, int y) {
		for (PuzzlePart part : detectAllGlued()) {
			part.setStartSingle(x, y);
		}
	}

	private void setStartSingle(int x, int y) {
		m_startClick = new Point(x, y);
		m_startDestination = new Rect(m_location);
	}

	public void move(int x, int y) {
		for (PuzzlePart part : detectAllGlued()) {
			part.moveSingle(x, y);
		}
	}

	private void moveSingle(int x, int y) {
		HashSet<PuzzlePart> all = new HashSet<PuzzlePart>();
		all.add(this);

		int xMove = x - m_startClick.x;
		int yMove = y - m_startClick.y;
		m_location = new Rect(m_startDestination.left + xMove,
				m_startDestination.top + yMove, m_startDestination.right
						+ xMove, m_startDestination.bottom + yMove);
	}

	public void rotate() {
		for (PuzzlePart part : detectAllGlued()) {
			part.rotateSingle();
		}
	}

	private void rotateSingle() {
		Matrix matrix = new Matrix();
		matrix.postRotate(90);
		Bitmap rotated = Bitmap.createBitmap(m_bitmap, 0, 0,
				m_bitmap.getWidth(), m_bitmap.getHeight(), matrix, false);
		m_bitmap = rotated;
		m_rotation = (m_rotation + 90) % 360;
		Collections.rotate(m_neighbours, 1);
	}

	public void setNeighbours(PuzzlePart left, PuzzlePart up, PuzzlePart right,
			PuzzlePart down) {
		m_neighbours = new LinkedList<PuzzlePart>();
		m_neighbours.add(left);
		m_neighbours.add(up);
		m_neighbours.add(right);
		m_neighbours.add(down);
	}

	public void matchNeighbours() {
		for (int side = 0; side < 4; side++) {
			matchNeighbour(side);
		}
	}

	private void matchNeighbour(int side) {
		PuzzlePart other = m_neighbours.get(side);
		if (other == null) {
			return;
		}
		if (m_glued.contains(other)) {
			return;
		}
		if (m_rotation != other.m_rotation) {
			return;
		}

		switch (side) {

		case LEFT:
			if (!isNear(m_location.left, m_location.top,
					other.m_location.right, other.m_location.top)) {
				return;
			}
			other.setStart(0, 0);
			other.move(m_location.left - other.m_location.right, m_location.top
					- other.m_location.top);
			break;
		case UP:
			if (!isNear(m_location.left, m_location.top, other.m_location.left,
					other.m_location.bottom)) {
				return;
			}
			other.setStart(0, 0);
			other.move(m_location.left - other.m_location.left, m_location.top
					- other.m_location.bottom);
			break;
		case RIGHT:
			if (!isNear(m_location.right, m_location.top,
					other.m_location.left, other.m_location.top)) {
				return;
			}
			other.setStart(0, 0);
			other.move(m_location.right - other.m_location.left, m_location.top
					- other.m_location.top);
			break;
		case DOWN:
			if (!isNear(m_location.left, m_location.bottom,
					other.m_location.left, other.m_location.top)) {
				return;
			}
			other.setStart(0, 0);
			other.move(m_location.left - other.m_location.left,
					m_location.bottom - other.m_location.top);
			break;
		}

		updateGlued(other);
	}

	private void updateGlued(PuzzlePart other) {
		m_glued.add(other);
		other.m_glued.add(this);
		Collection<PuzzlePart> all = detectAllGlued();
		for (PuzzlePart part : all) {
			part.m_glued.addAll(all);
		}
	}

	private Collection<PuzzlePart> detectAllGlued() {
		HashSet<PuzzlePart> visited = new HashSet<PuzzlePart>();
		LinkedList<PuzzlePart> toVisit = new LinkedList<PuzzlePart>();

		toVisit.add(this);
		while (!toVisit.isEmpty()) {
			PuzzlePart part = toVisit.remove();
			visited.add(part);

			for (PuzzlePart next : part.m_glued) {
				if (visited.contains(next)) {
					continue;
				}
				if (toVisit.contains(next)) {
					continue;
				}
				toVisit.add(next);
			}
		}

		return visited;
	}

	private boolean isNear(int x1, int y1, int x2, int y2) {
		int grace = m_view.getGrace();
		int distanceX = Math.abs(x2 - x1);
		if (distanceX > grace) {
			return false;
		}
		int distanceY = Math.abs(y2 - y1);
		if (distanceY > grace) {
			return false;
		}
		return true;
	}

	public Collection<PuzzlePart> getGlued() {
		return m_glued;
	}
}
