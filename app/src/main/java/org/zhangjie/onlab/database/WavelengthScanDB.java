package org.zhangjie.onlab.database;

import java.util.Collections;
import java.util.HashMap;
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
				+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT,iindex INTEGER,wavelength FLOAT,abs FLOAT,trans FLOAT,energy INTEGER,energyRef INTEGER,date TEXT)");
		db.execSQL("insert into _" + recordName
						+ " (iindex,wavelength,abs,trans,energy,energyRef,date) values(?,?,?,?,?,?,?)",
				new Object[] { record.getIndex(), record.getWavelength(),
						record.getAbs(), record.getTrans(), record.getEnergy(),
						record.getEnergyRef(), record.getDate() });
	}

	public void saveRecord(String recordName, List<HashMap<String, String>> data) {
		db.beginTransaction();
		for (int i = 0; i < data.size(); i++) {
			int index = 0;
			float wavelength = 0;
			float abs = 0.0f;
			float trans = 0.0f;
			int energy = 0;
			int energyRef = 0;
			long date = 0;

			HashMap<String, String> map = data.get(i);
			index = Integer.parseInt(map.get("id"));
			wavelength = Float.parseFloat(map.get("wavelength"));
			abs = Float.parseFloat(map.get("abs"));
			trans = Float.parseFloat(map.get("trans"));
			energy = Integer.parseInt(map.get("energy"));
			energyRef = Integer.parseInt(map.get("energyRef"));
			date = Long.parseLong(map.get("date"));

			db.execSQL("CREATE table IF NOT EXISTS _"
					+ recordName
					+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT,iindex INTEGER,wavelength FLOAT,abs FLOAT,trans FLOAT,energy INTEGER,energyRef INTEGER,date TEXT)");
			db.execSQL("insert into _" + recordName
							+ " (iindex,wavelength,abs,trans,energy,energyRef,date) values(?,?,?,?,?,?,?)",
					new Object[] { index, wavelength, abs, trans, energy, energyRef, date });
		}
		db.setTransactionSuccessful();
		db.endTransaction();
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
			int energyRef = c.getInt(c.getColumnIndex("energyRef"));
			long date = c.getLong(c.getColumnIndex("date"));
			WavelengthScanRecord record = new WavelengthScanRecord(
					index, wavelength, abs, trans, energy, energyRef, date);
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
