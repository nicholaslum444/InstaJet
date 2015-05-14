package com.nick.instajet;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import org.json.JSONException;
import org.json.JSONObject;


public class ProfilePage extends Activity implements ProfilePageFragment.OnFragmentInteractionListener {

    private static final String PROFILE_PAGE_FRAG_TAG = "profilePageFragTag";
    private FragmentManager fm = getFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        // get the profiledata json
        Intent intent = getIntent();
        String profileDataString = intent.getExtras().getString("profileDataString");
        JSONObject profileData = new JSONObject();
        try {
            profileData = new JSONObject(profileDataString);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("asd", "cannot make profile data from string");
        }

        // launch the fragment with the profiledata
        FragmentTransaction ft = fm.beginTransaction();
        Fragment profilePageFrag = ProfilePageFragment.newInstance(profileData);
        ft.replace(R.id.LayoutFragment, profilePageFrag, PROFILE_PAGE_FRAG_TAG);
        ft.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
