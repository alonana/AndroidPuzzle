package com.alon.android.puzzle;

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

	public void restoreParts(LinkedList<PuzzlePart> parts) {

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

	public Rect getPartLocation(int partDestinationWidth,
			int partDestinationHeight, int partSequence, int totalWidth,
			int totalHeight) {

		PartStatus status = m_statuses.get(partSequence);
		int startX = (int) (status.xPercent * totalWidth);
		int startY = (int) (status.yPercent * totalHeight);
		Rect restored = new Rect(startX, startY, startX + partDestinationWidth,
				startY + partDestinationHeight);
		return restored;
	}
}
