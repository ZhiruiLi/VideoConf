package com.example.zhiruili.videoconf;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.zhiruili.videoconf.data.RecentCallsDbHelper;
import com.example.zhiruili.videoconf.data.RecentCallsListContract.*;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class RecentCallsFragment extends Fragment {

    private OnFragmentInteractionListener mInteractionListener;
    private RecentCallsListAdapter mListAdapter = null;
    private SQLiteDatabase mDb;

    public RecentCallsFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_recent_calls, container, false);
        final RecyclerView callsListView = (RecyclerView) rootView.findViewById(R.id.rv_recent_calls_list);
        callsListView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));

        Single
                .fromCallable(() -> new RecentCallsDbHelper(getActivity()).getWritableDatabase())
                .doOnSuccess(db -> mDb = db)
                // .doOnSuccess(TestHelper::insertFakeData)
                .map(_ignore -> getAllRecentCalls())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(cursor -> new RecentCallsListAdapter(rootView.getContext(), cursor, mInteractionListener::onListItemClick))
                .doOnSuccess(adapter -> mListAdapter = adapter)
                .doOnSuccess(callsListView::setAdapter)
                .doOnError(Throwable::printStackTrace)
                .subscribe();

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public interface OnFragmentInteractionListener {
        void onListItemClick(String callId);
    }

    public Single<Boolean> updateCalls(List<String> callIds, boolean isCallIn) {
        return Single
                .fromCallable(() -> {
                    mDb.beginTransaction();
                    for (String id : callIds) {
                        mDb.delete(RecentCallsListEntry.TABLE_NAME, RecentCallsListEntry.COLUMN_PERSON_ID + "=?", new String[] { id });
                    }
                    for (String id : callIds) {
                        ContentValues cv = new ContentValues();
                        cv.put(RecentCallsListEntry.COLUMN_PERSON_ID, id);
                        cv.put(RecentCallsListEntry.COLUMN_IS_CALL_IN, isCallIn);
                        mDb.insert(RecentCallsListEntry.TABLE_NAME, null, cv);
                    }
                    mDb.setTransactionSuccessful();
                    return true;
                })
                .doFinally(() -> mDb.endTransaction())
                .map(_ignore -> getAllRecentCalls())
                .doOnError(Throwable::printStackTrace)
                .subscribeOn(Schedulers.io())
                .doOnSuccess(mListAdapter::swapCursor)
                .subscribeOn(AndroidSchedulers.mainThread())
                .map(_ignore -> true)
                .onErrorReturn(err -> {
                    err.printStackTrace();
                    return false;
                });
    }

    public Cursor getAllRecentCalls() {
        return mDb.query(
                RecentCallsListEntry.TABLE_NAME,
                null, null, null, null, null,
                RecentCallsListEntry.COLUMN_CALL_TIME + " DESC");
    }
}
