package org.zhangjie.onlab.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.zhangjie.onlab.setting.TimescanSettingActivity;
import org.zhangjie.onlab.setting.WavelengthSettingActivity;

/**
 * Created by H151136 on 6/8/2016.
 */
public class SharedPreferenceUtils {
    //+++time scan
    public static final String KEY_TIMESCAN_WORK_WAVELENGTH = "key_timescan_work_wavelength";
    public static final String KEY_TIMESCAN_START_TIME = "key_timescan_start_time";
    public static final String KEY_TIMESCAN_END_TIME = "key_timescan_end_time";
    public static final String KEY_TIMESCAN_TIME_INTERVAL = "key_timescan_time_interval";
    public static final String KEY_TIMESCAN_TEST_MODE = "key_timescan_test_mode";
    public static final String KEY_TIMESCAN_LIMIT_UP = "key_timescan_limit_up";
    public static final String KEY_TIMESCAN_LIMIT_DOWN = "key_timescan_limit_down";
    //---
    //+++wavelength scan
    public static final String KEY_WAVELENGTHSCAN_TEST_MODE = "key_wavelengthscan_test_mode";
    public static final String KEY_WAVELENGTHSCAN_LIMIT_UP = "key_wavelengthscan_limit_up";
    public static final String KEY_WAVELENGTHSCAN_LIMIT_DOWN = "key_wavelengthscan_limit_down";
    public static final String KEY_WAVELENGTHSCAN_START = "key_wavelengthscan_start";
    public static final String KEY_WAVELENGTHSCAN_END = "key_wavelengthscan_end";
    public static final String KEY_WAVELENGTHSCAN_SPEED = "key_wavelengthscan_speed";
    public static final String KEY_WAVELENGTHSCAN_INTERVAL = "key_wavelengthscan_interval";
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

    public int getTimescanTestMode() {
        return mSp.getInt(KEY_TIMESCAN_TEST_MODE, TimescanSettingActivity.TEST_MODE_ABS);
    }

    public float getTimescanLimitUp() {
        if(getTimescanTestMode() == TimescanSettingActivity.TEST_MODE_ABS) {
            return mSp.getFloat(KEY_TIMESCAN_LIMIT_UP, 3.0f);
        } else {
            return mSp.getFloat(KEY_TIMESCAN_LIMIT_UP, 100.0f);
        }
    }

    public float getTimescanLimitDown() {
        return mSp.getFloat(KEY_TIMESCAN_LIMIT_DOWN, 0.0f);
    }

    public int getWavelengthscanTestMode() {
        return mSp.getInt(KEY_WAVELENGTHSCAN_TEST_MODE, WavelengthSettingActivity.TEST_MODE_ABS);
    }

    public float getWavelengthscanLimitUp() {
        if(getWavelengthscanTestMode() == WavelengthSettingActivity.TEST_MODE_ABS) {
            return mSp.getFloat(KEY_WAVELENGTHSCAN_LIMIT_UP, 3.0f);
        } else if(getWavelengthscanTestMode() == WavelengthSettingActivity.TEST_MODE_TRANS) {
            return mSp.getFloat(KEY_WAVELENGTHSCAN_LIMIT_UP, 100.0f);
        } else {
            return mSp.getFloat(KEY_WAVELENGTHSCAN_LIMIT_UP, 65535);
        }
    }

    public float getWavelengthscanLimitDown() {
        return mSp.getFloat(KEY_WAVELENGTHSCAN_LIMIT_DOWN, 0.0f);
    }

    public float getWavelengthscanStart() {
        return mSp.getFloat(KEY_WAVELENGTHSCAN_START, 400.0f);
    }

    public float getWavelengthscanEnd() {
        return mSp.getFloat(KEY_WAVELENGTHSCAN_END, 650.0f);
    }

    public int getWavelengthscanSpeed() {
        return mSp.getInt(KEY_WAVELENGTHSCAN_SPEED, WavelengthSettingActivity.SPEED_STANDARD);
    }

    public float getWavelengthscanInterval() {
        return mSp.getFloat(KEY_WAVELENGTHSCAN_INTERVAL, 1.0f);
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

    public void setKeyTimescanTestMode(int mode) {
        mEditor.putInt(KEY_TIMESCAN_TEST_MODE, mode);
        mEditor.commit();
    }

    public void setKeyTimescanLimitUp(float up) {
        mEditor.putFloat(KEY_TIMESCAN_LIMIT_UP, up);
        mEditor.commit();
    }

    public void setKeyTimescanLimitDown(float down) {
        mEditor.putFloat(KEY_TIMESCAN_LIMIT_DOWN, down);
        mEditor.commit();
    }

    public void setKeyWavelengthscanTestMode(int mode) {
        mEditor.putInt(KEY_WAVELENGTHSCAN_TEST_MODE, mode);
        mEditor.commit();
    }

    public void setKeyWavelengthscanLimitUp(float up) {
        mEditor.putFloat(KEY_WAVELENGTHSCAN_LIMIT_UP, up);
        mEditor.commit();
    }

    public void setKeyWavelengthscanLimitDown(float down) {
        mEditor.putFloat(KEY_WAVELENGTHSCAN_LIMIT_DOWN, down);
        mEditor.commit();
    }

    public void setKeyWavelengthscanStart(float start) {
        mEditor.putFloat(KEY_WAVELENGTHSCAN_START, start);
        mEditor.commit();
    }

    public void setKeyWavelengthscanEnd(float end) {
        mEditor.putFloat(KEY_WAVELENGTHSCAN_END, end);
        mEditor.commit();
    }

    public void setKeyWavelengthscanSpeed(int speed) {
        mEditor.putInt(KEY_WAVELENGTHSCAN_SPEED, speed);
        mEditor.commit();
    }

    public void setKeyWavelengthscanInterval(float interval) {
        mEditor.putFloat(KEY_WAVELENGTHSCAN_INTERVAL, interval);
        mEditor.commit();
    }
}
