package com.nick.instajet;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
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
import java.util.prefs.Preferences;

public class Home
		extends Activity
		implements UrlFragment.OnFragmentInteractionListener,
			SearchFragment.OnFragmentInteractionListener {

	FragmentManager fm = getFragmentManager();
	
	/** ON CREATE */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		onClickButtonUrl(null);
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
		UrlFragment urlFrag = UrlFragment.newInstance("hello", "world");
		ft.replace(R.id.LayoutFragment, urlFrag, "urlFragTag");
		ft.commit();
	}

	public void onClickButtonSearch(View v) {
		FragmentTransaction ft = fm.beginTransaction();
		SearchFragment searchFrag = SearchFragment.newInstance("hello", "world");
		ft.replace(R.id.LayoutFragment, searchFrag, "searchFragTag");
		ft.commit();
	}

}
