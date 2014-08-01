package com.alon.android.puzzle;

import java.io.Serializable;
import java.util.LinkedList;

public class PartStatus implements Serializable {
	private static final long serialVersionUID = 1269121220218212451L;

	public float xPercent;
	public float yPercent;
	public int sequence;
	public int rotation;

	public LinkedList<Integer> glued = new LinkedList<Integer>();

}
