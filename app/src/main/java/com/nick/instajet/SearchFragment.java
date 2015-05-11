package com.nick.instajet;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SearchFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment implements InstagramApiHandlerTaskListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final String SEARCH_REQUEST_TEMPLATE = "https://api.instagram.com/v1/users/search?q=%1$s&access_token=%2$s";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchFragment newInstance(String param1, String param2) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_search, container, false);

        EditText searchField = (EditText) v.findViewById(R.id.EditTextSearchField);
        searchField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch();
                    return true;
                }
                return false;
            }
        });

        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void performSearch() {
        EditText searchField = (EditText) getActivity().findViewById(R.id.EditTextSearchField);
        if (isEmpty(searchField) || isWhitespace(searchField)) {
            makeToast("Please enter a search term");
        } else {
            showLoadingSign();
            String searchTerm = searchField.getText().toString().trim();
            makeToast("Searching " + searchTerm);

            String accessToken = getActivity().getSharedPreferences("InstaJetPrefs", Context.MODE_PRIVATE).getString("accessToken", "notoken");

            // construct the api url
            String apiUrl = String.format(SEARCH_REQUEST_TEMPLATE, searchTerm, accessToken);

            // execute the json getter
            InstagramApiHandlerTask dataGetter = new InstagramApiHandlerTask(this);
            dataGetter.execute(apiUrl);
        }
    }

    private void showLoadingSign() {
        ProgressBar p = (ProgressBar) getActivity().findViewById(R.id.ProgressBarSearchProgress);
        p.setVisibility(View.VISIBLE);
        LinearLayout l = (LinearLayout) getActivity().findViewById(R.id.LinearLayoutSearchResults);
        l.setVisibility(View.GONE);
    }

    private void hideLoadingSign() {
        ProgressBar p = (ProgressBar) getActivity().findViewById(R.id.ProgressBarSearchProgress);
        p.setVisibility(View.GONE);
        LinearLayout l = (LinearLayout) getActivity().findViewById(R.id.LinearLayoutSearchResults);
        l.setVisibility(View.VISIBLE);
    }

    @Override
    public void receiveApiResponse(JSONObject o) {
        hideLoadingSign();
        if (o == null) {
            Log.e("asd", "o is null");
            makeToast("Server error. Please try again.");
        } else {
            Log.e("asd", o.toString());
            displayResults(o);
        }
    }

    private void displayResults(JSONObject o) {
        try {
            ArrayList<JSONObject> users = new ArrayList<>();

            JSONArray data = o.getJSONArray("data");
            for (int i = 0; i < data.length(); i++) {
                users.add(data.getJSONObject(i));
            }

            ListView resultsList = (ListView) getActivity().findViewById(R.id.ListViewSearchResults);
            ArrayAdapter<JSONObject> resultsAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, users);
            resultsList.setAdapter(resultsAdapter);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void makeToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }

    private boolean isWhitespace(EditText e) {
        return e.getText().toString().trim().length() == 0;
    }

    private boolean isEmpty(EditText e) {
        return e.getText().length() == 0;
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
