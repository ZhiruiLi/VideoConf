package com.example.zhiruili.videoconf;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.zhiruili.videoconf.utils.TextUtils;
import com.example.zhiruili.videoconf.utils.ViewCreator;
import com.google.android.flexbox.FlexboxLayout;
import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.view.RxViewGroup;
import com.jakewharton.rxbinding2.widget.RxTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class ToCallBufferFragment extends Fragment {

    private static final String ARG_INIT_IDS = "init_ids";

    private FlexboxLayout mWaitedToCalledBuffer;
    private EditText mCalledIdEditText;
    private AppCompatImageButton mAddCalledIdButton;
    private AppCompatButton mStartCallingButton;
    private ArrayList<String> mInitCallIds;
    private OnFragmentInteractionListener mInteractionListener;

    public ToCallBufferFragment() { }

    public static ToCallBufferFragment newInstance(ArrayList<String> initIds) {
        ToCallBufferFragment fragment = new ToCallBufferFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_INIT_IDS, initIds);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mInitCallIds = args.getStringArrayList(ARG_INIT_IDS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_to_call_buffer, container, false);
        bindViews(rootView);
        initViews();
        addIdsToBuffer(mInitCallIds);
        return rootView;
    }

    public void addIdToBuffer(@NonNull final String id) {
        final int count = mWaitedToCalledBuffer.getFlexItemCount();
        if (count >= AppConstants.MAX_CALL_NUM) {
            Snackbar.make(mWaitedToCalledBuffer, R.string.error_unable_to_add_more_members, Snackbar.LENGTH_SHORT).show();
            return;
        }
        View tagView = null;
        for (int i = 0; i < count; ++i) {
            if (id.equals(mWaitedToCalledBuffer.getFlexItemAt(i).getTag().toString())) {
                tagView = mWaitedToCalledBuffer.getFlexItemAt(i);
                mWaitedToCalledBuffer.removeViewAt(i);
                break;
            }
        }
        if (tagView == null) {
            tagView = ViewCreator.createTag(getActivity(), id, id, mWaitedToCalledBuffer::removeView);
        }
        mWaitedToCalledBuffer.addView(tagView);
    }

    public void addIdsToBuffer(@Nullable List<String> accounts) {
        if (accounts == null) {
            return;
        }
        accounts.forEach(this::addIdToBuffer);
    }

    public void removeAllBufferIds() {
        mWaitedToCalledBuffer.removeAllViews();
    }

    public void removeBufferIdAt(int idx) {
        mWaitedToCalledBuffer.removeViewAt(idx);
    }

    public void removeBufferId(String id) {
        final int count = mWaitedToCalledBuffer.getFlexItemCount();
        for (int i = 0; i < count; ++i) {
            if (mWaitedToCalledBuffer.getFlexItemAt(i).getTag().toString().equals(id)) {
                mWaitedToCalledBuffer.removeViewAt(i);
                break;
            }
        }
    }

    private void bindViews(View rootView) {
        mWaitedToCalledBuffer = (FlexboxLayout) rootView.findViewById(R.id.waited_to_called_buffer);
        mCalledIdEditText = (EditText) rootView.findViewById(R.id.et_called_id_input);
        mAddCalledIdButton = (AppCompatImageButton) rootView.findViewById(R.id.ib_add_called_id);
        mStartCallingButton = (AppCompatButton) rootView.findViewById(R.id.btn_start_calling);
    }

    private void initViews() {

        RxViewGroup
                .changeEvents(mWaitedToCalledBuffer)
                .map(ignore -> mWaitedToCalledBuffer.getFlexItemCount() > 0)
                .subscribe(show -> mStartCallingButton.setVisibility(show ? View.VISIBLE : View.GONE));

        RxTextView
                .afterTextChangeEvents(mCalledIdEditText)
                .map(_ignore -> mCalledIdEditText.getText().toString().trim())
                .map(text -> text.length() > 0)
                .subscribe(show -> mAddCalledIdButton.setVisibility(show ? View.VISIBLE : View.GONE));

        RxView
                .clicks(mAddCalledIdButton)
                .throttleFirst(getResources().getInteger(R.integer.shake_throttle), TimeUnit.MILLISECONDS)
                .map(_ignore -> mCalledIdEditText.getText().toString().trim())
                .doOnNext(id -> {
                    if (!TextUtils.isUserNameValid(id)) {
                        mCalledIdEditText.setError(getString(R.string.error_invalid_user_name));
                    }
                })
                .filter(TextUtils::isUserNameValid)
                .doOnNext(_ignore -> mCalledIdEditText.setText(""))
                .subscribe(this::addIdToBuffer);

        RxView
                .clicks(mStartCallingButton)
                .throttleFirst(getResources().getInteger(R.integer.shake_throttle), TimeUnit.MILLISECONDS)
                .filter(_ignore -> mWaitedToCalledBuffer.getFlexItemCount() > 0)
                .map(_ignore -> mWaitedToCalledBuffer.getChildCount())
                .map(count ->
                        new ArrayList<String>() {{
                            for (int i = 0; i < count; ++i) {
                                add(mWaitedToCalledBuffer.getFlexItemAt(i).getTag().toString());
                            }
                        }}
                )
                .subscribe(mInteractionListener::onStartCalling);
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
        void onStartCalling(ArrayList<String> calledIds);
    }
}
