package org.zhangjie.onlab.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import org.zhangjie.onlab.DeviceApplication;
import org.zhangjie.onlab.MainActivity;
import org.zhangjie.onlab.R;

import java.io.File;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

import lecho.lib.hellocharts.util.ChartUtils;

/**
 * Created by H151136 on 6/15/2016.
 */
public class Utils {

    public static final int OPERATE_TYPE_ADD = 0;
    public static final int OPERATE_TYPE_SUB = 1;
    public static final int OPERATE_TYPE_MUL = 2;
    public static final int OPERATE_TYPE_DIV = 3;

    public static final int[] COLORS = {ChartUtils.DEFAULT_COLOR, ChartUtils.COLOR_ORANGE,
            ChartUtils.COLOR_BLUE, ChartUtils.COLOR_VIOLET};

    public static final float DEFAULT_ABS_VALUE = 3.0f;
    public static final float DEFAULT_TRANS_VALUE = 100.0f;
    public static final int DEFAULT_ENERGY_VALUE = 65535;

    public static boolean needToSave = false;

    public static String FILE_PATH_ONLAB = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Onlab/";
    public static String FILE_PATH_PHOTOMETRIC_MEASURE = FILE_PATH_ONLAB + "/PhtotmetricMeasure/";
    public static String FILE_PATH_QUANTITATIVE_ANALYSIS = FILE_PATH_ONLAB + "/QuantitativeAnalysis/";
    public static String FILE_PATH_WAVELENGTH_SCAN = FILE_PATH_ONLAB + "/WavelengthScan/";
    public static String FILE_PATH_TIME_SCAN = FILE_PATH_ONLAB + "/TimeScan/";
    public static String FILE_PATH_MULTIPLE_WAVELENGTH = FILE_PATH_ONLAB + "/MultipleWavelengthScan/";
    public static String FILE_PATH_DNA = FILE_PATH_ONLAB + "/DNA/";

    public static boolean isFake() {
        return false;
    }

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

    public static void showMessageDialog(Context context, String title, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setMessage(msg);
        builder.create().show();
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

    public static void showAlertDialog(Context context, String title, String message, DialogInterface.OnClickListener listner,
                                       DialogInterface.OnCancelListener cancelListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.ok_string, listner);
        builder.setOnCancelListener(cancelListener);
        builder.setCancelable(false);
        builder.create().show();
    }

    public static boolean checkWavelengthInvalid(Context context, float wavelength) {
        if (wavelength < 190 || (wavelength > 1100)) {
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

    public static void createDirectory(String dirPath) {
        File file = new File(dirPath);
        if(!file.exists()) {
            file.mkdirs();
        }
    }

    public static void checkDirectory() {
        createDirectory(FILE_PATH_ONLAB);
        createDirectory(FILE_PATH_PHOTOMETRIC_MEASURE);
        createDirectory(FILE_PATH_QUANTITATIVE_ANALYSIS);
        createDirectory(FILE_PATH_WAVELENGTH_SCAN);
        createDirectory(FILE_PATH_TIME_SCAN);
        createDirectory(FILE_PATH_MULTIPLE_WAVELENGTH);
        createDirectory(FILE_PATH_DNA);
    }

    public static File getPhotometricMeasureFile(String name) {
        return new File(FILE_PATH_PHOTOMETRIC_MEASURE + name);
    }

    public static File getQuantitativeAnalsisFile(String name) {
        return new File(FILE_PATH_QUANTITATIVE_ANALYSIS + name);
    }

    public static File getWavelengthScanFile(String name) {
        return new File(FILE_PATH_WAVELENGTH_SCAN + name);
    }

    public static File getTimeScanFile(String name) {
        return new File(FILE_PATH_TIME_SCAN + name);
    }

    public static File getMultipleWavelengthFile(String name) {
        return new File(FILE_PATH_MULTIPLE_WAVELENGTH + name);
    }

    public static File getDnaFile(String name) {
        return new File(FILE_PATH_DNA + name);
    }

    private static final Charset charset = Charset.forName("UTF-8");

    public static String encode(String key, String enc) {
        byte[] keyBytes = key.getBytes(charset);
        byte[] b = enc.getBytes(charset);
        for(int i = 0, size = b.length; i < size; i++) {
            for(byte keyBytes0: keyBytes) {
                b[i] = (byte)(b[i]^keyBytes0);
            }
        }
        return new String(b);
    }

    public static String decode(String key, String dec) {
        byte[] keyBytes = key.getBytes(charset);
        byte[] e = dec.getBytes(charset);
        byte[] dee = e;
        for(int i = 0, size = e.length; i < size; i++) {
            for(byte keyBytes0: keyBytes) {
                e[i] = (byte)(dee[i]^keyBytes0);
            }
        }
        return new String(e);
    }
}
