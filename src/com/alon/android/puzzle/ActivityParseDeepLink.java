package com.alon.android.puzzle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.plus.PlusShare;

public class ActivityParseDeepLink extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String deepLinkId = PlusShare.getDeepLinkId(this.getIntent());
		new Utils(this).debug("got here from " + getClass().getSimpleName()
				+ " " + deepLinkId);
		Intent target = parseDeepLinkId(deepLinkId);
		if (target != null) {
			startActivity(target);
		}

		finish();
	}

	/**
	 * Get the intent for an activity corresponding to the deep-link ID.
	 * 
	 * @param deepLinkId
	 *            The deep-link ID to parse.
	 * @return The intent corresponding to the deep-link ID.
	 */
	private Intent parseDeepLinkId(String deepLinkId) {
		Intent route = new Intent();
		if ("/pages/create".equals(deepLinkId)) {
			route.setClass(getApplicationContext(), ActivityMain.class);
		} else {
			// Fallback to the MainActivity in your app.
			route.setClass(getApplicationContext(), ActivityMain.class);
		}
		return route;
	}
}