package org.zhangjie.onlab.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.zhangjie.onlab.record.DnaRecord;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2016/6/22.
 */
public class DnaDB {

    private final String TAG = "Onlab.Dna";
    public static final String DNA_DB_NAME = "dna.db";
    private SQLiteDatabase mDb;

    public DnaDB(Context context) {
        mDb = context.openOrCreateDatabase(DNA_DB_NAME,
                Context.MODE_PRIVATE, null);
    }

    public void saveRecord(String recordName, DnaRecord record) {
        mDb.execSQL("CREATE table IF NOT EXISTS _"
                + recordName
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT,iindex INTEGER,name TEXT,abs1 FLOAT,abs2 FLOAT,absRef FLOAT,dna FLOAT,protein FLOAT,ratio FLOAT,date TEXT)");
        mDb.execSQL(
                "insert into _"
                        + recordName
                        + " (iindex,name,abs1,abs2,absRef,dna,protein,ratio,date) values(?,?,?,?,?,?,?,?,?)",
                new Object[]{record.getIndex(), record.getName(),record.getAbs1(), record.getAbs2(),
                        record.getAbsRef(), record.getDna(), record.getProtein(), record.getRatio(),
                        record.getDate()});
    }

    public List<DnaRecord> getRecords(String recordName) {
        List<DnaRecord> list = new LinkedList<DnaRecord>();

        Cursor c = mDb.rawQuery(
                "SELECT * from _" + recordName + " ORDER BY _id", null);
        while (c.moveToNext()) {
            int index = c.getInt(c.getColumnIndex("iindex"));
            String name = c.getString(c.getColumnIndex("name"));
            float abs1 = c.getFloat(c.getColumnIndex("abs1"));
            float abs2 = c.getFloat(c.getColumnIndex("abs2"));
            float absRef = c.getFloat(c.getColumnIndex("absRef"));
            float dna = c.getFloat(c.getColumnIndex("dna"));
            float protein = c.getFloat(c.getColumnIndex("protein"));
            float ratio = c.getFloat(c.getColumnIndex("ratio"));
            long date = c.getLong(c.getColumnIndex("date"));
            DnaRecord record = new DnaRecord(index, name, abs1, abs2, absRef, dna, protein, ratio, date);
            list.add(record);
        }
        c.close();
//        Collections.reverse(list);
        return list;
    }

    public void delItem(String recordName, long date) {
        mDb.delete("_" + recordName, "date=?", new String[] { "" + date });
    }

    public List<String> getTables() {
        List<String> tables = new LinkedList<String>();
        Cursor cursor = mDb
                .rawQuery(
                        "select name from sqlite_master where type='table' order by name",
                        null);
        while (cursor.moveToNext()) {
            String name = cursor.getString(0);
            if (name.startsWith("_")) {
                Log.d(TAG, name);
                tables.add(name.substring(1));
            }
        }

        return tables;
    }

    public void delRecord(String recordName) {
        mDb.execSQL("DROP TABLE IF EXISTS _" + recordName);
    }
}
