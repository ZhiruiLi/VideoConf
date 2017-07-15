package com.example.zhiruili.videoconf.data;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.example.zhiruili.videoconf.data.RecentCallsListContract.*;

import java.util.ArrayList;

public final class TestHelper {

    public static void insertFakeData(SQLiteDatabase db) {

        ArrayList<ContentValues> list = new ArrayList<ContentValues>() {{
            for (int i = 10; i < 60; ++i) {
                ContentValues cv = new ContentValues();
                cv.put(RecentCallsListEntry.COLUMN_PERSON_ID, "alonglongid_" + i);
                cv.put(RecentCallsListEntry.COLUMN_IS_CALL_IN, i % 3 == 0);
                cv.put(RecentCallsListEntry.COLUMN_CALL_TIME, "2017-01-01 12:14:" + i);
                add(cv);
            }
        }};
        try {
            db.beginTransaction();
            db.delete(RecentCallsListEntry.TABLE_NAME, null, null);
            for (ContentValues v : list) {
                db.insert(RecentCallsListEntry.TABLE_NAME, null, v);
            }
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

    }
}
