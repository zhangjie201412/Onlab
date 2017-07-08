package org.zhangjie.onlab.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.zhangjie.onlab.record.BaselineRecord;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2016/6/22.
 */
public class BaselineDB {

    private final String TAG = "Onlab.Baseline";
    public static final String PHOTOMETRIC_MEASURE_DB_NAME = "baseline.db";
    private SQLiteDatabase mDb;

    public BaselineDB(Context context) {
        mDb = context.openOrCreateDatabase(PHOTOMETRIC_MEASURE_DB_NAME,
                Context.MODE_PRIVATE, null);
    }

    public void saveRecord(String recordName, BaselineRecord record) {
        mDb.execSQL("CREATE table IF NOT EXISTS _"
                + recordName
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT,iindex INTEGER,wavelength FLOAT,gain INTEGER,energy INTEGER,date TEXT)");
        mDb.execSQL(
                "insert into _"
                        + recordName
                        + " (iindex,wavelength,gain,energy,date) values(?,?,?,?,?)",
                new Object[]{record.getIndex(), record.getWavelength(),
                        record.getGain(), record.getEnergy(),
                        record.getDate()});
    }

    public void saveRecord(String recordName, List<HashMap<String, String>> data) {
        mDb.beginTransaction();
        for(int i = 0; i < data.size(); i++) {
            int index = 0;
            float wavelength = 0.0f;
            int gain = 0;
            int energy = 0;
            long date = 0;

            HashMap<String, String> map = data.get(i);
            index = Integer.parseInt(map.get("id"));
            wavelength = Float.parseFloat(map.get("wavelength"));
            gain = Integer.parseInt(map.get("gain"));
            energy = Integer.parseInt(map.get("energy"));
            mDb.execSQL("CREATE table IF NOT EXISTS _"
                    + recordName
                    + " (_id INTEGER PRIMARY KEY AUTOINCREMENT,iindex INTEGER,wavelength FLOAT,gain INTEGER,energy INTEGER,date TEXT)");
            mDb.execSQL(
                    "insert into _"
                            + recordName
                            + " (iindex,wavelength,gain,energy,date) values(?,?,?,?,?)",
                    new Object[]{index, wavelength, gain, energy, date});
        }
        mDb.setTransactionSuccessful();
        mDb.endTransaction();
    }

    public List<BaselineRecord> getRecords(String recordName) {
        List<BaselineRecord> list = new LinkedList<BaselineRecord>();

        Cursor c = mDb.rawQuery(
                "SELECT * from _" + recordName + " ORDER BY _id", null);
        while (c.moveToNext()) {
            int index = c.getInt(c.getColumnIndex("iindex"));
            float wavelength = c.getFloat(c.getColumnIndex("wavelength"));
            int gain = c.getInt(c.getColumnIndex("gain"));
            int energy = c.getInt(c.getColumnIndex("energy"));
            long date = c.getLong(c.getColumnIndex("date"));
            BaselineRecord record = new BaselineRecord(index,
                    wavelength, gain, energy, date);
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
