package org.zhangjie.onlab.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import org.zhangjie.onlab.DeviceApplication;
import org.zhangjie.onlab.MainActivity;
import org.zhangjie.onlab.R;

import java.util.regex.Pattern;

/**
 * Created by H151136 on 6/15/2016.
 */
public class Utils {

    public static final float DEFAULT_ABS_VALUE = 3.0f;
    public static final float DEFAULT_TRANS_VALUE = 100.0f;
    public static final int DEFAULT_ENERGY_VALUE = 65535;

    public static boolean needToSave = false;

    public static String formatAbs(float abs) {
        String result = "";
        int acc = DeviceApplication.getInstance().getSpUtils().getAcc();
        if (acc == MainActivity.ACC_HIGH) {
            result = String.format("%.4f", abs);
        } else if (acc == MainActivity.ACC_LOW) {
            result = String.format("%.3f", abs);
        }
        return result;
    }

    public static String formatTrans(float trans) {
        String result = "";
        int acc = DeviceApplication.getInstance().getSpUtils().getAcc();
        if (acc == MainActivity.ACC_HIGH) {
            result = String.format("%.2f", trans);
        } else if (acc == MainActivity.ACC_LOW) {
            result = String.format("%.1f", trans);
        }
        return result;
    }

    public static String formatConc(float conc) {
        String result = "";
        int acc = DeviceApplication.getInstance().getSpUtils().getAcc();
        if (acc == MainActivity.ACC_HIGH) {
            result = String.format("%.3f", conc);
        } else if (acc == MainActivity.ACC_LOW) {
            result = String.format("%.2f", conc);
        }
        return result;
    }

    public static float getValidAbs(float abs) {
        float valid = abs;
        if (valid < -4.0f) {
            valid = -4.0f;
        } else if (valid > 4.0f) {
            valid = 4.0f;
        }

        if (Float.isNaN(abs))
            valid = 0.0f;

        return valid;
    }

    public static float getValidTrans(float trans) {
        float valid = trans;
        if (valid > 3.0f) {
            valid = 3.0f;
        } else if (valid < -0.01f) {
            valid = -0.01f;
        }

        if (Float.isNaN(trans))
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

    public static void showMultipleSelectDialog(Context context, String title, final String[] items
            , DialogInterface.OnMultiChoiceClickListener listener,
                                                DialogInterface.OnClickListener deleteListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        int count = items.length;
        boolean[] select = new boolean[count];
        builder.setTitle(title);
        builder.setIcon(R.mipmap.ic_launcher);
//        builder.setItems(items, listener);
        builder.setMultiChoiceItems(items, select, listener);
//        builder.setPositiveButton(context.getString(R.string.ok_string), okListener);
        builder.setNeutralButton(context.getString(R.string.action_delete), deleteListener);
        builder.create().show();
    }

    public static void showAlertDialog(Context context, String title, String message, DialogInterface.OnClickListener listner) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.ok_string, listner);
        builder.create().show();
    }

    public static boolean checkWavelengthInvalid(Context context, float wavelength) {
        if (wavelength <= 190 || (wavelength >= 1100)) {
            Toast.makeText(context, context.getString(R.string.notice_wavelength_invalid), Toast.LENGTH_LONG).show();
            return false;
        } else {
            return true;
        }
    }

    public static boolean isValidName(String name) {
        String namePattern = "^[\\u4E00-\\u9FA5\\uF900-\\uFA2D\\w]{1,10}$";
        boolean result = Pattern.matches(namePattern, name);
        return result;
    }
}
