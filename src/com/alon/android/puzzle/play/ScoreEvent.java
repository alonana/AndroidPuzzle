package com.alon.android.puzzle.play;

import java.io.Serializable;

public class ScoreEvent implements Serializable {
	private static final long serialVersionUID = 3019663610448950750L;

	public int sequence;
	public int matchingAmount;
}
