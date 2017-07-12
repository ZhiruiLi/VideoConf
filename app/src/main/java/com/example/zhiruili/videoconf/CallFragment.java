package com.example.zhiruili.videoconf;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class CallFragment extends Fragment {

    private static final String ARG_CALL_LIST = "call_list";

    private ArrayList<String> mCallList;

    private OnFragmentInteractionListener mInteractionListener;

    public CallFragment() { }

    public static CallFragment newInstance(ArrayList<String> callList) {
        CallFragment fragment = new CallFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_CALL_LIST, callList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCallList = getArguments().getStringArrayList(ARG_CALL_LIST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_call, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mInteractionListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mInteractionListener = null;
    }

    public interface OnFragmentInteractionListener { }
}
