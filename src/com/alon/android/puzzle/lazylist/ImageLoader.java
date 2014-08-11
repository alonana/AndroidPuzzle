package com.alon.android.puzzle.lazylist;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.widget.ImageView;

import com.alon.android.puzzle.R;
import com.alon.android.puzzle.Utils;

public class ImageLoader {

	private static final int stub_id = R.drawable.stub;

	private MemoryCache m_memoryCache = new MemoryCache();
	private FileCache m_fileCache;
	private Map<ImageView, String> m_imageViews = Collections
			.synchronizedMap(new WeakHashMap<ImageView, String>());
	private ExecutorService m_executorService;
	private Handler m_handler = new Handler();

	public ImageLoader(Context context) {
		m_fileCache = new FileCache(context);
		m_executorService = Executors.newFixedThreadPool(5);
	}

	public void displayImage(String url, ImageView imageView) {
		m_imageViews.put(imageView, url);
		Bitmap bitmap = m_memoryCache.get(url);
		if (bitmap != null) {
			imageView.setImageBitmap(bitmap);
		} else {
			queuePhoto(url, imageView);
			imageView.setImageResource(stub_id);
		}
	}

	private void queuePhoto(String url, ImageView imageView) {
		PhotoToLoad photo = new PhotoToLoad(url, imageView);
		m_executorService.submit(new PhotosLoader(photo));
	}

	private Bitmap getBitmap(String url) throws Exception {
		File f = m_fileCache.getFile(url);

		// from SD cache
		Bitmap bitmap = decodeFile(f);
		if (bitmap != null)
			return bitmap;

		// from web
		Utils.saveFileFromUrl(url, f);
		bitmap = decodeFile(f);
		return bitmap;
	}

	// decodes image and scales it to reduce memory consumption
	private Bitmap decodeFile(File file) {
		if (!file.exists()) {
			return null;
		}
		try {
			// decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			FileInputStream stream1 = new FileInputStream(file);
			BitmapFactory.decodeStream(stream1, null, o);
			stream1.close();

			// Find the correct scale value. It should be the power of 2.
			final int REQUIRED_SIZE = 70;
			int width_tmp = o.outWidth, height_tmp = o.outHeight;
			int scale = 1;
			while (true) {
				if (width_tmp / 2 < REQUIRED_SIZE
						|| height_tmp / 2 < REQUIRED_SIZE)
					break;
				width_tmp /= 2;
				height_tmp /= 2;
				scale *= 2;
			}

			// decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			FileInputStream stream2 = new FileInputStream(file);
			Bitmap bitmap = BitmapFactory.decodeStream(stream2, null, o2);
			stream2.close();
			return bitmap;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// Task for the queue
	private class PhotoToLoad {
		public String url;
		public ImageView imageView;

		public PhotoToLoad(String u, ImageView i) {
			url = u;
			imageView = i;
		}
	}

	class PhotosLoader implements Runnable {
		PhotoToLoad photoToLoad;

		PhotosLoader(PhotoToLoad photoToLoad) {
			this.photoToLoad = photoToLoad;
		}

		@Override
		public void run() {
			try {
				if (imageViewReused(photoToLoad))
					return;
				Bitmap bmp = getBitmap(photoToLoad.url);
				m_memoryCache.put(photoToLoad.url, bmp);
				if (imageViewReused(photoToLoad))
					return;
				BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
				m_handler.post(bd);
			} catch (Throwable th) {
				th.printStackTrace();
			}
		}
	}

	boolean imageViewReused(PhotoToLoad photoToLoad) {
		String tag = m_imageViews.get(photoToLoad.imageView);
		if (tag == null || !tag.equals(photoToLoad.url))
			return true;
		return false;
	}

	// Used to display bitmap in the UI thread
	class BitmapDisplayer implements Runnable {
		Bitmap bitmap;
		PhotoToLoad photoToLoad;

		public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
			bitmap = b;
			photoToLoad = p;
		}

		public void run() {
			if (imageViewReused(photoToLoad))
				return;
			if (bitmap != null)
				photoToLoad.imageView.setImageBitmap(bitmap);
			else
				photoToLoad.imageView.setImageResource(stub_id);
		}
	}

	public void clearCache() {
		m_memoryCache.clear();
		m_fileCache.clear();
	}

}
