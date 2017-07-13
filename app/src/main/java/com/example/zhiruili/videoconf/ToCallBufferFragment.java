package com.example.zhiruili.videoconf;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

    private static final String ARG_INIT_ACCOUNTS = "init_accounts";

    private FlexboxLayout mWaitedToCalledBuffer;
    private EditText mCalledAccountEditText;
    private AppCompatImageButton mAddCalledAccountButton;
    private AppCompatButton mStartCallingButton;
    private ArrayList<String> mAccounts;
    private OnFragmentInteractionListener mInteractionListener;

    public ToCallBufferFragment() { }

    public static ToCallBufferFragment newInstance(ArrayList<String> initAccounts) {
        ToCallBufferFragment fragment = new ToCallBufferFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_INIT_ACCOUNTS, initAccounts);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mAccounts = getArguments().getStringArrayList(ARG_INIT_ACCOUNTS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_to_call_buffer, container, false);
        bindViews(rootView);
        initViews();
        addAccountsToBuffer(mAccounts);
        return rootView;
    }

    public void addAccountToBuffer(@NonNull final String account) {
        final int count = mWaitedToCalledBuffer.getFlexItemCount();
        View tagView = null;
        for (int i = 0; i < count; ++i) {
            if (account.equals(mWaitedToCalledBuffer.getFlexItemAt(i).getTag().toString())) {
                tagView = mWaitedToCalledBuffer.getFlexItemAt(i);
                mWaitedToCalledBuffer.removeViewAt(i);
                break;
            }
        }
        if (tagView == null) {
            tagView = ViewCreator.createTag(getActivity(), account, account, mWaitedToCalledBuffer::removeView);
        }
        mWaitedToCalledBuffer.addView(tagView);
    }

    private void addAccountsToBuffer(List<String> accounts) {
        if (accounts == null) {
            return;
        }
        accounts.forEach(this::addAccountToBuffer);
    }

    private void bindViews(View rootView) {
        mWaitedToCalledBuffer = (FlexboxLayout) rootView.findViewById(R.id.waited_to_called_buffer);
        mCalledAccountEditText = (EditText) rootView.findViewById(R.id.et_called_account_input);
        mAddCalledAccountButton = (AppCompatImageButton) rootView.findViewById(R.id.ib_add_called_account);
        mStartCallingButton = (AppCompatButton) rootView.findViewById(R.id.btn_start_calling);
    }

    private void initViews() {

        RxViewGroup
                .changeEvents(mWaitedToCalledBuffer)
                .map(ignore -> mWaitedToCalledBuffer.getFlexItemCount() > 0)
                .subscribe(show -> mStartCallingButton.setVisibility(show ? View.VISIBLE : View.GONE));

        RxTextView
                .afterTextChangeEvents(mCalledAccountEditText)
                .map(_ignore -> mCalledAccountEditText.getText().toString().trim())
                .map(text -> text.length() > 0)
                .subscribe(show -> mAddCalledAccountButton.setVisibility(show ? View.VISIBLE : View.GONE));

        RxView
                .clicks(mAddCalledAccountButton)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .map(_ignore -> mCalledAccountEditText.getText().toString().trim())
                .doOnNext(account -> {
                    if (!TextUtils.isUserNameValid(account)) {
                        mCalledAccountEditText.setError(getString(R.string.error_invalid_user_name));
                    }
                })
                .filter(TextUtils::isUserNameValid)
                .doOnNext(_ignore -> mCalledAccountEditText.setText(""))
                .subscribe(this::addAccountToBuffer);

        RxView
                .clicks(mStartCallingButton)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .filter(_ignore -> mWaitedToCalledBuffer.getFlexItemCount() > 0)
                .map(_ignore -> mWaitedToCalledBuffer.getChildCount())
                .map(count ->
                        new ArrayList<String>() {{
                            for (int i = 0; i < count; ++i) {
                                add(mWaitedToCalledBuffer.getFlexItemAt(i).getTag().toString());
                            }
                        }}
                )
                .subscribe(mInteractionListener::startCalling);
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
        void startCalling(List<String> calledAccounts);
    }
}
