package com.nick.instajet;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.EditText;
import android.widget.Toast;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.prefs.Preferences;

public class Home
		extends Activity
		implements UrlFragment.OnFragmentInteractionListener,
			SearchFragment.OnFragmentInteractionListener {

	private FragmentManager fm = getFragmentManager();

	private HashMap<String, Fragment> allFragments = new HashMap<>();

	private static final String URL_FRAG_TAG = "urlFragTag";
	private static final String SEARCH_FRAG_TAG = "searchFragTag";
	
	/** ON CREATE */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.e("asd", "inside oncreate");
		if (savedInstanceState != null) {
			Log.e("asd", savedInstanceState.toString());
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		boolean isLoggedIn = getSharedPreferences("InstaJetPrefs", MODE_PRIVATE).getBoolean("isLoggedIn", false);

		if (isLoggedIn) {
			// go to url download by default
			onClickButtonUrl(null);
		} else {
			// show login page
			Intent intent = new Intent(this, Login.class);
			startActivity(intent);
			finish();
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		//super.onNewIntent(intent);
		Log.e("asd", "newintent called");
	}

	@Override
	protected void onDestroy() {
		Log.e("asd", "destroying");
		super.onDestroy();
	}

	@Override
	public void onFragmentInteraction(Uri uri) {
		// required method for OnFragmentInteractionListener interface
	}

	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}*/

	/*@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}*/

	public void onClickButtonUrl(View v) {
		FragmentTransaction ft = fm.beginTransaction();
		UrlFragment urlFrag = (UrlFragment) getFrag(URL_FRAG_TAG);
		ft.replace(R.id.LayoutFragment, urlFrag, URL_FRAG_TAG);
		ft.commit();
	}

	public void onClickButtonSearch(View v) {
		FragmentTransaction ft = fm.beginTransaction();
		SearchFragment searchFrag = (SearchFragment) getFrag(SEARCH_FRAG_TAG);
		ft.replace(R.id.LayoutFragment, searchFrag, SEARCH_FRAG_TAG);
		ft.commit();
	}

	private Fragment getFrag(String fragTag) {
		if (allFragments.containsKey(fragTag)) {
			return allFragments.get(fragTag);
		} else {
			Fragment frag;
			switch (fragTag) {
				case URL_FRAG_TAG :
					frag = UrlFragment.newInstance("asd", "asd");
					break;

				case SEARCH_FRAG_TAG :
					frag = SearchFragment.newInstance("asd", "asd");
					break;

				default :
					Log.e("asd", "no such tag");
					frag = UrlFragment.newInstance("asd", "asd");
					break;
			}
			allFragments.put(fragTag, frag);
			return frag;
		}
	}

}
