package org.zhangjie.onlab.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import org.zhangjie.onlab.DeviceApplication;
import org.zhangjie.onlab.MainActivity;
import org.zhangjie.onlab.R;

/**
 * Created by H151136 on 6/15/2016.
 */
public class Utils {

    public static final float DEFAULT_ABS_VALUE = 3.0f;
    public static final float DEFAULT_TRANS_VALUE = 100.0f;
    public static final int DEFAULT_ENERGY_VALUE = 65535;

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
            result = String.format("%.2f", trans);
        } else if(acc == MainActivity.ACC_LOW) {
            result = String.format("%.1f", trans);
        }
        return result;
    }

    public static String formatConc(float conc) {
        String result = "";
        int acc = DeviceApplication.getInstance().getSpUtils().getAcc();
        if(acc == MainActivity.ACC_HIGH) {
            result = String.format("%.3f", conc);
        } else if(acc == MainActivity.ACC_LOW) {
            result = String.format("%.2f", conc);
        }
        return result;
    }

    public static float getValidAbs(float abs) {
        float valid = abs;
        if(valid < -4.0f) {
            valid = -4.0f;
        } else if(valid > 4.0f) {
            valid = 4.0f;
        }

        if(Float.isNaN(abs))
            valid = 0.0f;

        return valid;
    }

    public static float getValidTrans(float trans) {
        float valid = trans;
        if(valid > 3.0f) {
            valid = 3.0f;
        } else if(valid < -0.01f) {
            valid = -0.01f;
        }

        if(Float.isNaN(trans))
            valid = 0.0f;

        return valid;
    }

    public static void showItemSelectDialog(Context context, String title, final String[] items, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setItems(items, listener);
        builder.create().show();
    }
}
