package org.zhangjie.onlab.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.zhangjie.onlab.MainActivity;
import org.zhangjie.onlab.setting.QuantitativeAnalysisSettingActivity;
import org.zhangjie.onlab.setting.TimescanSettingActivity;
import org.zhangjie.onlab.setting.WavelengthSettingActivity;

import java.util.Arrays;

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
    public static final String KEY_QA_START_CONC = "key_qa_start_conc";
    public static final String KEY_QA_END_CONC = "key_qa_end_conc";
    public static final String KEY_QA_LIMIT_UP = "key_qa_limit_up";
    public static final String KEY_QA_LIMIT_DOWN = "key_qa_limit_down";
    //---
    //+++time scan
    public static final String KEY_TIMESCAN_WORK_WAVELENGTH = "key_timescan_work_wavelength";
    public static final String KEY_TIMESCAN_START_TIME = "key_timescan_start_time";
    public static final String KEY_TIMESCAN_END_TIME = "key_timescan_end_time";
    public static final String KEY_TIMESCAN_TIME_INTERVAL = "key_timescan_time_interval";
    public static final String KEY_TIMESCAN_TIME_DELAY = "key_timescan_time_delay";
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
    //+++multiple wavelength
    public static final String KEY_MULTIPLE_WAVELENGTH = "key_multiple_wavelength_settings";
    public static final String KEY_MULTIPLE_WAVELENGTH_LENGTH = "key_multiple_wavelength_length";
    //---

    public static final String KEY_ACC = "key_acc";
    public static final String KEY_BASELINE_AVAILABLE = "key_baseline_available";
    public static final String KEY_PEAK_DISTANCE = "key_peak_distance";
    public static final String KEY_LAMP_WAVELENGTH = "key_lamp_wavelength";
    public static final String KEY_D2_STATUS = "key_d2_status";
    public static final String KEY_WU_STATUS = "key_wu_status";

    public static final String KEY_MAC_ADDRESS = "key_mac_address";

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
        return mSp.getInt(KEY_QA_CONC_UNIT, QuantitativeAnalysisSettingActivity.CONCENTRATION_UNIT_UG_ML);
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
        return mSp.getFloat(KEY_QA_RATIO1, 1.0f);
    }
    public float getQARatio2() {
        return mSp.getFloat(KEY_QA_RATIO2, 1.0f);
    }
    public float getQARatio3() {
        return mSp.getFloat(KEY_QA_RATIO3, 1.0f);
    }
    public float getQAStartConc() {
        return mSp.getFloat(KEY_QA_START_CONC, 0);
    }
    public float getQAEndConc() {
        return mSp.getFloat(KEY_QA_END_CONC, 10.0f);
    }
    public float getQALimitUp() {
        return mSp.getFloat(KEY_QA_LIMIT_UP, 4.0f);
    }
    public float getQALimitDown() {
        return mSp.getFloat(KEY_QA_LIMIT_DOWN, 0.0f);
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

    public int getTimescanTimeDelay() {
        return mSp.getInt(KEY_TIMESCAN_TIME_DELAY, 0);
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

    public int getAcc() {
        return mSp.getInt(KEY_ACC, MainActivity.ACC_LOW);
    }

    public float getPeakDistance() {
        return mSp.getFloat(KEY_PEAK_DISTANCE, 1.0f);
    }

    public float getLampWavelength() {
        return mSp.getFloat(KEY_LAMP_WAVELENGTH, 340.0f);
    }

    public int getMultipleWavelengthLength() {
        return mSp.getInt(KEY_MULTIPLE_WAVELENGTH_LENGTH, 0);
    }

    public boolean getBaselineAvailable() {
        return mSp.getBoolean(KEY_BASELINE_AVAILABLE, false);
    }

    public boolean getD2Status() {
        return mSp.getBoolean(KEY_D2_STATUS, false);
    }

    public boolean getWuStatus() {
        return mSp.getBoolean(KEY_WU_STATUS, false);
    }

    public String getMacAddress() {
        return mSp.getString(KEY_MAC_ADDRESS, "");
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
    public void setKeyQaStartConc(float conc) {
        mEditor.putFloat(KEY_QA_START_CONC, conc);
        mEditor.commit();
    }
    public void setKeyQaEndConc(float conc) {
        mEditor.putFloat(KEY_QA_END_CONC, conc);
        mEditor.commit();
    }
    public void setKeyQaLimitUp(float up) {
        mEditor.putFloat(KEY_QA_LIMIT_UP, up);
        mEditor.commit();
    }
    public void setKeyQaLimitDown(float down) {
        mEditor.putFloat(KEY_QA_LIMIT_DOWN, down);
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

    public void setKeyTimescanTimeDelay(int interval) {
        mEditor.putInt(KEY_TIMESCAN_TIME_DELAY, interval);
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

    public void setKeyAcc(int acc) {
        mEditor.putInt(KEY_ACC, acc);
        mEditor.commit();
    }

    public void setPeakDistance(float distance) {
        mEditor.putFloat(KEY_PEAK_DISTANCE, distance);
        mEditor.commit();
    }

    public void setLampWavelength(float wavelength) {
        mEditor.putFloat(KEY_LAMP_WAVELENGTH, wavelength);
        mEditor.commit();
    }

    public void setKeyMultipleWavelengthLength(int length) {
        mEditor.putInt(KEY_MULTIPLE_WAVELENGTH_LENGTH, length);
        mEditor.commit();
    }

    public void setKeyBaselineAvailable(boolean available) {
        mEditor.putBoolean(KEY_BASELINE_AVAILABLE, available);
        mEditor.commit();
    }

    public void setKeyD2Status(boolean on) {
        mEditor.putBoolean(KEY_D2_STATUS, on);
        mEditor.commit();
    }

    public void setKeyWuStatus(boolean on) {
        mEditor.putBoolean(KEY_WU_STATUS, on);
        mEditor.commit();
    }

    public void setKeyMacAddress(String macAddress) {
        mEditor.putString(KEY_MAC_ADDRESS, macAddress);
        mEditor.commit();
    }

    public void saveBaseline(int[] base) {
        JSONArray jsonArray = new JSONArray();
        for(int i : base) {
            jsonArray.put(i);
        }
        mEditor.putString("key_baseline", jsonArray.toString());
        mEditor.commit();
    }

    public int[] getBaseline(int length) {
        int[] base = new int[length];
        Arrays.fill(base, 8);
        try {
            JSONArray jsonArray = new JSONArray(mSp.getString("key_baseline", "[]"));
            for(int i= 0; i < jsonArray.length(); i++) {
                base[i] = jsonArray.getInt(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return base;
    }

    public void saveBaselineRef(int[] base) {
        JSONArray jsonArray = new JSONArray();
        for(int i : base) {
            jsonArray.put(i);
        }
        mEditor.putString("key_baseline_ref", jsonArray.toString());
        mEditor.commit();
    }

    public int[] getBaselineRef(int length) {
        int[] base = new int[length];
        Arrays.fill(base, 8);
        try {
            JSONArray jsonArray = new JSONArray(mSp.getString("key_baseline_ref", "[]"));
            for(int i= 0; i < jsonArray.length(); i++) {
                base[i] = jsonArray.getInt(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return base;
    }

    public void saveMultipleWavelength(float[] wavelengths) {
        JSONArray jsonArray = new JSONArray();
        for(float i : wavelengths) {
            double val = i;
            try {
                jsonArray.put(val);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        mEditor.putString(KEY_MULTIPLE_WAVELENGTH, jsonArray.toString());
        mEditor.commit();
    }

    public float[] getMultipleWavelength() {
        int length = getMultipleWavelengthLength();
        if(length == 0)
            return null;
        float[] wavelengths = new float[length];
        Arrays.fill(wavelengths, 0);
        try {
            JSONArray jsonArray = new JSONArray(mSp.getString(KEY_MULTIPLE_WAVELENGTH, "[]"));
            for(int i= 0; i < jsonArray.length(); i++) {
                wavelengths[i] = (float)jsonArray.getDouble(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return wavelengths;
    }
}
