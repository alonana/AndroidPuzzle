package com.alon.android.puzzle.play;

import java.util.ArrayList;
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

import com.alon.android.puzzle.PuzzleException;
import com.alon.android.puzzle.R;
import com.alon.android.puzzle.Utils;
import com.alon.android.puzzle.fragments.FragmentNetworkGame;
import com.alon.android.puzzle.fragments.FragmentPuzzle;
import com.google.android.gms.games.Games;

public class PuzzleView extends View implements View.OnTouchListener {

	public static final String SCORE = "score";
	private static final String SAVED_PARTS = PuzzleView.class.getSimpleName()
			+ "parts";

	private Utils m_utils;
	private FragmentPuzzle m_fragment;
	private FragmentNetworkGame m_networkGame;

	private LinkedList<PuzzlePart> m_parts;
	private ArrayList<PuzzlePart> m_partsBySequence;

	private PuzzlePart m_selectedPart;
	private boolean m_isMove;
	private boolean m_isDone;
	private boolean m_isScoreUpdated;
	private long m_downTime;
	private int m_grace;
	private int m_partWidth;
	private int m_partHeight;
	private PartsStatus m_savedStatus;
	private ScoresDraw m_scoresDraw;

	private Bitmap m_original;
	private Rect m_allClip;

	public PuzzleView(FragmentPuzzle fragment, Utils utils,
			Bundle savedInstanceState, FragmentNetworkGame network) {
		super(fragment.getMainActivity());

		m_networkGame = network;
		if (m_networkGame != null) {
			m_networkGame.setView(this);
		}

		m_scoresDraw = new ScoresDraw(this);
		m_utils = utils;
		m_utils.debug("new game starting");
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

	public void restoreInstanceState(Bundle savedInstanceState) {
		m_savedStatus = (PartsStatus) savedInstanceState.get(SAVED_PARTS);
	}

	public void setImage(Bitmap bitmap, int amount) throws Exception {
		m_allClip = new Rect(0, 0, getWidth(), getHeight());
		m_original = bitmap;
		m_grace = getHeight() / 50;
		LinkedList<PuzzlePart> parts = createParts(bitmap, amount);
		updateNeighbours(parts, amount);
		shuffleParts(parts);
		restoreParts(parts);
		m_parts = parts;
	}

	public int getTotalPartsAmount() {
		return m_parts.size();
	}

	private void restoreParts(LinkedList<PuzzlePart> parts) throws Exception {
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

	private void shuffleParts(LinkedList<PuzzlePart> parts) throws Exception {
		if (m_savedStatus != null) {
			return;
		}

		int partIndex = 0;
		for (PuzzlePart part : parts) {
			int times = getRandomRotation(partIndex);
			for (int rotate = 0; rotate < times; rotate++) {
				part.rotate(true);
			}
			partIndex++;
		}

		Collections.shuffle(parts);
	}

	private int getRandomRotation(int partIndex) {
		if (m_networkGame == null) {
			Random random = new Random();
			return random.nextInt(4);
		}

		return m_networkGame.getGameInit().getRotation(partIndex);
	}

	private LinkedList<PuzzlePart> createParts(Bitmap bitmap, int amount) {

		int partSourceWidth = bitmap.getWidth() / amount;
		int partSourceHeight = bitmap.getHeight() / amount;
		m_partWidth = getWidth() / amount;
		m_partHeight = getHeight() / amount;
		int partDestinationAdvance = m_grace * 2;
		int partXOffset = 0;
		int sequence = 0;
		m_partsBySequence = new ArrayList<PuzzlePart>();

		LinkedList<PuzzlePart> parts = new LinkedList<PuzzlePart>();
		for (int row = 0; row < amount; row++) {
			for (int col = 0; col < amount; col++) {

				Rect source = new Rect(col * partSourceWidth, row
						* partSourceHeight, (col + 1) * partSourceWidth,
						(row + 1) * partSourceHeight);

				Rect destination = createLocation(partDestinationAdvance,
						partXOffset, sequence);

				PuzzlePart part = new PuzzlePart(this, bitmap, source,
						destination, sequence);

				m_partsBySequence.add(sequence, part);
				parts.add(part);
				sequence++;
				if (sequence % 10 == 0) {
					partXOffset += m_partWidth / 2;
				}
			}
		}
		return parts;
	}

	private Rect createLocation(int partsAdvance, int partXOffset,
			int partSequence) {

		if (m_savedStatus == null) {

			int startPointY = (partSequence % 10) * partsAdvance;
			int startPointX = startPointY + partXOffset;
			Rect destination = new Rect(partXOffset + startPointX, startPointY,
					partXOffset + startPointX + m_partWidth, startPointY
							+ m_partHeight);
			return destination;
		}

		return m_savedStatus.getPartLocation(m_partWidth, m_partHeight,
				partSequence, getWidth(), getHeight());
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
		try {
			return onTouchWorker(event);
		} catch (Exception e) {
			m_utils.handleError(e);
			return false;
		}
	}

	private boolean onTouchWorker(MotionEvent event) throws Exception {

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

	private boolean handleMove(int eventX, int eventY) throws Exception {
		if (m_selectedPart == null) {
			return false;
		}

		m_selectedPart.move(eventX, eventY);
		m_isMove = true;
		invalidate();
		return true;
	}

	private boolean handleUp() throws Exception {
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
			m_selectedPart.rotate(false);
		}

		int matching = m_selectedPart.matchNeighbours();
		if (matching > 0) {
			m_scoresDraw.addScore(matching, m_selectedPart, false);
			sendScoreEvent(matching, m_selectedPart);
			if (isAllGlued()) {
				handleDone();
			} else {
				m_utils.playSound(R.raw.join);
			}
		}
		invalidate();
		m_selectedPart = null;
		return true;
	}

	private void sendScoreEvent(int matching, PuzzlePart part) throws Exception {
		if (m_networkGame == null) {
			return;
		}
		ScoreEvent event = new ScoreEvent();
		event.matchingAmount = matching;
		event.sequence = part.getSequence();
		m_networkGame.sendMessage(true, event);
	}

	public boolean isAllGlued() {
		if (m_parts == null) {
			return false;
		}
		if (m_parts.get(0).getGlued().size() != m_parts.size()) {
			return false;
		}
		return true;
	}

	private void handleDone() {
		if (m_isDone) {
			return;
		}

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
		dialog.setCanceledOnTouchOutside(false);
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
				if (m_networkGame != null) {
					m_networkGame.leaveRoom(false);
				}
				dialog.dismiss();
				m_fragment.getMainActivity().setFragmentMain();
			}

			private void updateGooglePlay(int newScore) {
				if (!m_fragment.getMainActivity().isSignedIn()) {
					return;
				}

				String boardId = m_fragment.getMainActivity().getString(
						R.string.leaderboard_id);
				Games.Leaderboards.submitScore(m_fragment.getApiClient(),
						boardId, newScore);

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
				String achievement = m_fragment.getMainActivity().getString(
						achievementId);
				Games.Achievements.unlock(m_fragment.getApiClient(),
						achievement);
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

	public FragmentNetworkGame getNetworkGame() {
		return m_networkGame;
	}

	public void updateFromNetwork(PartStatus status, boolean isReliable)
			throws Exception {

		if (m_parts == null) {
			return;
		}
		PuzzlePart part = m_partsBySequence.get(status.sequence);
		if (part == null) {
			throw new PuzzleException("part sequence " + status.sequence
					+ " not found");
		}

		Rect newLocation = status.getPartLocation(getWidth(), getHeight(),
				m_partWidth, m_partHeight);
		part.updateFromNetwork(newLocation, status, isReliable,
				m_partsBySequence);

		if (isAllGlued()) {
			handleDone();
		}

		invalidate();
	}

	public void updateScoreFromNetwork(ScoreEvent event) throws Exception {

		if (m_parts == null) {
			return;
		}
		PuzzlePart part = m_partsBySequence.get(event.sequence);
		if (part == null) {
			throw new PuzzleException("part sequence " + event.sequence
					+ " not found");
		}

		m_scoresDraw.addScore(event.matchingAmount, part, true);
		invalidate();
	}

}
