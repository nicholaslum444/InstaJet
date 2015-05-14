package com.nick.instajet;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PhotoStreamFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PhotoStreamFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PhotoStreamFragment extends Fragment implements InstagramApiHandlerTaskListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_USER_ID = "userId";
    private static final String ARG_STREAM_TYPE = "streamType";

    // TODO: Rename and change types of parameters
    private String userId;
    private String streamType;
    private String accessToken;

    private String nextUrl;
    private JSONArray mediaList;
    private PhotoStreamListAdapter mediaListAdapter;

    private OnFragmentInteractionListener mListener;

    private static final String RECENT_FIRST_API_REQUEST_TEMPLATE = "https://api.instagram.com/v1/users/%1$s/media/recent?access_token=%2$s";
    private static final String RECENT_PAGINATED_API_REQUEST_TEMPLATE = "https://api.instagram.com/v1/users/%1$s/media/recent?access_token=%2$s&max_id=%3$s";

    public static final String STREAM_TYPE_USER = "user";
    public static final String STREAM_TYPE_FEED = "feed";
    public static final String STREAM_TYPE_POPULAR = "popular";

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param userId Parameter 1.
     * @param streamType Parameter 2.
     * @return A new instance of fragment PhotoStreamFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PhotoStreamFragment newInstance(String userId, String streamType) {
        PhotoStreamFragment fragment = new PhotoStreamFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        args.putString(ARG_STREAM_TYPE, streamType);
        fragment.setArguments(args);
        return fragment;
    }

    public PhotoStreamFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString(ARG_USER_ID);
            streamType = getArguments().getString(ARG_STREAM_TYPE);
        }
        accessToken = getActivity().getSharedPreferences("InstaJetPrefs", Context.MODE_PRIVATE).getString("accessToken", "notoken");
        mediaList = new JSONArray();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_photo_stream, container, false);

        return v;
    }

    @Override
    public void onViewCreated(View v, Bundle b) {
        super.onViewCreated(v, b);
        setupStream();
    }

    private void setupStream() {
        String firstApiUrl = "";

        switch (streamType) {
            case STREAM_TYPE_USER :
                firstApiUrl = String.format(RECENT_FIRST_API_REQUEST_TEMPLATE, userId, accessToken);
                break;

            default :
                Log.e("asd", "invalid stream type");

        }

        requestNextPhotoSet(firstApiUrl);
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

    private void requestNextPhotoSet(String nextUrl) {
        if (nextUrl == null) {
            return;
        }

        setProgressBarVisibility(View.VISIBLE);
        setLoadMoreButtonVisibility(View.GONE);

        Log.e("asd", nextUrl);
        makeToast(nextUrl);

        new InstagramApiHandlerTask(this).execute(nextUrl);
    }

    private void onReceiveNextPhotoSet(JSONObject response) {
        Log.e("asd", response.toString());

        setProgressBarVisibility(View.GONE);

        try {
            JSONObject meta = response.getJSONObject("meta");
            int responseCode = meta.getInt("code");
            if (responseCode == 200) {
                // all ok
                populateStream(response);

            } else if (responseCode == 400) {
                // permission error here
                setRestrictedWarningVisibility(View.VISIBLE);

            } else {
                Log.e("asd", "unknown errror: " + responseCode);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void populateStream(JSONObject response) {
        try {

            // settle pagination
            JSONObject pagination = response.getJSONObject("pagination");
            String nextUrlTemp = pagination.optString(nextUrl);
            if (nextUrlTemp == null) {
                setLoadMoreButtonVisibility(View.GONE);
                nextUrl = null;
            } else {
                nextUrl = pagination.getString("next_url");
                setLoadMoreButtonVisibility(View.VISIBLE);
            }

            // update the main medialist
            JSONArray mediaListUpdates = response.getJSONArray("data");
            for (int i = 0; i < mediaListUpdates.length(); i++) {
                mediaList.put(mediaListUpdates.getJSONObject(i));
            }

            // set the adapter if none, else call update on it
            if (mediaListAdapter == null) {
                mediaListAdapter = new PhotoStreamListAdapter(getActivity(), mediaList);
                ListView listViewPhotoStream = (ListView) getView().findViewById(R.id.ListViewPhotoStream);
                listViewPhotoStream.setAdapter(mediaListAdapter);
            } else {
                mediaListAdapter.notifyDataSetChanged();
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("asd", "jsondied");
        }
    }




    private void showRestrictedWarning(View v) {
        LinearLayout layoutRestrictedWarning = (LinearLayout) v.findViewById(R.id.LayoutRestrictedWarning);
        layoutRestrictedWarning.setVisibility(View.VISIBLE);
    }

    @Override
    public void receiveApiResponse(JSONObject j) {
        // TODO
        if (j == null) {
            Log.e("asd", "j is null");
        } else {
            onReceiveNextPhotoSet(j);
        }
    }

    private void makeToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }

    private void setLoadMoreButtonVisibility(int visibility) {
        Button buttonPhotoStreamLoadMore = (Button) getView().findViewById(R.id.ButtonPhotoStreamLoadMore);
        buttonPhotoStreamLoadMore.setVisibility(visibility);
    }

    private void setProgressBarVisibility(int visibility) {
        ProgressBar progressBarLoadProgress = (ProgressBar) getView().findViewById(R.id.ProgressBarLoadProgress);
        progressBarLoadProgress.setVisibility(visibility);
    }

    private void setRestrictedWarningVisibility(int visibility) {
        LinearLayout layoutRestrictedWarning = (LinearLayout) getView().findViewById(R.id.LayoutRestrictedWarning);
        layoutRestrictedWarning.setVisibility(visibility);
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
