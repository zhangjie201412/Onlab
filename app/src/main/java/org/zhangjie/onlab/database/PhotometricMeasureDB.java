package org.zhangjie.onlab.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.zhangjie.onlab.record.PhotoMeasureRecord;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2016/6/22.
 */
public class PhotometricMeasureDB {
    private final String TAG = "Onlab.PhotometricMea";
    public static final String PHOTOMETRIC_MEASURE_DB_NAME = "photometric_measure.db";
    private static final String PHOTOMETRIC_MEASURE_TABLE_NAME = "";
    private SQLiteDatabase mDb;

    public PhotometricMeasureDB(Context context) {
        mDb = context.openOrCreateDatabase(PHOTOMETRIC_MEASURE_DB_NAME,
                Context.MODE_PRIVATE, null);
    }

    public void saveRecord(String recordName, PhotoMeasureRecord record) {
        mDb.execSQL("CREATE table IF NOT EXISTS _"
                + recordName
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT,iindex INTEGER,wavelength FLOAT,abs FLOAT,trans FLOAT,energy INTEGER,date TEXT)");
        mDb.execSQL(
                "insert into _"
                        + recordName
                        + " (iindex,wavelength,abs,trans,energy,date) values(?,?,?,?,?,?)",
                new Object[]{record.getIndex(), record.getWavelength(),
                        record.getAbs(), record.getTrans(), record.getEnergy(),
                        record.getDate()});
    }

    public void saveRecord(String recordName, List<HashMap<String, String>> data) {
        mDb.beginTransaction();
        for (int i = 0; i < data.size(); i++) {
            int index = 0;
            float wavelength = 0.0f;
            float abs = 0.0f;
            float trans = 0.0f;
            int energy = 0;
            long date = 0;

            HashMap<String, String> map = data.get(i);
            index = Integer.parseInt(map.get("id"));
            wavelength = Float.parseFloat(map.get("wavelength"));
            abs = Float.parseFloat(map.get("abs"));
            trans = Float.parseFloat(map.get("trans"));
            energy = Integer.parseInt(map.get("energy"));
            date = Long.parseLong(map.get("date"));

            mDb.execSQL("CREATE table IF NOT EXISTS _"
                    + recordName
                    + " (_id INTEGER PRIMARY KEY AUTOINCREMENT,iindex INTEGER,wavelength FLOAT,abs FLOAT,trans FLOAT,energy INTEGER,date TEXT)");
            mDb.execSQL(
                    "insert into _"
                            + recordName
                            + " (iindex,wavelength,abs,trans,energy,date) values(?,?,?,?,?,?)",
                    new Object[]{index, wavelength, abs, trans, energy, date});
        }

        mDb.setTransactionSuccessful();
        mDb.endTransaction();
    }

    public List<PhotoMeasureRecord> getRecords(String recordName) {
        List<PhotoMeasureRecord> list = new LinkedList<PhotoMeasureRecord>();

        Cursor c = mDb.rawQuery(
                "SELECT * from _" + recordName + " ORDER BY _id", null);
        while (c.moveToNext()) {
            int index = c.getInt(c.getColumnIndex("iindex"));
            float wavelength = c.getFloat(c.getColumnIndex("wavelength"));
            float abs = c.getFloat(c.getColumnIndex("abs"));
            float trans = c.getFloat(c.getColumnIndex("trans"));
            int energy = c.getInt(c.getColumnIndex("energy"));
            long date = c.getLong(c.getColumnIndex("date"));
            Log.d(TAG, "E = " + energy);
            PhotoMeasureRecord record = new PhotoMeasureRecord(index,
                    wavelength, abs, trans, energy, date);
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
