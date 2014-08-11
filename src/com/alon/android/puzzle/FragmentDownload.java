package com.alon.android.puzzle;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.fedorvlasov.lazylist.LazyAdapter;

public class FragmentDownload extends FragmentBase {

	private View m_topView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		getUtils().loadSound(R.raw.click);

		m_topView = inflater.inflate(R.layout.fragment_download, container,
				false);

		String[] urls = new String[] {
				"http://4.bp.blogspot.com/-EjDAgMQ0TTE/U-hmIC00dnI/AAAAAAAAAuY/mRSvbg4Xb1A/s1600/horse_small.JPG",
				"http://2.bp.blogspot.com/-m9bK_PBlFGQ/U-hoAlvM-7I/AAAAAAAAAus/b80FSS3nScg/s1600/fish_small.jpg" };
		ListView list = (ListView) m_topView.findViewById(R.id.listDownload);
		ListAdapter adapter = new LazyAdapter(getMainActivity(), urls);
		list.setAdapter(adapter);

		return m_topView;
	}

}
