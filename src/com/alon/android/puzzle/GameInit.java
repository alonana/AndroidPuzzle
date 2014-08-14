package com.alon.android.puzzle;

import java.io.Serializable;
import java.util.Random;

import com.alon.android.puzzle.fragments.FragmentDownload;

public class GameInit implements Serializable {

	private static final long serialVersionUID = 1112501618970698819L;

	transient private boolean m_joined;
	transient private Utils m_utils;

	private short m_imageIndex;
	private short[] m_rotation;

	private GameInit() {

	}

	public GameInit(Utils utils, int pieces) {
		m_utils = utils;
		Random random = new Random();
		m_imageIndex = (short) random.nextInt(FragmentDownload.getImages()
				.size());
		m_rotation = new short[pieces * pieces];
		for (int piece = 0; piece < m_rotation.length; piece++) {
			m_rotation[piece] = (short) random.nextInt(4);
		}
		m_utils.debug("init " + this);

		m_joined = false;
	}

	public GameInit join(GameInit other) {

		if (m_joined) {
			throw new PuzzleException("join cannot be called more than once");
		}

		m_joined = true;

		GameInit joined = new GameInit();
		joined.m_utils = this.m_utils;

		joined.m_imageIndex = addMod(this.m_imageIndex, other.m_imageIndex,
				FragmentDownload.getImages().size());

		joined.m_rotation = new short[this.m_rotation.length];
		for (int piece = 0; piece < m_rotation.length; piece++) {
			joined.m_rotation[piece] = addMod(this.m_rotation[piece],
					other.m_rotation[piece], 4);
		}

		m_utils.debug("join with " + other);
		m_utils.debug("join result" + joined);
		return joined;
	}

	private short addMod(int i1, int i2, int mod) {
		int result = i1 + i2;
		result = result % mod;
		return (short) result;
	}

	public int getImageIndex() {
		return m_imageIndex;
	}

	public int getRotation(int piece) {
		return m_rotation[piece];
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("gameinit " + m_imageIndex + " ");

		for (int piece = 0; piece < m_rotation.length; piece++) {
			result.append(m_rotation[piece] + ",");
		}

		return result.toString();
	}
}
