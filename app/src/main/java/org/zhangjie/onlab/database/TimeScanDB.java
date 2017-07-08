package org.zhangjie.onlab.database;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.zhangjie.onlab.record.TimeScanRecord;

public class TimeScanDB {

	private final String TAG = "Onlab.TimeScanDB";

	public static final String TS_DB_NAME = "time_scan.db";
	private static final String TS_TABLE_NAME = "";
	private SQLiteDatabase db;

	public TimeScanDB(Context context) {
		db = context.openOrCreateDatabase(TS_DB_NAME, Context.MODE_PRIVATE,
				null);
	}

	public void saveRecord(String recordName, TimeScanRecord record) {
		db.execSQL("CREATE table IF NOT EXISTS _"
				+ recordName
				+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT,iindex INTEGER,second INTEGER,abs FLOAT,trans FLOAT,energy INTEGER,date TEXT)");
		db.execSQL(
				"insert into _"
						+ recordName
						+ " (iindex,second,abs,trans,energy,date) values(?,?,?,?,?,?)",
				new Object[] { record.getIndex(), record.getSecond(),
						record.getAbs(), record.getTrans(), record.getEnergy(),
						record.getDate() });
	}

	public void saveRecord(String recordName, List<HashMap<String, String>> data) {
		db.beginTransaction();
		for (int i = 0; i < data.size(); i++) {
			int idx = 0;
			int second = 0;
			float abs = 0.0f;
			float trans = 0.0f;
			int energy = 0;
			long date = 0;

			HashMap<String, String> map = data.get(i);
			idx = Integer.parseInt(map.get("id"));
			second = Integer.parseInt(map.get("second"));
			abs = Float.parseFloat(map.get("abs"));
			trans = Float.parseFloat(map.get("trans"));
			energy = Integer.parseInt(map.get("energy"));
			date = Long.parseLong(map.get("date"));

			db.execSQL("CREATE table IF NOT EXISTS _"
					+ recordName
					+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT,iindex INTEGER,second INTEGER,abs FLOAT,trans FLOAT,energy INTEGER,date TEXT)");
			db.execSQL(
					"insert into _"
							+ recordName
							+ " (iindex,second,abs,trans,energy,date) values(?,?,?,?,?,?)",
					new Object[] { idx, second, abs, trans, energy, date});
		}
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	public List<TimeScanRecord> getRecords(String recordName) {
		List<TimeScanRecord> list = new LinkedList<TimeScanRecord>();

		Cursor c = db.rawQuery(
				"SELECT * from _" + recordName + " ORDER BY _id", null);
		while (c.moveToNext()) {
			int index = c.getInt(c.getColumnIndex("iindex"));
			int second = c.getInt(c.getColumnIndex("second"));
			float abs = c.getFloat(c.getColumnIndex("abs"));
			float trans = c.getFloat(c.getColumnIndex("trans"));
			int energy = c.getInt(c.getColumnIndex("energy"));
			long date = c.getLong(c.getColumnIndex("date"));
			TimeScanRecord record = new TimeScanRecord(index, second,
					abs, trans, energy, date);
			list.add(record);
		}
		c.close();
//		Collections.reverse(list);
		return list;
	}

	public void delItem(String recordName, long date) {
		db.delete("_" + recordName, "date=?", new String[] { "" + date });
	}

	public List<String> getTables() {
		List<String> tables = new LinkedList<String>();
		Cursor cursor = db
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
		db.execSQL("DROP TABLE IF EXISTS _" + recordName);
	}
}
