package org.zhangjie.onlab.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.zhangjie.onlab.record.MultipleWavelengthRecord;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2016/6/22.
 */
public class MultipleWavelengthDB {

    private final String TAG = "Onlab.MultipleWav";
    public static final String MULTIPLE_WAVELENGTH_DB_NAME = "multiple_wavelength.db";
    private SQLiteDatabase mDb;

    public MultipleWavelengthDB(Context context) {
        mDb = context.openOrCreateDatabase(MULTIPLE_WAVELENGTH_DB_NAME,
                Context.MODE_PRIVATE, null);
    }

    public void saveRecord(String recordName, MultipleWavelengthRecord record) {
        mDb.execSQL("CREATE table IF NOT EXISTS _"
                + recordName
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT,iindex INTEGER,subid INTEGER,wavelength FLOAT,abs FLOAT,trans FLOAT,energy INTEGER,date TEXT)");
        mDb.execSQL(
                "insert into _"
                        + recordName
                        + " (iindex,subid,wavelength,abs,trans,energy,date) values(?,?,?,?,?,?,?)",
                new Object[]{record.getIndex(), record.getSubIndex(), record.getWavelength(),
                        record.getAbs(), record.getTrans(), record.getEnergy(),
                        record.getDate()});
    }

    public List<MultipleWavelengthRecord> getRecords(String recordName) {
        List<MultipleWavelengthRecord> list = new LinkedList<MultipleWavelengthRecord>();

        Cursor c = mDb.rawQuery(
                "SELECT * from _" + recordName + " ORDER BY _id", null);
        while (c.moveToNext()) {
            int index = c.getInt(c.getColumnIndex("iindex"));
            int subIndex = c.getInt(c.getColumnIndex("subid"));
            float wavelength = c.getFloat(c.getColumnIndex("wavelength"));
            float abs = c.getFloat(c.getColumnIndex("abs"));
            float trans = c.getFloat(c.getColumnIndex("trans"));
            int energy = c.getInt(c.getColumnIndex("energy"));
            long date = c.getLong(c.getColumnIndex("date"));
            MultipleWavelengthRecord record = new MultipleWavelengthRecord(index,
                    subIndex, wavelength, abs, trans, energy, date);
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
