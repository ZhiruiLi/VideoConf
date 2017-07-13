package com.example.zhiruili.videoconf;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jakewharton.rxbinding2.view.RxView;
import com.tencent.ilivesdk.view.AVRootView;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public final class CallFragment extends Fragment {

    private static final String ARG_CALL_ID = "call_id";
    private static final String ARG_SPONSOR = "sponsor";
    private static final String ARG_MEMBERS = "members";

    private int mCallId;
    private String mSponsor;
    private ArrayList<String> mMembers;

    private OnFragmentInteractionListener mInteractionListener;

    public CallFragment() { }

    public static CallFragment newInstance(int callId, String sponsor, ArrayList<String> members) {
        CallFragment fragment = new CallFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CALL_ID, callId);
        args.putString(ARG_SPONSOR, sponsor);
        args.putStringArrayList(ARG_MEMBERS, members);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCallId = getArguments().getInt(ARG_CALL_ID);
            mSponsor = getArguments().getString(ARG_SPONSOR);
            mMembers = getArguments().getStringArrayList(ARG_MEMBERS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_call, container, false);
        final AVRootView mAvRootView = (AVRootView) rootView.findViewById(R.id.call_av_root_view);
        mInteractionListener.onCreateAvRootView(mAvRootView);

        RxView
                .clicks(rootView.findViewById(R.id.btn_end_call))
                .throttleFirst(getResources().getInteger(R.integer.shake_throttle), TimeUnit.MICROSECONDS)
                .subscribe(_ignore -> mInteractionListener.onEndCall(mCallId, mSponsor, mMembers));

        final View turnOffCameraBtn = rootView.findViewById(R.id.btn_turn_off_camera);
        final View turnOnCameraBtn = rootView.findViewById(R.id.btn_turn_on_camera);

        RxView
                .clicks(turnOffCameraBtn)
                .throttleFirst(getResources().getInteger(R.integer.shake_throttle), TimeUnit.MICROSECONDS)
                .doOnNext(_ignore -> turnOnCameraBtn.setVisibility(View.VISIBLE))
                .doOnNext(_ignore -> turnOffCameraBtn.setVisibility(View.GONE))
                .subscribe(_ignore -> mInteractionListener.onSwitchCamera(false));

        RxView
                .clicks(turnOnCameraBtn)
                .throttleFirst(getResources().getInteger(R.integer.shake_throttle), TimeUnit.MICROSECONDS)
                .doOnNext(_ignore -> turnOffCameraBtn.setVisibility(View.VISIBLE))
                .doOnNext(_ignore -> turnOnCameraBtn.setVisibility(View.GONE))
                .subscribe(_ignore -> mInteractionListener.onSwitchCamera(true));

        return rootView;
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

    public interface OnFragmentInteractionListener {
        void onEndCall(int callId, String sponsor, ArrayList<String> members);
        void onCreateAvRootView(AVRootView avView);
        void onSwitchCamera(boolean enableCamera);
    }
}
