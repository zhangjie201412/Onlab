package org.zhangjie.onlab;

import android.app.Application;

import org.zhangjie.onlab.utils.SharedPreferenceUtils;

/**
 * Created by H151136 on 6/8/2016.
 */
public class DeviceApplication extends Application {
    public static final String SP_FILE_NAME = "onlab_setting_sp";
    private static DeviceApplication mApplication;

    private SharedPreferenceUtils mSpUtils;

    public synchronized static DeviceApplication getInstance() {
        return mApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;

        mSpUtils = new SharedPreferenceUtils(this, SP_FILE_NAME);
    }

    public synchronized SharedPreferenceUtils getSpUtils() {
        if(mSpUtils == null)
            mSpUtils = new SharedPreferenceUtils(this, SP_FILE_NAME);

        return mSpUtils;
    }
}
