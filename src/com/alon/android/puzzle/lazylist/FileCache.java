package com.alon.android.puzzle.lazylist;

import java.io.File;

import android.content.Context;

import com.alon.android.puzzle.Utils;

public class FileCache {

	private File m_folder;

	public FileCache(Context context) {
		m_folder = new Utils(context).getStorageFolder();
	}

	public File getFile(String url) {
		// I identify images by hashcode. Not a perfect solution, good for the
		// demo.
		String filename = String.valueOf(url.hashCode());
		// Another possible solution (thanks to grantland)
		// String filename = URLEncoder.encode(url);
		File f = new File(m_folder, filename);
		return f;

	}

	public void clear() {
		File[] files = m_folder.listFiles();
		if (files == null) {
			return;
		}
		for (File f : files) {
			f.delete();
		}
	}

}