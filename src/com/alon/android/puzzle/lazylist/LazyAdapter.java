package com.alon.android.puzzle.lazylist;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.alon.android.puzzle.R;

public class LazyAdapter extends BaseAdapter {

	private static LayoutInflater m_inflater = null;

	private Activity m_activity;
	private ArrayList<ListItemData> m_elements;
	public ImageLoader m_imageLoader;

	public LazyAdapter(Activity activity, ArrayList<ListItemData> data) {
		m_activity = activity;
		m_elements = data;
		m_inflater = (LayoutInflater) m_activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		m_imageLoader = new ImageLoader(m_activity.getApplicationContext());
	}

	public int getCount() {
		return m_elements.size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("InflateParams")
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (convertView == null) {
			view = m_inflater.inflate(R.layout.lazy_item, null);
		}

		TextView text = (TextView) view.findViewById(R.id.text);
		ImageView image = (ImageView) view.findViewById(R.id.image);

		ListItemData element = m_elements.get(position);
		text.setText(element.getName());
		m_imageLoader.displayImage(element.getUrlSmall(), image);
		return view;
	}
}