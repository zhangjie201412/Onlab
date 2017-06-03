package org.zhangjie.onlab.database;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.zhangjie.onlab.record.WavelengthScanRecord;

public class WavelengthScanDB {
	private final String TAG = "Onlab.WavelengthScan";

	public static final String WS_DB_NAME = "wavelength_scanning.db";
	private static final String WS_TABLE_NAME = "";
	private SQLiteDatabase db;

	public WavelengthScanDB(Context context) {
		db = context.openOrCreateDatabase(WS_DB_NAME, Context.MODE_PRIVATE,
				null);
	}

	public void saveRecord(String recordName, WavelengthScanRecord record) {
		db.execSQL("CREATE table IF NOT EXISTS _"
				+ recordName
				+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT,iindex INTEGER,wavelength FLOAT,abs FLOAT,trans FLOAT,energy INTEGER,date TEXT)");
		db.execSQL("insert into _" + recordName
						+ " (iindex,wavelength,abs,trans,energy,date) values(?,?,?,?,?,?)",
				new Object[] { record.getIndex(), record.getWavelength(),
						record.getAbs(), record.getTrans(), record.getEnergy(),
						record.getDate() });
	}

	public List<WavelengthScanRecord> getRecords(String recordName) {
		List<WavelengthScanRecord> list = new LinkedList<WavelengthScanRecord>();

		Cursor c = db.rawQuery(
				"SELECT * from _" + recordName + " ORDER BY _id", null);
		while (c.moveToNext()) {
			int index = c.getInt(c.getColumnIndex("iindex"));
			float wavelength = c.getFloat(c.getColumnIndex("wavelength"));
			float abs = c.getFloat(c.getColumnIndex("abs"));
			float trans = c.getFloat(c.getColumnIndex("trans"));
			int energy = c.getInt(c.getColumnIndex("energy"));
			long date = c.getLong(c.getColumnIndex("date"));
			WavelengthScanRecord record = new WavelengthScanRecord(
					index, wavelength, abs, trans, energy, date);
			list.add(record);
		}
		c.close();
		Collections.reverse(list);
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
