package com.alon.android.puzzle;

import java.io.File;
import java.util.ArrayList;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.alon.android.puzzle.lazylist.LazyAdapter;
import com.alon.android.puzzle.lazylist.ListItemData;

public class FragmentDownload extends FragmentBase implements
		OnItemClickListener {

	private View m_topView;
	private ArrayList<ListItemData> m_items;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		getUtils().loadSound(R.raw.click);

		m_topView = inflater.inflate(R.layout.fragment_download, container,
				false);

		m_items = new ArrayList<ListItemData>();

		m_items.add(new ListItemData(
				"Horse",
				"http://4.bp.blogspot.com/-EjDAgMQ0TTE/U-hmIC00dnI/AAAAAAAAAuY/mRSvbg4Xb1A/s1600/horse_small.JPG",
				"http://3.bp.blogspot.com/-G4E-QwfjIp4/U-hmIBky5nI/AAAAAAAAAuc/l9QQLSCPzVA/s1600/horse.JPG"));

		m_items.add(new ListItemData(
				"Fish",
				"http://2.bp.blogspot.com/-m9bK_PBlFGQ/U-hoAlvM-7I/AAAAAAAAAus/b80FSS3nScg/s1600/fish_small.jpg",
				"http://1.bp.blogspot.com/-qhGMd4vuwZQ/U-hoDj3IXsI/AAAAAAAAAu0/nczuexISv1Q/s1600/fish.jpg"));

		m_items.add(new ListItemData(
				"Octopus",
				"http://3.bp.blogspot.com/-v8cl4GccwLk/U-ii_7mO4iI/AAAAAAAAAvE/3KNZEZ_Vxl8/s1600/octopus_small.jpg",
				"http://4.bp.blogspot.com/-bMf4s-oK8R8/U-ijDQ-ocAI/AAAAAAAAAvM/NEHZ8XiLJbA/s1600/octopus.jpg"));

		m_items.add(new ListItemData(
				"Lion",
				"http://3.bp.blogspot.com/-ZsnuYghYKdU/U-ijHhzU0rI/AAAAAAAAAvU/9Ieg7L3yNe8/s1600/lion_small.jpg",
				"http://4.bp.blogspot.com/-XurggFHXmxw/U-ijKFr89dI/AAAAAAAAAvc/le01I3Fio1M/s1600/lion.jpg"));

		m_items.add(new ListItemData(
				"Zebra",
				"http://2.bp.blogspot.com/-wu4SK2hy2CM/U-ijNhRTl4I/AAAAAAAAAvk/sYkcI-ywcMs/s1600/zebra_small.jpg",
				"http://1.bp.blogspot.com/-1tOwgE4ZZD8/U-ijQPzG3II/AAAAAAAAAvs/hT_zQ854E4c/s1600/zebra.jpg"));

		m_items.add(new ListItemData(
				"Spider",
				"http://2.bp.blogspot.com/-meUCtZPMWAc/U-ijTayG4MI/AAAAAAAAAv0/0Omx726VwOY/s1600/spider_small.jpg",
				"http://2.bp.blogspot.com/-uUMvD5Wv5j4/U-ijWsqp1oI/AAAAAAAAAv8/NDyxNa8_p3E/s1600/spider.jpg"));

		m_items.add(new ListItemData(
				"HummingBird",
				"http://2.bp.blogspot.com/-xNu5YoyTTgY/U-ijZ3KcjlI/AAAAAAAAAwE/MTNMauGLXlM/s1600/humming_small.jpg",
				"http://2.bp.blogspot.com/-LIhLrpLp47s/U-ijc7WqRlI/AAAAAAAAAwM/Mlowu_aBnls/s1600/humming.jpg"));

		m_items.add(new ListItemData(
				"Crow",
				"http://3.bp.blogspot.com/-p8wqKwScJ8c/U-ijgHNKLDI/AAAAAAAAAwU/6aQRacux_Vo/s1600/crow_small.jpg",
				"http://2.bp.blogspot.com/-Dkml9vahI9s/U-ijisHmDYI/AAAAAAAAAwc/bsJPV2ZEYXU/s1600/crow.jpg"));

		ListView list = (ListView) m_topView.findViewById(R.id.listDownload);
		ListAdapter adapter = new LazyAdapter(getMainActivity(), m_items);
		list.setAdapter(adapter);

		list.setOnItemClickListener(this);

		return m_topView;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		try {
			onItemClickWorker(position);
		} catch (Exception e) {
			getUtils().handleError(e);
		}

	}

	private void onItemClickWorker(int position) throws Exception {
		ListItemData item = m_items.get(position);
		new DownloadSelectTask(this).execute(item);
	}

	public void downloadInBackground(ListItemData item) throws Exception {
		File storage = getUtils().getStorageSubFolder("download");
		File image = new File(storage, item.getName());
		if (!image.exists()) {
			Utils.saveFileFromUrl(item.getUrlBig(), image);
		}
		getGameSettings().setImage(Uri.fromFile(image));
		getMainActivity().setFragmentNewGame();
	}

}
