package com.nick.instajet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UrlFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UrlFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UrlFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

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
     * @return A new instance of fragment UrlFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UrlFragment newInstance(String param1, String param2) {
        UrlFragment fragment = new UrlFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public UrlFragment() {
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
        View v = inflater.inflate(R.layout.fragment_url, container, false);
        Button urlDlBtn = (Button) v.findViewById(R.id.ButtonDownloadByUrl);
        urlDlBtn.setOnClickListener(this);

        return v;
    }

    @Override
    public void onClick(View v) {
        Log.e("asd", "went into onclick");
        switch (v.getId()) {
            case R.id.ButtonDownloadByUrl :
                onClickButtonDownloadUrl(v);
                break;

            default :
                Toast.makeText(this.getActivity(), "no method bound", Toast.LENGTH_LONG).show();
                break;
        }
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

    public void onClickButtonDownloadUrl(View v) {
        boolean isLoggedIn = getActivity().getSharedPreferences("InstaJetPrefs", Context.MODE_PRIVATE).getBoolean("isLoggedIn", false);
        if (isLoggedIn) {
            EditText urlField = (EditText) getActivity().findViewById(R.id.EditTextUrlField);
            String urlString = urlField.getText().toString();
            DownloaderService.startUrlDownload(getActivity(), urlString);
            showDownloadingAlert();
        } else {
            Intent intent = new Intent(getActivity(), Login.class);
            startActivity(intent);
        }
    }

    private void showDownloadingAlert() {
        AlertDialog.Builder a = new AlertDialog.Builder(getActivity());
        a.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which){
                dialog.dismiss();
            }
        });
        a.setCancelable(false);
        a.setTitle("Downloading");
        a.setMessage("Download will start shortly");
        a.show();
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
