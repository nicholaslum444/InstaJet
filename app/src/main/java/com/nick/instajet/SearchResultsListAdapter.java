package com.nick.instajet;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Nick on 12/5/2015.
 */
public class SearchResultsListAdapter extends BaseAdapter implements ListAdapter {

    private final Activity caller;
    private final JSONArray results;

    private SearchResultsListAdapter(Activity caller, JSONArray results) {
        super();
        this.results = results;
        this.caller = caller;
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
        View view = convertView; // re-use an existing view, if one is available
        if (view == null) {
            view = caller.getLayoutInflater().inflate(R.layout.listitem_search_result, null);
        }
        JSONObject user = results.optJSONObject(position);
        setupResultRow(view, user);
        return view;
    }

    private void setupResultRow(View rowView, JSONObject user) {
        ;
    }
}
