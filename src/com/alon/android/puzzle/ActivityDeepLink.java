package com.alon.android.puzzle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class ActivityDeepLink extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		new Utils(this).debug("got here from " + getClass().getSimpleName());

		Intent route = new Intent();
		route.setClass(getApplicationContext(), ActivityMain.class);
		startActivity(route);
		finish();
	}

}