package com.nick.instajet;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProfilePageFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProfilePageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfilePageFragment extends Fragment implements View.OnClickListener, InstagramApiHandlerTaskListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PROFILE_DATA_STRING = "PROFILE_DATA_STRING";
    private static final String DEFAULT_USERNAME = "Username not available";
    private static final String DEFAULT_FULLNAME = "Full name not available";
    private static final String DEFAULT_PROFILE_PIC_URL = "invalid_url";

    private static final String DOWNLOAD_PROFILE_PIC_MESSAGE_TEMPLATE = "Download %1$s's profile picture?";

    private static final String RELATIONSHIP_API_REQUEST_TEMPLATE = "https://api.instagram.com/v1/users/%1$s/relationship?access_token=%2$s";

    // TODO: Rename and change types of parameters
    private JSONObject profileData;

    private OnFragmentInteractionListener mListener;

    private static final String PHOTO_STREAM_FRAG_TAG = "photoStreamFragTag";
    private FragmentManager fm;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param profileData Parameter 1.
     * @return A new instance of fragment ProfilePageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfilePageFragment newInstance(JSONObject profileData) {
        ProfilePageFragment fragment = new ProfilePageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PROFILE_DATA_STRING, profileData.toString());
        fragment.setArguments(args);
        return fragment;
    }

    public ProfilePageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            try {
                profileData = new JSONObject(getArguments().getString(ARG_PROFILE_DATA_STRING));
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("asd", "error making profile data from string");
                profileData = new JSONObject();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_profile_page, container, false);

        fm = getActivity().getFragmentManager();

        setupProfileDetails(v, profileData);
        setupRelationshipDetails(v, profileData);
        setupPhotoStream(v, profileData);

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

        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.app_name));
        }
    }

    private void setupRelationshipDetails(View v, JSONObject profileData) {
        TextView textViewRelationshipInvalid = (TextView) v.findViewById(R.id.TextViewRelationshipInvalid);
        TextView textViewRelationshipIncoming = (TextView) v.findViewById(R.id.TextViewRelationshipIncoming);
        TextView textViewRelationshipOutgoing = (TextView) v.findViewById(R.id.TextViewRelationshipOutgoing);
        TextView textViewRelationshipPrivate = (TextView) v.findViewById(R.id.TextViewRelationshipPrivate);
        textViewRelationshipInvalid.setVisibility(View.GONE);
        textViewRelationshipIncoming.setVisibility(View.GONE);
        textViewRelationshipOutgoing.setVisibility(View.GONE);
        textViewRelationshipPrivate.setVisibility(View.GONE);
        try {
            String userId = profileData.getString("id");
            String selfUserId = getActivity().getSharedPreferences("InstaJetPrefs", Context.MODE_PRIVATE).getString("userId", "");
            if (userId.equals(selfUserId)) {
                // no r/s details for self.
                textViewRelationshipInvalid.setVisibility(View.VISIBLE);
            } else {
                String accessToken = getActivity().getSharedPreferences("InstaJetPrefs", Context.MODE_PRIVATE).getString("accessToken", "notoken");
                try {
                    accessToken = URLEncoder.encode(accessToken, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    Log.e("asd", e.getMessage());
                    e.printStackTrace();
                }
                String apiUrl = String.format(RELATIONSHIP_API_REQUEST_TEMPLATE, userId, accessToken);
                new InstagramApiHandlerTask(this).execute(apiUrl);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("asd", "cannot get user id from profile data");
        }
    }

    private void continueSetupRelationshipDetails(JSONObject relationshipData) {
        TextView textViewRelationshipIncoming = (TextView) getActivity().findViewById(R.id.TextViewRelationshipIncoming);
        TextView textViewRelationshipOutgoing = (TextView) getActivity().findViewById(R.id.TextViewRelationshipOutgoing);
        TextView textViewRelationshipPrivate = (TextView) getActivity().findViewById(R.id.TextViewRelationshipPrivate);
        try {
            JSONObject data = relationshipData.getJSONObject("data");
            String outgoingStatus = data.getString("outgoing_status");
            String incomingStatus = data.getString("incoming_status");
            boolean isPrivate = data.getBoolean("target_user_is_private");

            if (incomingStatus.equals("followed_by")) {
                textViewRelationshipIncoming.setText("Following you");
                textViewRelationshipIncoming.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.presence_online, 0, 0, 0);
            } else {
                textViewRelationshipIncoming.setText("Not following you");
                textViewRelationshipIncoming.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.presence_offline, 0, 0, 0);
            }

            if (outgoingStatus.equals("follows")) {
                textViewRelationshipOutgoing.setText("You're following");
                textViewRelationshipOutgoing.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.presence_online, 0, 0, 0);
            } else {
                textViewRelationshipOutgoing.setText("You're not following");
                textViewRelationshipOutgoing.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.presence_offline, 0, 0, 0);
            }

            if (isPrivate) {
                textViewRelationshipPrivate.setText("Private profile");
                textViewRelationshipPrivate.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.presence_invisible, 0, 0, 0);
            } else {
                textViewRelationshipPrivate.setText("Public profile");
                textViewRelationshipPrivate.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.presence_online, 0, 0, 0);
            }

            textViewRelationshipOutgoing.setVisibility(View.VISIBLE);
            textViewRelationshipIncoming.setVisibility(View.VISIBLE);
            textViewRelationshipPrivate.setVisibility(View.VISIBLE);

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("asd", "cannot get user id from profile data");
        }
    }

    private void setupProfileDetails(View v, JSONObject profileData) {
        String username = DEFAULT_USERNAME;
        String fullname = DEFAULT_FULLNAME;
        String profilePicUrl = DEFAULT_PROFILE_PIC_URL;
        try {
            username = profileData.getString("username");
            fullname = profileData.getString("full_name");
            profilePicUrl = profileData.getString("profile_picture");

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("asd", "unable to load username or fullname");
        }

        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(username);
        }

        TextView textViewUsername = (TextView) v.findViewById(R.id.TextViewUsername);
        textViewUsername.setText(username);
        TextView textViewFullname = (TextView) v.findViewById(R.id.TextViewFullname);
        textViewFullname.setText(fullname);

        ImageView imageViewProfilePic = (ImageView) v.findViewById(R.id.ImageViewProfilePic);
        new ImageViewLoaderTask(imageViewProfilePic, profilePicUrl).execute();

        imageViewProfilePic.setOnClickListener(this);
    }

    private void setupPhotoStream(View v, JSONObject profileData) {

        try {
            String userId = profileData.getString("id");
            // launch the fragment with the profiledata
            FragmentTransaction ft = fm.beginTransaction();
            Fragment photoStreamFrag = PhotoStreamFragment.newInstance(userId, PhotoStreamFragment.STREAM_TYPE_USER);
            ft.replace(R.id.LayoutPhotoStreamFragment, photoStreamFrag, PHOTO_STREAM_FRAG_TAG);
            ft.commit();

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("asd", "cannot get id from profiledata");
            makeToast("Something went wrong. The page may not load correctly.");
        }
    }

    private void onClickProfilePic(View v) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        String username = profileData.optString("username", DEFAULT_USERNAME);
        dialog.setMessage(String.format(DOWNLOAD_PROFILE_PIC_MESSAGE_TEMPLATE, username));
        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                downloadProfilePic();
            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        dialog.show();
    }

    private void downloadProfilePic() {
        DownloaderService.startProfilePicDownload(getActivity(), profileData.toString());
    }


    private void makeToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void receiveApiResponse(JSONObject j) {
        if (getActivity() == null) {
            return;
        }
        continueSetupRelationshipDetails(j);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ImageViewProfilePic :
                onClickProfilePic(v);
                break;

            default :
                makeToast(v.getId() + " Button not registered");
        }
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
