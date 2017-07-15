package com.example.zhiruili.videoconf;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.zhiruili.videoconf.data.RecentCallsListContract.*;

public class RecentCallsListAdapter extends RecyclerView.Adapter<RecentCallsListAdapter.RecentCallViewHolder> {

    private Context mContext;
    private Cursor mCursor;
    private OnItemClickListener mClickListener;

    public RecentCallsListAdapter(Context context, Cursor cursor, OnItemClickListener listener) {
        mContext = context;
        mCursor = cursor;
        mClickListener = listener;
    }

    @Override
    public RecentCallViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.recent_calls_list_item, parent, false);
        return new RecentCallViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecentCallViewHolder holder, int position) {
        if (!mCursor.moveToPosition(position)) {
            return;
        }
        int idxCallId = mCursor.getColumnIndex(RecentCallsListEntry.COLUMN_PERSON_ID);
        String callId = mCursor.getString(idxCallId);
        int idxIsCallIn = mCursor.getColumnIndex(RecentCallsListEntry.COLUMN_IS_CALL_IN);
        boolean isCallIn = mCursor.getInt(idxIsCallIn) != 0;
        int idxCallTime = mCursor.getColumnIndex(RecentCallsListEntry.COLUMN_CALL_TIME);
        String callTimeString = mCursor.getString(idxCallTime);
        holder.callId.setText(callId);
        holder.callTime.setText(callTimeString);
        holder.callInOrOut.setText(isCallIn ? mContext.getString(R.string.label_call_in) : mContext.getString(R.string.label_call_out));
    }

    void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    class RecentCallViewHolder extends RecyclerView.ViewHolder {

        TextView callId;
        TextView callTime;
        TextView callInOrOut;

        public RecentCallViewHolder(View itemView) {
            super(itemView);
            callId = (TextView) itemView.findViewById(R.id.tv_call_id);
            callTime = (TextView) itemView.findViewById(R.id.tv_call_time);
            callInOrOut = (TextView) itemView.findViewById(R.id.tv_call_in_or_out);
            itemView.setOnClickListener(v -> mClickListener.onClick(callId.getText().toString()));
        }
    }

    public interface OnItemClickListener {
        void onClick(String callId);
    }
}
