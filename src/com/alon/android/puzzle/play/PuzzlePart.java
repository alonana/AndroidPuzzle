package com.alon.android.puzzle.play;

import java.util.ArrayList;
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

import com.alon.android.puzzle.fragments.FragmentNetworkGame;

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
	private int m_sequence;
	private LinkedList<PuzzlePart> m_neighbours;
	private HashSet<PuzzlePart> m_glued;

	private Point m_startClick;
	private Rect m_startDestination;

	public PuzzlePart(PuzzleView puzzleView, Bitmap sourceImage, Rect source,
			Rect destination, int partSequence) {

		m_view = puzzleView;
		m_sequence = partSequence;

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
		for (PuzzlePart part : m_glued) {
			part.setStartSingle(x, y);
		}
	}

	private void setStartSingle(int x, int y) {
		m_startClick = new Point(x, y);
		m_startDestination = new Rect(m_location);
	}

	public void move(int x, int y) throws Exception {
		for (PuzzlePart part : m_glued) {
			part.moveSingle(x, y);
		}
	}

	private void moveSingle(int x, int y) throws Exception {
		HashSet<PuzzlePart> all = new HashSet<PuzzlePart>();
		all.add(this);

		int xMove = x - m_startClick.x;
		int yMove = y - m_startClick.y;
		m_location = new Rect(m_startDestination.left + xMove,
				m_startDestination.top + yMove, m_startDestination.right
						+ xMove, m_startDestination.bottom + yMove);

		sendPartStatus(false);
	}

	public void updateFromNetwork(Rect newLocation, PartStatus status,
			boolean isReliable, ArrayList<PuzzlePart> partsBySequence)
			throws Exception {
		m_location = newLocation;
		if (!isReliable) {
			return;
		}

		while (m_rotation != status.rotation) {
			rotateSingleLocally();
		}

		if (status.glued.size() != this.m_glued.size()) {
			HashSet<PuzzlePart> newGlued = new HashSet<PuzzlePart>();
			for (Integer partSequence : status.glued) {
				PuzzlePart part = partsBySequence.get(partSequence);
				newGlued.add(part);
			}
			m_glued = newGlued;
		}
	}

	private void sendPartStatus(boolean reliable) throws Exception {
		FragmentNetworkGame networkGame = m_view.getNetworkGame();
		if (networkGame == null) {
			return;
		}

		networkGame.sendMessage(reliable, getStatus());
	}

	public void rotate(boolean localOnly) throws Exception {
		for (PuzzlePart part : m_glued) {
			part.rotateSingleLocally();
		}

		updateGluedLocations();

		if (!localOnly) {
			sendGluedStatus();
		}
	}

	private void sendGluedStatus() throws Exception {
		for (PuzzlePart part : m_glued) {
			part.sendPartStatus(true);
		}
	}

	private void updateGluedLocations() throws Exception {

		HashSet<PuzzlePart> handled = new HashSet<PuzzlePart>();
		handled.add(this);

		LinkedList<PuzzleConnection> toHandle = new LinkedList<PuzzleConnection>();
		this.addNeighboursToHandle(toHandle);

		while (!toHandle.isEmpty()) {

			PuzzleConnection connection = toHandle.remove();
			if (handled.contains(connection.to)) {
				continue;
			}
			handled.add(connection.to);

			connection.from.updateNeighbourLocation(connection.to);
			connection.to.addNeighboursToHandle(toHandle);
		}
	}

	private void addNeighboursToHandle(LinkedList<PuzzleConnection> toHandle) {
		for (PuzzlePart neighbour : m_neighbours) {
			if (neighbour == null) {
				continue;
			}
			if (!m_glued.contains(neighbour)) {
				continue;
			}

			PuzzleConnection next = new PuzzleConnection(this, neighbour);
			if (toHandle.contains(next)) {
				continue;
			}
			toHandle.add(next);
		}
	}

	private void updateNeighbourLocation(PuzzlePart neighbour) throws Exception {
		PuzzlePart part = m_neighbours.get(LEFT);
		if ((part != null) && (part.equals(neighbour))) {
			neighbour.m_location = new Rect(m_location.left
					- m_location.width(), m_location.top, m_location.left,
					m_location.bottom);

			neighbour.sendPartStatus(false);
			return;
		}

		part = m_neighbours.get(UP);
		if ((part != null) && (part.equals(neighbour))) {
			neighbour.m_location = new Rect(m_location.left, m_location.top
					- m_location.height(), m_location.right, m_location.top);

			neighbour.sendPartStatus(false);
			return;
		}

		part = m_neighbours.get(RIGHT);
		if ((part != null) && (part.equals(neighbour))) {
			neighbour.m_location = new Rect(m_location.right, m_location.top,
					m_location.right + m_location.width(), m_location.bottom);

			neighbour.sendPartStatus(false);
			return;
		}

		part = m_neighbours.get(DOWN);
		if ((part != null) && (part.equals(neighbour))) {
			neighbour.m_location = new Rect(m_location.left, m_location.bottom,
					m_location.right, m_location.bottom + m_location.height());

			neighbour.sendPartStatus(false);
			return;
		}

		throw new RuntimeException("internal error");
	}

	private void rotateSingleLocally() throws Exception {

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

	public int matchNeighbours() throws Exception {
		int match = 0;
		for (PuzzlePart part : m_glued) {
			match += part.matchNeighboursSingle();
		}

		return match;
	}

	private int matchNeighboursSingle() throws Exception {
		int match = 0;
		for (int side = 0; side < 4; side++) {
			if (matchNeighbour(side)) {
				match++;
			}
		}
		return match;
	}

	private boolean matchNeighbour(int side) throws Exception {
		PuzzlePart other = m_neighbours.get(side);
		if (other == null) {
			return false;
		}
		if (m_glued.contains(other)) {
			return false;
		}
		if (m_rotation != other.m_rotation) {
			return false;
		}

		if (!updateNear(side, other)) {
			return false;
		}

		updateGlued(other);
		return true;
	}

	private boolean updateNear(int side, PuzzlePart other) throws Exception {
		switch (side) {

		case LEFT:
			if (!isNear(m_location.left, m_location.top,
					other.m_location.right, other.m_location.top)) {
				return false;
			}
			other.setStart(0, 0);
			other.move(m_location.left - other.m_location.right, m_location.top
					- other.m_location.top);
			return true;
		case UP:
			if (!isNear(m_location.left, m_location.top, other.m_location.left,
					other.m_location.bottom)) {
				return false;
			}
			other.setStart(0, 0);
			other.move(m_location.left - other.m_location.left, m_location.top
					- other.m_location.bottom);
			return true;
		case RIGHT:
			if (!isNear(m_location.right, m_location.top,
					other.m_location.left, other.m_location.top)) {
				return false;
			}
			other.setStart(0, 0);
			other.move(m_location.right - other.m_location.left, m_location.top
					- other.m_location.top);
			return true;
		case DOWN:
			if (!isNear(m_location.left, m_location.bottom,
					other.m_location.left, other.m_location.top)) {
				return false;
			}
			other.setStart(0, 0);
			other.move(m_location.left - other.m_location.left,
					m_location.bottom - other.m_location.top);
			return true;
		}

		throw new RuntimeException("should not get here");
	}

	private void updateGlued(PuzzlePart other) throws Exception {
		HashSet<PuzzlePart> newGlued = new HashSet<PuzzlePart>();
		newGlued.add(this);
		newGlued.add(other);
		newGlued.addAll(this.m_glued);
		newGlued.addAll(other.m_glued);

		this.m_glued = newGlued;
		other.m_glued = newGlued;

		Collection<PuzzlePart> all = detectAllGlued();
		for (PuzzlePart part : all) {
			part.m_glued = newGlued;
			part.sendPartStatus(true);
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

	public PartStatus getStatus() {
		PartStatus status = new PartStatus();
		status.xPercent = (float) m_location.left / m_view.getWidth();
		status.yPercent = (float) m_location.top / m_view.getHeight();
		status.sequence = m_sequence;
		status.rotation = m_rotation;

		for (PuzzlePart glued : m_glued) {
			status.glued.add(glued.m_sequence);
		}
		return status;
	}

	public void rotateTo(int rotation) throws Exception {
		while (m_rotation != rotation) {
			rotate(true);
		}
	}

	public Rect getLocation() {
		return m_location;
	}

	public int getSequence() {
		return m_sequence;
	}
}
