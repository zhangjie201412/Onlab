package org.zhangjie.onlab.utils;

import org.zhangjie.onlab.DeviceApplication;
import org.zhangjie.onlab.MainActivity;

/**
 * Created by H151136 on 6/15/2016.
 */
public class Utils {
    public static String formatAbs(float abs) {
        String result = "";
        int acc = DeviceApplication.getInstance().getSpUtils().getAcc();
        if(acc == MainActivity.ACC_HIGH) {
            result = String.format("%.4f", abs);
        } else if(acc == MainActivity.ACC_LOW) {
            result = String.format("%.3f", abs);
        }
        return result;
    }

    public static String formatTrans(float trans) {
        String result = "";
        int acc = DeviceApplication.getInstance().getSpUtils().getAcc();
        if(acc == MainActivity.ACC_HIGH) {
            result = String.format("%.4f", trans);
        } else if(acc == MainActivity.ACC_LOW) {
            result = String.format("%.3f", trans);
        }
        return result;
    }
}