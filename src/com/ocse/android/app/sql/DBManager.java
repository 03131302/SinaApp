package com.ocse.android.app.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class DBManager {
    private DBHelper helper;
    private SQLiteDatabase db;

    public DBManager(Context context) {
        helper = new DBHelper(context);
        db = helper.getWritableDatabase();
    }

    public void add(List<Douban> doubans) {
        db.beginTransaction();
        try {
            for (Douban douban : doubans) {
                db.execSQL("INSERT INTO douban VALUES(null, ?, ?, ?)", new Object[]{douban.getTag(), douban.getStart(), douban.getEnd()});
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void updateAge(Douban douban) {
        ContentValues cv = new ContentValues();
        cv.put("tag", douban.getTag());
        cv.put("start", douban.getStart());
        cv.put("end", douban.getEnd());
        db.update("douban", cv, "", null);
    }

    public List<Douban> query() {
        List<Douban> doubans = new ArrayList<Douban>();
        Cursor c = queryTheCursor();
        while (c.moveToNext()) {
            Douban douban = new Douban();
            douban.set_id(c.getInt(c.getColumnIndex("_id")));
            douban.setTag(c.getString(c.getColumnIndex("tag")));
            douban.setStart(c.getInt(c.getColumnIndex("start")));
            douban.setEnd(c.getInt(c.getColumnIndex("end")));
            doubans.add(douban);
        }
        c.close();
        return doubans;
    }

    public Cursor queryTheCursor() {
        Cursor c = db.rawQuery("SELECT * FROM douban", null);
        return c;
    }

    public void closeDB() {
        db.close();
    }
}
