package com.nick.instajet;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Nick on 12/5/2015.
 */
public class SearchResultsListAdapter extends BaseAdapter implements ListAdapter {

    private final Activity caller;
    private final JSONArray results;

    private HashMap<String, Bitmap> profilePicCache;

    SearchResultsListAdapter(Activity caller, JSONArray results) {
        super();
        this.results = results;
        this.caller = caller;
        this.profilePicCache = new HashMap<>();
    }

    @Override
    public int getCount() {
        return results.length();
    }

    @Override
    public Object getItem(int position) {
        return results.opt(position);
    }

    @Override
    public long getItemId(int position) {
        JSONObject user = results.optJSONObject(position);
        return user.optLong("id");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
//        View view = convertView; // re-use an existing view, if one is available
//        if (view == null) {
//            view = caller.getLayoutInflater().inflate(R.layout.listitem_search_result, null);
//        }
        View view = caller.getLayoutInflater().inflate(R.layout.listitem_search_result, null);
        JSONObject user = results.optJSONObject(position);
        setupResultRow(view, user);
        return view;
    }

    private void setupResultRow(View rowView, JSONObject user) {
        TextView textViewUsername = (TextView) rowView.findViewById(R.id.TextViewUsername);
        String username = user.optString("username", "(Username not available)");
        textViewUsername.setText(username);

        TextView textViewFullname = (TextView) rowView.findViewById(R.id.TextViewFullname);
        String fullname = user.optString("full_name", "(Full name not available)");
        textViewFullname.setText(fullname);

        ImageView imageViewProfilePic = (ImageView) rowView.findViewById(R.id.ImageViewProfilePic);
        String profilePicUrl = user.optString("profile_picture");
        loadProfilePic(imageViewProfilePic, profilePicUrl);

    }

    private void loadProfilePic(ImageView imageViewProfilePic, String profilePicUrl) {
        if (profilePicCache.containsKey(profilePicUrl)) {
            imageViewProfilePic.setImageBitmap(profilePicCache.get(profilePicUrl));
        } else {
            new ProfilePicLoaderTask(imageViewProfilePic, profilePicUrl, profilePicCache).execute();
        }
    }
}
