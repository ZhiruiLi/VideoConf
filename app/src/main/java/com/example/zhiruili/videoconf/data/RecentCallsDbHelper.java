package com.example.zhiruili.videoconf.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.zhiruili.videoconf.data.RecentCallsListContract.*;

public class RecentCallsDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "recent_calls.db";

    private static final int DATABASE_VERSION = 1;

    public RecentCallsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String sqlCreateTable =
                "CREATE TABLE " + RecentCallsListEntry.TABLE_NAME + "(" +
                        RecentCallsListEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        RecentCallsListEntry.COLUMN_PERSON_ID  + " TEXT NOT NULL, " +
                        RecentCallsListEntry.COLUMN_IS_CALL_IN + " INTEGER NOT NULL, " +
                        RecentCallsListEntry.COLUMN_CALL_TIME + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ");";
        db.execSQL(sqlCreateTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + RecentCallsListEntry.TABLE_NAME);
        onCreate(db);
    }
}
