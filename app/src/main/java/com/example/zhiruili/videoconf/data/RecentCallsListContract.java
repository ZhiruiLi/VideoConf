package com.example.zhiruili.videoconf.data;

import android.provider.BaseColumns;

public class RecentCallsListContract {

    private RecentCallsListContract() { }

    public static final class RecentCallsListEntry implements BaseColumns {

        public static final String TABLE_NAME = "recent_calls_list";
        public static final String COLUMN_PERSON_ID = "person_id";
        public static final String COLUMN_CALL_TIME = "call_time";
        public static final String COLUMN_IS_CALL_IN = "is_call_in";
    }
}
