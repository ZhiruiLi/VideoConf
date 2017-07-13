package com.example.zhiruili.videoconf;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public final class RecentCallsFragment extends Fragment {

    private OnFragmentInteractionListener mInteractionListener;

    private RecyclerView mRecentCallsList;

    public RecentCallsFragment() { }

    public static RecentCallsFragment newInstance() {
        RecentCallsFragment fragment = new RecentCallsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) { }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recent_calls, container, false);
        bindViews(rootView);
        return rootView;
    }

    private void bindViews(View rootView) {
        mRecentCallsList = (RecyclerView) rootView.findViewById(R.id.rv_recent_calls_list);
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
    }
}
