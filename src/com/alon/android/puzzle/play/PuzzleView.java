package com.alon.android.puzzle.play;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.alon.android.puzzle.FragmentPuzzle;
import com.alon.android.puzzle.R;
import com.alon.android.puzzle.Utils;
import com.google.android.gms.games.Games;

public class PuzzleView extends View implements View.OnTouchListener {

	public static final String SCORE = "score";
	private static final String SAVED_PARTS = PuzzleView.class.getSimpleName()
			+ "parts";

	private Utils m_utils;
	private FragmentPuzzle m_fragment;

	private LinkedList<PuzzlePart> m_parts;
	private PuzzlePart m_selectedPart;
	private boolean m_isMove;
	private boolean m_isDone;
	private boolean m_isScoreUpdated;
	private long m_downTime;
	private int m_grace;
	private PartsStatus m_savedStatus;
	private ScoresDraw m_scoresDraw;

	private Bitmap m_original;
	private Rect m_allClip;

	public PuzzleView(FragmentPuzzle fragment, Utils utils,
			Bundle savedInstanceState) {
		super(fragment.getMainActivity());
		if (savedInstanceState != null) {
			m_savedStatus = (PartsStatus) savedInstanceState.get(SAVED_PARTS);
		}
		m_scoresDraw = new ScoresDraw(this);
		m_utils = utils;
		m_isDone = false;
		m_isScoreUpdated = false;
		setBackgroundColor(Color.BLACK);
		this.setOnTouchListener(this);
		m_fragment = fragment;

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
		restoreParts(parts);
		m_parts = parts;
	}

	private void restoreParts(LinkedList<PuzzlePart> parts) {
		if (m_savedStatus == null) {
			return;
		}

		m_savedStatus.restoreParts(parts);
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
		if (m_savedStatus != null) {
			return;
		}

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

				Rect destination = createDestination(partDestinationWidth,
						partDestinationHeight, partDestinationAdvance,
						partDestinationOffset, partSequence);

				PuzzlePart part = new PuzzlePart(this, bitmap, source,
						destination, partSequence);
				parts.add(part);
				partSequence++;
				if (partSequence % 10 == 0) {
					partDestinationOffset += partDestinationWidth / 2;
				}
			}
		}
		return parts;
	}

	private Rect createDestination(int partDestinationWidth,
			int partDestinationHeight, int partDestinationAdvance,
			int partDestinationOffset, int partSequence) {

		if (m_savedStatus == null) {

			int startPointY = (partSequence % 10) * partDestinationAdvance;
			int startPointX = startPointY + partDestinationOffset;
			Rect destination = new Rect(partDestinationOffset + startPointX,
					startPointY, partDestinationOffset + startPointX
							+ partDestinationWidth, startPointY
							+ partDestinationHeight);
			return destination;
		}

		return m_savedStatus.getPartLocation(partDestinationWidth,
				partDestinationHeight, partSequence, getWidth(), getHeight());
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
		m_scoresDraw.draw(canvas);
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		view.performClick();

		if (m_isDone) {
			return true;
		}
		if (m_parts == null) {
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
		m_downTime = System.currentTimeMillis();
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

		if (System.currentTimeMillis() - m_downTime < 200) {
			m_isMove = false;
		}

		if (m_isMove) {
			m_utils.playSound(R.raw.release);
		} else {
			m_utils.playSound(R.raw.rotate);
			m_selectedPart.rotate();
		}

		int matching = m_selectedPart.matchNeighbours();
		if (matching > 0) {
			m_scoresDraw.addScore(matching, m_parts.size(), m_selectedPart);
			if (m_selectedPart.getGlued().size() == m_parts.size()) {
				handleDone();
			} else {
				m_utils.playSound(R.raw.join);
			}
		}
		invalidate();
		m_selectedPart = null;
		return true;
	}

	private void handleDone() {
		m_utils.playSound(R.raw.done);
		m_isDone = true;

		Timer timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				m_fragment.getMainActivity().runOnUiThread(new Runnable() {

					@Override
					public void run() {
						showDoneDialog();
					}
				});
			}

		}, 2000);
	}

	private void showDoneDialog() {
		if (!m_fragment.isAdded()) {
			return;
		}
		final Dialog dialog = new Dialog(m_fragment.getMainActivity());
		dialog.setContentView(R.layout.dialog_end);
		ColorDrawable color = new ColorDrawable(Color.WHITE);
		color.setAlpha(0x80);
		dialog.getWindow().setBackgroundDrawable(color);
		dialog.setTitle("Puzzle completed");
		TextView text = (TextView) dialog.findViewById(R.id.textEndPuzzle);
		text.setText("Game score: " + m_scoresDraw.getScoresAndStop());
		Button ok = (Button) dialog.findViewById(R.id.btnEndPuzzle);
		ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (m_isScoreUpdated) {
					return;
				}
				m_isScoreUpdated = true;

				int newScore = m_fragment.getGameSettings().addScore(
						m_scoresDraw.getScoresAndStop());

				updateGooglePlay(newScore);
				dialog.dismiss();
				m_fragment.getMainActivity().setFragmentMain();
			}

			private void updateGooglePlay(int newScore) {
				if (!m_fragment.getMainActivity().isSignedIn()) {
					return;
				}

				String boardId = m_fragment.getString(R.string.leaderboard_id);
				Games.Leaderboards.submitScore(m_fragment.getMainActivity()
						.getApiClient(), boardId, newScore);

				int size = (int) Math.sqrt(m_parts.size());
				int achievementId;
				switch (size) {
				case 2:
					achievementId = R.string.achievement_second_grade;
					break;
				case 3:
					achievementId = R.string.achievement_third_grade;
					break;
				case 4:
					achievementId = R.string.achievement_fourth_grade;
					break;
				case 5:
					achievementId = R.string.achievement_fifth_grade;
					break;
				case 6:
					achievementId = R.string.achievement_sixth_grade;
					break;
				default:
					throw new RuntimeException("invalid size " + size);
				}
				String achievement = m_fragment.getString(achievementId);
				Games.Achievements.unlock(m_fragment.getMainActivity()
						.getApiClient(), achievement);
			}
		});
		dialog.show();
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

	@SuppressLint("UseSparseArrays")
	public void saveInstanceState(Bundle outState) {

		PartsStatus status = new PartsStatus(m_parts);
		outState.putSerializable(SAVED_PARTS, status);
	}

	public void runOnUiThread(Runnable action) {
		m_fragment.getMainActivity().runOnUiThread(action);
	}
}
