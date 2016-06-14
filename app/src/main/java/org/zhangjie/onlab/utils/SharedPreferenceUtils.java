package org.zhangjie.onlab.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.zhangjie.onlab.setting.QuantitativeAnalysisSettingActivity;
import org.zhangjie.onlab.setting.TimescanSettingActivity;
import org.zhangjie.onlab.setting.WavelengthSettingActivity;

/**
 * Created by H151136 on 6/8/2016.
 */
public class SharedPreferenceUtils {
    //+++qa
    public static final String KEY_QA_FITTING_METHOD = "key_qa_fitting_method";
    public static final String KEY_QA_CONC_UNIT = "key_qa_conc_unit";
    public static final String KEY_QA_CALC_TYPE = "key_qa_calc_type";
    public static final String KEY_QA_K0 = "key_qa_k0";
    public static final String KEY_QA_K1 = "key_qa_k1";
    public static final String KEY_QA_WAVELENGTH_SETTING = "key_qa_wavelength_setting";
    public static final String KEY_QA_WAVELENGTH1 = "key_qa_wavelength1";
    public static final String KEY_QA_WAVELENGTH2 = "key_qa_wavelength2";
    public static final String KEY_QA_WAVELENGTH3 = "key_qa_wavelength3";
    public static final String KEY_QA_RATIO1 = "key_qa_ratio1";
    public static final String KEY_QA_RATIO2 = "key_qa_ratio2";
    public static final String KEY_QA_RATIO3 = "key_qa_ratio3";
    //---
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

    public int getQAFittingMethod() {
        return mSp.getInt(KEY_QA_FITTING_METHOD, QuantitativeAnalysisSettingActivity.FITTING_METHOD_ONE);
    }

    public int getQAConcUnit() {
        return mSp.getInt(KEY_QA_CONC_UNIT, QuantitativeAnalysisSettingActivity.CONCENTRATION_UNIT_MG_ML);
    }

    public int getQACalcType() {
        return mSp.getInt(KEY_QA_CALC_TYPE, QuantitativeAnalysisSettingActivity.CALC_TYPE_SAMPLE);
    }

    public float getQAK0() {
        return mSp.getFloat(KEY_QA_K0, 1.0f);
    }

    public float getQAK1() {
        return mSp.getFloat(KEY_QA_K1, 1.0f);
    }

    public int getQAWavelengthSetting() {
        return mSp.getInt(KEY_QA_WAVELENGTH_SETTING, QuantitativeAnalysisSettingActivity.WAVELENGTH_ONE);
    }

    public float getQAWavelength1() {
        return mSp.getFloat(KEY_QA_WAVELENGTH1, 645.0f);
    }
    public float getQAWavelength2() {
        return mSp.getFloat(KEY_QA_WAVELENGTH2, 645.0f);
    }
    public float getQAWavelength3() {
        return mSp.getFloat(KEY_QA_WAVELENGTH3, 645.0f);
    }
    public float getQARatio1() {
        return mSp.getFloat(KEY_QA_RATIO1, 0.0f);
    }
    public float getQARatio2() {
        return mSp.getFloat(KEY_QA_RATIO2, 0.0f);
    }
    public float getQARatio3() {
        return mSp.getFloat(KEY_QA_RATIO3, 0.0f);
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

    public void setKeyQaFittingMethod(int method) {
        mEditor.putInt(KEY_QA_FITTING_METHOD, method);
        mEditor.commit();
    }

    public void setKeyQaCalcType(int type) {
        mEditor.putInt(KEY_QA_CALC_TYPE, type);
        mEditor.commit();
    }

    public void setKeyQaConcUnit(int unit) {
        mEditor.putInt(KEY_QA_CONC_UNIT, unit);
        mEditor.commit();
    }

    public void setKeyQaK0(float k) {
        mEditor.putFloat(KEY_QA_K0, k);
        mEditor.commit();
    }
    public void setKeyQaK1(float k) {
        mEditor.putFloat(KEY_QA_K1, k);
        mEditor.commit();
    }

    public void setKeyQaWavelengthSetting(int setting) {
        mEditor.putInt(KEY_QA_WAVELENGTH_SETTING, setting);
        mEditor.commit();
    }

    public void setKeyQaWavelength1(float wavelength) {
        mEditor.putFloat(KEY_QA_WAVELENGTH1, wavelength);
        mEditor.commit();
    }
    public void setKeyQaWavelength2(float wavelength) {
        mEditor.putFloat(KEY_QA_WAVELENGTH2, wavelength);
        mEditor.commit();
    }
    public void setKeyQaWavelength3(float wavelength) {
        mEditor.putFloat(KEY_QA_WAVELENGTH3, wavelength);
        mEditor.commit();
    }

    public void setKeyQaRatio1(float ratio) {
        mEditor.putFloat(KEY_QA_RATIO1, ratio);
        mEditor.commit();
    }
    public void setKeyQaRatio2(float ratio) {
        mEditor.putFloat(KEY_QA_RATIO2, ratio);
        mEditor.commit();
    }
    public void setKeyQaRatio3(float ratio) {
        mEditor.putFloat(KEY_QA_RATIO3, ratio);
        mEditor.commit();
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
