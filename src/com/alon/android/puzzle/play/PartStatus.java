package com.alon.android.puzzle.play;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedList;

import android.graphics.Rect;

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
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		ObjectOutputStream output = new ObjectOutputStream(bytes);
		output.writeObject(this);
		output.flush();

		byte[] result = bytes.toByteArray();

		output.close();
		bytes.close();

		return result;
	}

	public static PartStatus fromBytes(byte[] data) throws Exception {
		ByteArrayInputStream bytes = new ByteArrayInputStream(data);
		ObjectInputStream in = new ObjectInputStream(bytes);
		PartStatus status = (PartStatus) in.readObject();
		in.close();
		bytes.close();
		return status;
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
