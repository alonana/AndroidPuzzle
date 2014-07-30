package com.example.alon1;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends ActionBarActivity {

	private static final int SELECT_PHOTO = 100;

	private Utils m_utils;
	private Uri m_uri;

	public MainActivity() {
		m_utils = new Utils(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button b = (Button) findViewById(R.id.button2);
		b.setEnabled(false);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void getPicture(View view) {
		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		photoPickerIntent.setType("image/*");
		startActivityForResult(photoPickerIntent, SELECT_PHOTO);
	}

	public void startPuzzle(View view) {
		Intent intent = new Intent(this, PuzzleActivity.class);
		intent.putExtra(PuzzleActivity.URI, m_uri.toString());
		startActivity(intent);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent imageReturnedIntent) {

		super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

		switch (requestCode) {
		case SELECT_PHOTO:
			if (resultCode == RESULT_OK) {
				m_uri = imageReturnedIntent.getData();
				ImageView imageView = (ImageView) findViewById(R.id.imageView1);
				Bitmap yourSelectedImage = m_utils.decodeSampledBitmapFromUri(
						m_uri, imageView.getWidth(), imageView.getHeight());
				imageView.setImageBitmap(yourSelectedImage);

				Button b = (Button) findViewById(R.id.button2);
				b.setEnabled(true);
			}
		}
	}

}
