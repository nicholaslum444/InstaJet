package com.nick.instajet;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.HashMap;

public class Home
		extends Activity
		implements UrlFragment.OnFragmentInteractionListener,
			SearchFragment.OnFragmentInteractionListener,
			SettingsFragment.OnFragmentInteractionListener {

	private FragmentManager fm = getFragmentManager();
	private HashMap<String, Fragment> fragmentCache = new HashMap<>();

	private static final long BACK_PRESS_DELAY_MILS = 2000;
	private long backPressedTimePrevious = 0;
	private Toast backPressedToast;

	private static final String URL_FRAG_TAG = "urlFragTag";
	private static final String SEARCH_FRAG_TAG = "searchFragTag";
	private static final String SELF_FRAG_TAG = "selfFragTag";
	private static final String SETTINGS_FRAG_TAG = "settingsFragTag";
	
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
			onClickButtonSearch(null);
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

	@Override
	public void onFragmentInteraction(String uri) {
		// required method for OnFragmentInteractionListener interface
	}

	@Override
	public void onBackPressed() {
		if (backPressedToast != null) {
			backPressedToast.cancel();
		}

		long backPressedTimeNow =  System.currentTimeMillis();

		if (backPressedTimeNow < (backPressedTimePrevious + BACK_PRESS_DELAY_MILS)) {
			// carry on exiting
			super.onBackPressed();
		} else {
			// wait for second press
			backPressedTimePrevious = backPressedTimeNow;
			backPressedToast = Toast.makeText(this, "Press BACK again to exit", Toast.LENGTH_SHORT);
			backPressedToast.show();
		}
	}



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

	public void onClickButtonSettings(View v) {
		FragmentTransaction ft = fm.beginTransaction();
		SettingsFragment settingsFrag = (SettingsFragment) getFrag(SETTINGS_FRAG_TAG);
		ft.replace(R.id.LayoutFragment, settingsFrag, SETTINGS_FRAG_TAG);
		ft.commit();
	}

	private Fragment getFrag(String fragTag) {
		if (fragmentCache.containsKey(fragTag)) {
			return fragmentCache.get(fragTag);
		} else {
			Fragment frag;
			switch (fragTag) {
				case URL_FRAG_TAG :
					frag = UrlFragment.newInstance("asd", "asd");
					break;

				case SEARCH_FRAG_TAG :
					frag = SearchFragment.newInstance("asd", "asd");
					break;

				case SETTINGS_FRAG_TAG :
					frag = SettingsFragment.newInstance("asd", "asd");
					break;

				default :
					Log.e("asd", "no such tag");
					frag = UrlFragment.newInstance("asd", "asd");
					break;
			}
			fragmentCache.put(fragTag, frag);
			return frag;
		}
	}

}
