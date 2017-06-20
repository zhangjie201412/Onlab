package org.zhangjie.onlab.database;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.zhangjie.onlab.record.QuantitativeAnalysisRecord;

public class QuantitativeAnalysisDB {
	private final String TAG = "Onlab.QuantitativeAnaly";

	public static final String QA_DB_NAME = "quantitative_analysis.db";
	private static final String QA_TABLE_NAME = "";
	private SQLiteDatabase db;

	public QuantitativeAnalysisDB(Context context) {
		db = context.openOrCreateDatabase(QA_DB_NAME, Context.MODE_PRIVATE,
				null);
	}

	public void saveRecord(String recordName, QuantitativeAnalysisRecord record) {
		db.execSQL("CREATE table IF NOT EXISTS _"
				+ recordName
				+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT,iindex INTEGER,name TEXT,abs FLOAT,conc FLOAT,date TEXT)");
		db.execSQL(
				"insert into _" + recordName
						+ " (iindex,name,abs,conc,date) values(?,?,?,?,?)",
				new Object[] { record.getIndex(), record.getName(),
						record.getAbs(), record.getConc(), record.getDate() });
	}

	public List<QuantitativeAnalysisRecord> getRecords(String recordName) {
		List<QuantitativeAnalysisRecord> list = new LinkedList<QuantitativeAnalysisRecord>();

		Cursor c = db.rawQuery(
				"SELECT * from _" + recordName + " ORDER BY _id", null);
		while (c.moveToNext()) {
			int index = c.getInt(c.getColumnIndex("iindex"));
			String name = c.getString(c.getColumnIndex("name"));
			float abs = c.getFloat(c.getColumnIndex("abs"));
			float conc = c.getFloat(c.getColumnIndex("conc"));
			long date = c.getLong(c.getColumnIndex("date"));
			QuantitativeAnalysisRecord record = new QuantitativeAnalysisRecord(
					index, name, abs, conc, date);
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
