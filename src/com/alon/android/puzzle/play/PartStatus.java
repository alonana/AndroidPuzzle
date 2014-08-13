package com.alon.android.puzzle.play;

import java.io.Serializable;
import java.util.LinkedList;

import android.graphics.Rect;

import com.alon.android.puzzle.Utils;

public class PartStatus implements Serializable {
	private static final long serialVersionUID = 1269121220218212451L;

	public float xPercent;
	public float yPercent;
	public int sequence;
	public int rotation;

	public LinkedList<Integer> glued = new LinkedList<Integer>();

	@Override
	public String toString() {
		return sequence + ":(" + xPercent + "," + yPercent + ") rotation "
				+ rotation;
	}

	public byte[] toBytes() throws Exception {
		return Utils.serializeObject(this);
	}

	public Rect getPartLocation(int totalWidth, int totalHeight, int partWidth,
			int partHeight) {
		int startX = (int) (xPercent * totalWidth);
		int startY = (int) (yPercent * totalHeight);
		Rect rect = new Rect(startX, startY, startX + partWidth, startY
				+ partHeight);
		return rect;
	}

}
