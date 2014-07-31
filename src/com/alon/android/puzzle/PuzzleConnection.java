package com.alon.android.puzzle;

public class PuzzleConnection {

	public PuzzlePart from;
	public PuzzlePart to;

	public PuzzleConnection(PuzzlePart from, PuzzlePart to) {
		this.from = from;
		this.to = to;
	}

	@Override
	public boolean equals(Object other) {
		return equals((PuzzleConnection) other);
	}

	public boolean equals(PuzzleConnection other) {
		return to.equals(other.to);
	}

	@Override
	public int hashCode() {
		return to.hashCode();
	}
}
