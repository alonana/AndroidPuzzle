package com.alon.android.puzzle.play;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;

import android.annotation.SuppressLint;
import android.graphics.Rect;

public class PartsStatus implements Serializable {
	private static final long serialVersionUID = -2587966401414109582L;

	private HashMap<Integer, PartStatus> m_statuses;

	@SuppressLint("UseSparseArrays")
	public PartsStatus(LinkedList<PuzzlePart> m_parts) {

		m_statuses = new HashMap<Integer, PartStatus>();
		for (PuzzlePart part : m_parts) {
			PartStatus partStatus = part.getStatus();
			m_statuses.put(partStatus.sequence, partStatus);
		}

	}

	public void restoreParts(LinkedList<PuzzlePart> parts) throws Exception {

		for (int partIndex = 0; partIndex < parts.size(); partIndex++) {
			PuzzlePart part = parts.get(partIndex);
			PartStatus status = m_statuses.get(partIndex);
			part.rotateTo(status.rotation);
		}

		for (int partIndex = 0; partIndex < parts.size(); partIndex++) {
			PuzzlePart part = parts.get(partIndex);
			PartStatus status = m_statuses.get(partIndex);
			for (Integer gluedIndex : status.glued) {
				PuzzlePart glued = parts.get(gluedIndex);
				part.getGlued().add(glued);
			}
		}

	}

	public Rect getPartLocation(int partWidth, int partHeight,
			int partSequence, int totalWidth, int totalHeight) {

		PartStatus status = m_statuses.get(partSequence);
		Rect restored = status.getPartLocation(totalWidth, totalHeight,
				partWidth, partHeight);
		return restored;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (PartStatus status : m_statuses.values()) {
			result.append(status);
			result.append('\n');
		}
		return result.toString();
	}
}
