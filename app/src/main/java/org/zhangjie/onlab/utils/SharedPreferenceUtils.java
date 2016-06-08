package org.zhangjie.onlab.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by H151136 on 6/8/2016.
 */
public class SharedPreferenceUtils {
    //+++time scan
    public static final String KEY_TIMESCAN_WORK_WAVELENGTH = "key_timescan_work_wavelength";
    public static final String KEY_TIMESCAN_START_TIME = "key_timescan_start_time";
    public static final String KEY_TIMESCAN_END_TIME = "key_timescan_end_time";
    public static final String KEY_TIMESCAN_TIME_INTERVAL = "key_timescan_time_interval";
    //---

    private SharedPreferences mSp;
    private SharedPreferences.Editor mEditor;

    public SharedPreferenceUtils(Context context, String file) {
        mSp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        mEditor = mSp.edit();
    }

    public float getTimescanWorkWavelength() {
        return mSp.getFloat(KEY_TIMESCAN_WORK_WAVELENGTH, 800.0f);
    }

    public int getTimescanStartTime() {
        return mSp.getInt(KEY_TIMESCAN_START_TIME, 0);
    }

    public int getTimescanEndTime() {
        return mSp.getInt(KEY_TIMESCAN_END_TIME, 180);
    }

    public int getTimescanTimeInterval() {
        return mSp.getInt(KEY_TIMESCAN_TIME_INTERVAL, 1);
    }

    public void setKeyTimescanWorkWavelength(float wavelength) {
        mEditor.putFloat(KEY_TIMESCAN_WORK_WAVELENGTH, wavelength);
        mEditor.commit();
    }

    public void setKeyTimescanStartTime(int time) {
        mEditor.putInt(KEY_TIMESCAN_START_TIME, time);
        mEditor.commit();
    }

    public void setKeyTimescanEndTime(int time) {
        mEditor.putInt(KEY_TIMESCAN_END_TIME, time);
        mEditor.commit();
    }

    public void setKeyTimescanTimeInterval(int interval) {
        mEditor.putInt(KEY_TIMESCAN_TIME_INTERVAL, interval);
        mEditor.commit();
    }
}
