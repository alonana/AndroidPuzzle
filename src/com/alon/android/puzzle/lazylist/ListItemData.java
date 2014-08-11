package com.alon.android.puzzle.lazylist;

public class ListItemData {

	private String m_name;
	private String m_urlSmall;
	private String m_urlBig;

	public ListItemData(String name, String urlSmall, String urlBig) {
		m_name = name;
		m_urlBig = urlBig;
		m_urlSmall = urlSmall;
	}

	public String getUrlSmall() {
		return m_urlSmall;
	}

	public String getName() {
		return m_name;
	}

	public String getUrlBig() {
		return m_urlBig;
	}
}
