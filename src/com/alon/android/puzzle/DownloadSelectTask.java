package com.alon.android.puzzle;

import android.os.AsyncTask;

import com.alon.android.puzzle.lazylist.ListItemData;

public class DownloadSelectTask extends AsyncTask<ListItemData, Integer, Void> {

	private FragmentDownload m_fragmentDownload;

	public DownloadSelectTask(FragmentDownload fragmentDownload) {
		m_fragmentDownload = fragmentDownload;
	}

	@Override
	protected Void doInBackground(ListItemData... items) {
		try {
			m_fragmentDownload.downloadInBackground(items[0]);
		} catch (Exception e) {
			m_fragmentDownload.getUtils().handleError(e);
		}
		return null;
	}

}
