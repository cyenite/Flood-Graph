package com.project.grace.floodmeterapp;

import android.content.Context;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import static com.project.grace.floodmeterapp.Constant.ERROR_KEY;
import static com.project.grace.floodmeterapp.Constant.ERROR_KEY_MESSAGE;


public class NoLocation extends Fragment {

    private OnFragmentInteractionListener mListener;
    private View rootView;
    private ImageView image;
    private TextView message;

    public NoLocation() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_no_location, container, false);

        if(getArguments()!=null){
            image = rootView.findViewById(R.id.noLocationImage);
            message = rootView.findViewById(R.id.noLocationText);
            image.setImageResource(getArguments().getInt(ERROR_KEY));
            message.setText(getArguments().getString(ERROR_KEY_MESSAGE));
        }
        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
