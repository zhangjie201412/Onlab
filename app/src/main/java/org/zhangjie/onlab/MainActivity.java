package org.zhangjie.onlab;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import org.zhangjie.onlab.ble.BtleManager;
import org.zhangjie.onlab.device.DeviceManager;
import org.zhangjie.onlab.dialog.BaselineDialog;
import org.zhangjie.onlab.dialog.BaselineDialog.BaselineOperateListener;
import org.zhangjie.onlab.dialog.DeviceCheckDialog;
import org.zhangjie.onlab.dialog.DevicesSelectDialog;
import org.zhangjie.onlab.dialog.LightMgrDialog;
import org.zhangjie.onlab.dialog.SaveNameDialog;
import org.zhangjie.onlab.dialog.SettingEditDialog;
import org.zhangjie.onlab.dialog.WavelengthDialog;
import org.zhangjie.onlab.fragment.AboutFragment;
import org.zhangjie.onlab.fragment.DnaFragment;
import org.zhangjie.onlab.fragment.FragmentCallbackListener;
import org.zhangjie.onlab.fragment.HelloChartFragment;
import org.zhangjie.onlab.fragment.MainFragment;
import org.zhangjie.onlab.fragment.MultipleWavelengthFragment;
import org.zhangjie.onlab.fragment.PhotometricMeasureFragment;
import org.zhangjie.onlab.fragment.QuantitativeAnalysisFragment;
import org.zhangjie.onlab.fragment.TimeScanFragment;
import org.zhangjie.onlab.fragment.WavelengthScanFragment;
import org.zhangjie.onlab.otto.AboutExitEvent;
import org.zhangjie.onlab.otto.BusProvider;
import org.zhangjie.onlab.otto.CancelEvent;
import org.zhangjie.onlab.otto.DismissWarmDialog;
import org.zhangjie.onlab.otto.DnaCallbackEvent;
import org.zhangjie.onlab.otto.FileOperateEvent;
import org.zhangjie.onlab.otto.MultipleWavelengthCallbackEvent;
import org.zhangjie.onlab.otto.QaUpdateEvent;
import org.zhangjie.onlab.otto.RezeroEvent;
import org.zhangjie.onlab.otto.SetOperateEvent;
import org.zhangjie.onlab.otto.SetOperateModeEvent;
import org.zhangjie.onlab.otto.SetWavelengthEvent;
import org.zhangjie.onlab.otto.SettingEvent;
import org.zhangjie.onlab.otto.UpdateFragmentEvent;
import org.zhangjie.onlab.otto.WaitProgressEvent;
import org.zhangjie.onlab.otto.WavelengthScanCallbackEvent;
import org.zhangjie.onlab.otto.WavelengthScanCancelEvent;
import org.zhangjie.onlab.utils.MD5;
import org.zhangjie.onlab.utils.SharedPreferenceUtils;
import org.zhangjie.onlab.utils.Utils;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements WavelengthDialog.WavelengthInputListern,
        FragmentCallbackListener, View.OnClickListener, DialogInterface.OnClickListener {

    private static boolean isExit = false;
    private MainFragment mMain;
    private PhotometricMeasureFragment mPhotometricFragment;
    private TimeScanFragment mTimeScanFragment;
    private WavelengthScanFragment mWavelengthScanFragment;
    private QuantitativeAnalysisFragment mQuantitativeAnalysisFragment;
    private MultipleWavelengthFragment mMultipleWavelengthFragment;
    private AboutFragment mAboutFragment;
    private DnaFragment mDnaFragment;
    private HelloChartFragment mHelloChart;
    private TextView mTitleTextView;
    private TextView mBottomWavelength;
    private TextView mBottomAbs;
    private TextView mBottomTrans;

    private final String TAG = "Onlab.MainActivity";
    private boolean mIsBluetoothConnected = false;
    private boolean mIsInitialized = false;
    private Toolbar mTopToolbar;
    private Toolbar mStatusToolbar;
    private LinearLayout mSelectall;
    private LinearLayout mDelete;
    private boolean mOperateMode = false;

    private WavelengthDialog mWavelengthDialog;
    private DevicesSelectDialog mDeviceSelectDialog;
    private DeviceCheckDialog mDeviceCheckDialog;
    private SettingEditDialog mPeakDialog;

    private DeviceManager mDeviceManager;

    private Toast mToast;
    private BluetoothAdapter mBluetoothAdapter;
    private ProgressDialog mWaitDialog;
    //++++UV DATA
    private int[] mDark;
    private int[] mDarkRef;
    private int mA = 2;
    private int mARef = 2;
    private int mI0 = 20000;
    private int mI0Ref = 20000;
    private float mWavelength = 0;
    //----
    private final int WAVELENGTH_TIMEOUT = 120000;
    private final int PROCESS_TIMEOUT = 5000;
    private Handler mHandler = new Handler();

    private boolean mWavelengthScanRezero = false;

    private BaselineDialog mBaselineDialog;
    private LightMgrDialog mLightMgrDialog;

    private Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case DeviceManager.UI_MSG_DEVICE_CONNECTED:
                    mIsBluetoothConnected = true;
                    setBluetoothConnected(true);
                    dismissDialog();
//                    if(mDeviceSelectDialog != null) {
//                        mDeviceSelectDialog.dismiss();
//                    }
                    //device self check
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(50);
                                //show check self dialog
                                mDeviceCheckDialog.show(getFragmentManager(), "DeviceCheck");
                                Thread.sleep(1500);
                                mDeviceManager.getStatus();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();

//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//                                Thread.sleep(2000);
//                                //
//                                mDeviceManager.initializeWork();
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }).start();
                    break;
                case DeviceManager.UI_MSG_DEVICE_DISCONNECTED:
                    mIsBluetoothConnected = false;
                    setBluetoothConnected(false);
                    break;

                case DeviceManager.UI_MSG_DEVICE_SCAN:
                    Bundle bundle = msg.getData();
                    String name = bundle.getString("name");
                    String addr = bundle.getString("addr");
                    Log.d(TAG, "###name = " + name);
//                    mDeviceSelectDialog.addDevice(name, addr);
                    String address = DeviceApplication.getInstance().getSpUtils().getMacAddress();
                    if (address.equals(addr)) {
                        BtleManager.getInstance().connect(address);
                    }
                    break;
                case DeviceManager.UI_MSG_DEVICE_DATA:
                    Bundle data = msg.getData();
                    int entryFlag = msg.arg1;
                    String[] recvMsg = data.getStringArray("MSG");
                    Log.d(TAG, "recv tag = " + recvMsg[0]);
                    process(entryFlag, recvMsg);
                    break;
            }
        }
    };

    private void process(int flag, String[] msg) {
        if ((flag & DeviceManager.WORK_ENTRY_FLAG_GET_STATUS) != 0) {
            //getstatus ertry
            Log.d(TAG, "GET STATUS ENTRY");
            work_entry_getstatus(msg);
        }
        if ((flag & DeviceManager.WORK_ENTRY_FLAG_INITIALIZE) != 0) {
            //initialzation ertry
            Log.d(TAG, "INITIALZE ENTRY");
            work_entry_initialize(msg);
        }
        if ((flag & DeviceManager.WORK_ENTRY_FLAG_UPDATE_STATUS) != 0) {
            mDeviceManager.clearFlag(DeviceManager.WORK_ENTRY_FLAG_INITIALIZE);
            //update status entry
            Log.v(TAG, "UPDATE STATUS ENTRY");
            if (mIsInitialized) {
                work_entry_updatestatus(msg);
            } else {
                work_entry_initialize(msg);
            }
        }
        if ((flag & DeviceManager.WORK_ENTRY_FLAG_SET_WAVELENGTH) != 0) {
            //set wavelength entry
            Log.d(TAG, "SET WAVELENGTH ENTRY");
            work_entry_set_wavelength(msg);
        }
        if ((flag & DeviceManager.WORK_ENTRY_FLAG_SET_LAMP_WAVELENGTH) != 0) {
            //set lamp wavelength entry
            Log.d(TAG, "SET LAMP WAVELENGTH ENTRY");
            work_entry_set_lamp_wavelength(msg);
        }
        if ((flag & DeviceManager.WORK_ENTRY_FLAG_REZERO) != 0) {
            //rezero entry
            Log.d(TAG, "REZERO ENTRY");
            work_entry_rezero(msg);
        }
        if ((flag & DeviceManager.WORK_ENTRY_FLAG_PHOTOMETRIC_MEASURE) != 0) {
            //photometric measure entry
            Log.d(TAG, "PHOTOMETRIC MEASURE ENTRY");
            work_entry_photometric_measure(msg);
        }
        if ((flag & DeviceManager.WORK_ENTRY_FLAG_TIME_SCAN) != 0) {
            //time scan entry
            Log.d(TAG, "TIME SCAN ENTRY");
            work_entry_time_scan(msg);
        }
        if ((flag & DeviceManager.WORK_ENTRY_FLAG_BASELINE) != 0) {
            //baseline entry
            Log.d(TAG, "BASELINE ENTRY");
            work_entry_baseline(msg);
        }
        if ((flag & DeviceManager.WORK_ENTRY_FLAG_DOREZERO) != 0) {
            //dorezero entry
            Log.d(TAG, "DOREZERO ENTRY");
            work_entry_dorezero(msg);
        }
        if ((flag & DeviceManager.WORK_ENTRY_FLAG_WAVELENGTH_SCAN) != 0) {
            //do wavelength scan entry
            Log.d(TAG, "WAVELENGTH SCAN ENTRY");
            work_entry_wavelength_scan(msg);
        }
        if ((flag & DeviceManager.WORK_ENTRY_FLAG_MULTIPLE_WAVELENGTH_REZERO) != 0) {
            //do multiple wavelength rezero entry
            Log.d(TAG, "MULTIPLE WAVELENGTH REZERO ENTRY");
            work_entry_multiple_wavelength_rezero(msg);
        }
        if ((flag & DeviceManager.WORK_ENTRY_FLAG_MULTIPLE_WAVELENGTH_TEST) != 0) {
            //do multiple wavelength test entry
            Log.d(TAG, "MULTIPLE WAVELENGTH TEST ENTRY");
            work_entry_multiple_wavelength_test(msg);
        }
        if ((flag & DeviceManager.WORK_ENTRY_FLAG_QUANTITATIVE_ANALYSIS) != 0) {
            //do quantitative analysis entry
            Log.d(TAG, "QUANTITATIVE ANALYSIS ENTRY");
            work_entry_quantitative_analysis(msg);
        }

        if ((flag & DeviceManager.WORK_ENTRY_FLAG_DNA_REZERO) != 0) {
            Log.d(TAG, "DNA REZERO ENTRY");
            work_entry_dna_rezero(msg);
        }

        if ((flag & DeviceManager.WORK_ENTRY_FLAG_DNA_TEST) != 0) {
            Log.d(TAG, "DNA TEST ENTRY");
            work_entry_dna_test(msg);
        }

        if ((flag & DeviceManager.WORK_ENTRY_FLAG_QUANTITATIVE_ANALYSIS_SAMPLE) != 0) {
            Log.d(TAG, "QA SAMPLE ENTRY");
            work_entry_quantitative_analysis_sample(msg);
        }

        if ((flag & DeviceManager.WORK_ENTRY_FLAG_QUANTITATIVE_ANALYSIS_REZERO) != 0) {
            Log.d(TAG, "QA REZERO ENTRY");
            work_entry_quantitative_analysis_rezero(msg);
        }

        //...
        if ((flag & DeviceManager.WORK_ENTRY_FLAG_SINGLE_COMMAND) != 0) {
            //do quantitative analysis entry
            Log.d(TAG, "SINGLE COMMAND ENTRY");
            work_entry_single_command(msg);
        }
    }

    private boolean mSkipWarm = false;
    private AlertDialog mWarmAlertDialog;

    final Handler mWarmHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int left = msg.what;
            Log.d(TAG, "left = " + left);
            if (!mWarmAlertDialog.isShowing() && !mSkipWarm) {
                mWarmAlertDialog.show();
            }
            if (left > 0) {
                TextView tv = (TextView) mWarmAlertDialog.findViewById(R.id.tv_dialog_content);
                tv.setText(getString(R.string.skip_warm) + Utils.secondToMinute(left));
            } else {
                if (mWarmAlertDialog.isShowing()) {
                    mWarmAlertDialog.dismiss();
                }
                mDeviceManager.skip();
                if (!mIsInitialized) {
                    initDialog();
                    mDeviceManager.initializeWork();
                }
                mSkipWarm = true;
            }
        }
    };

    private void work_entry_getstatus(String[] msg) {
        String tag = msg[0];
        Log.d(TAG, "getstatustag = " + tag);
        for (int i = 0; i < msg.length; i++) {
            Log.d(TAG, "-> " + msg[i]);
        }
        if (tag.startsWith(DeviceManager.TAG_GET_STATUS)) {
            Log.d(TAG, "###TAG_GET_STATUS!");
        }
        if (tag.startsWith(DeviceManager.TAG_ONLINE)) {
            Log.d(TAG, "###TAG_ONLINE!");
            mDeviceCheckDialog.dismiss();
            if (!mIsInitialized) {
                initDialog();
                //mDeviceManager.doSingleCommand(DeviceManager.DEVICE_CMD_LIST_SET_QUIT);
                mDeviceManager.initializeWork();
            }
        }
        if (tag.startsWith(DeviceManager.TAG_READY)) {
            Log.d(TAG, "###TAG_READY!");
            mDeviceCheckDialog.dismiss();
            if (!mIsInitialized) {
                initDialog();
                //mDeviceManager.doSingleCommand(DeviceManager.DEVICE_CMD_LIST_SET_QUIT);
                mDeviceManager.initializeWork();
            }
        }
        if (tag.startsWith(DeviceManager.TAG_CHECK_LAMP_START)) {
//            mDeviceCheckDialog.addItem(getString(R.string.lamp) + getString(R.string.ing));
        }
        if (tag.startsWith(DeviceManager.TAG_CHECK_LAMP_DONE)) {
            mDeviceCheckDialog.addItem(getString(R.string.lamp) + getString(R.string.done));
        }
        if (tag.startsWith(DeviceManager.TAG_CHECK_AD_START)) {
//            mDeviceCheckDialog.addItem(getString(R.string.ad) + getString(R.string.ing));
        }
        if (tag.startsWith(DeviceManager.TAG_CHECK_AD_DONE)) {
            mDeviceCheckDialog.addItem(getString(R.string.ad) + getString(R.string.done));
        }
        if (tag.startsWith(DeviceManager.TAG_CHECK_DEUTERIUM_START)) {
//            mDeviceCheckDialog.addItem(getString(R.string.deuterium) + getString(R.string.ing));
        }
        if (tag.startsWith(DeviceManager.TAG_CHECK_DEUTERIUM_DONE)) {
            mDeviceCheckDialog.addItem(getString(R.string.deuterium) + getString(R.string.done));
        }
        if (tag.startsWith(DeviceManager.TAG_CHECK_TUNGSTEN_START)) {
//            mDeviceCheckDialog.addItem(getString(R.string.tungsten) + getString(R.string.ing));
        }
        if (tag.startsWith(DeviceManager.TAG_CHECK_TUNGSTEN_OK)) {
            mDeviceCheckDialog.addItem(getString(R.string.tungsten) + getString(R.string.done));
        }
        if (tag.startsWith(DeviceManager.TAG_CHECK_WAVE_START)) {
//            mDeviceCheckDialog.addItem(getString(R.string.wave) + getString(R.string.ing));
        }
        if (tag.startsWith(DeviceManager.TAG_CHECK_WAVE_DONE)) {
            Log.d(TAG, "####WAVE DONE");
            mDeviceCheckDialog.addItem(getString(R.string.wave) + getString(R.string.done));
        }
        if (tag.startsWith(DeviceManager.TAG_CHECK_PARA_START)) {
//            mDeviceCheckDialog.addItem(getString(R.string.para) + getString(R.string.ing));
        }
        if (tag.startsWith(DeviceManager.TAG_CHECK_PARA_DONE)) {
            Log.d(TAG, "####PARA DONE");
            mDeviceCheckDialog.addItem(getString(R.string.para) + getString(R.string.done));
        }
        if (tag.startsWith(DeviceManager.TAG_CHECK_DARK_START)) {
//            mDeviceCheckDialog.addItem(getString(R.string.dark) + getString(R.string.ing));
        }
        if (tag.startsWith(DeviceManager.TAG_CHECK_DARK_DONE)) {
            Log.d(TAG, "####DARK DONE");
            mDeviceCheckDialog.addItem(getString(R.string.dark) + getString(R.string.done));
        }
        if (tag.startsWith(DeviceManager.TAG_WARM)) {
            mDeviceCheckDialog.addItem(getString(R.string.warm));
//            mDeviceCheckDialog.warm();

            msg[1] = msg[1].replaceAll("\\D+", "").replaceAll("\r", "").replaceAll("\n", "").trim();
            final int leftTime = Integer.parseInt(msg[1]);
            //start thread to count seconds
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (int i = 0; i < leftTime; i++) {
                            if (mSkipWarm) {
                                return;
                            }
                            mWarmHandler.sendEmptyMessage(leftTime - i);
                            Thread.sleep(1000);
                        }
                        mWarmHandler.sendEmptyMessage(0);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private void work_entry_initialize(String[] msg) {
        String tag = msg[0];

        Log.d(TAG, "tag = " + tag);

        if (tag.startsWith(DeviceManager.TAG_CONNECT)) {
            //connect
            if (msg[1].startsWith("ok.")) {
                Log.d(TAG, "connect successfully!");
                dismissDialog();
                mIsInitialized = true;
                toastShow(getString(R.string.connect_done));
//                mDeviceSelectDialog.dismiss();
                mDeviceCheckDialog.dismiss();
                mDeviceManager.start();
            }
        } else if (tag.startsWith(DeviceManager.TAG_GET_WAVELENGTH)) {
            //get wavelength
            Log.d(TAG, "get wavelength = " + msg[1]);
        } else if (tag.startsWith(DeviceManager.TAG_GET_DARK)) {
            //get dark
            for (int i = 0; i < 8; i++) {
                msg[i + 1] = msg[i + 1].replaceAll("\\D+", "").replaceAll("\r", "").replaceAll("\n", "").trim();
                mDark[i] = Integer.parseInt(msg[i + 1]);
            }
            for (int i = 8; i < 16; i++) {
                msg[i + 1] = msg[i + 1].replaceAll("\\D+", "").replaceAll("\r", "").replaceAll("\n", "").trim();
                mDarkRef[i - 8] = Integer.parseInt(msg[i + 1]);
            }
        } else if (tag.startsWith(DeviceManager.TAG_GET_A)) {
            //get a
            msg[1] = msg[1].replaceAll("\\D+", "").replaceAll("\r", "").replaceAll("\n", "").trim();
            mA = Integer.parseInt(msg[1]);
        } else if (tag.startsWith(DeviceManager.TAG_GET_LAMP_WAVELENGTH)) {
            //get wavelength
            Log.d(TAG, "get lamp wavelength = " + msg[1]);
            msg[1] = msg[1].replaceAll(" ", "").replaceAll("\r", "").replaceAll("\n", "").trim();
            float wavelength = Float.parseFloat(msg[1]);
            DeviceApplication.getInstance().getSpUtils().setLampWavelength(wavelength);
        }
    }

    private void work_entry_updatestatus(String[] msgs) {
        String[] msg = msgs.clone();
        String tag = msg[0];

        if (msgs[0].startsWith(DeviceManager.TAG_SET_WAVELENGTH)) {
            Log.d(TAG, "##dismiss dialog");
            dismissDialog();
//            mDeviceManager.setLoopThreadRestart();
        } else if (tag.startsWith("ge2 5")) {
            int[] energies = new int[5];
            int[] energiesRef = new int[5];
            int I1 = 0;
            int I1Ref = 0;
            float trans = 0;
            float abs = 0;

            for (int i = 0; i < 5; i++) {
                msg[i + 1] = msg[i + 1].replaceAll("\\D+", "").replaceAll("\r", "").replaceAll("\n", "").trim();
                energies[i] = Integer.parseInt(msg[i + 1], 10);
                I1 += energies[i];
            }
            for (int i = 5; i < 10; i++) {
                msg[i + 1] = msg[i + 1].replaceAll("\\D+", "").replaceAll("\r", "").replaceAll("\n", "").trim();
                energiesRef[i - 5] = Integer.parseInt(msg[i + 1], 10);
                I1Ref += energiesRef[i - 5];
            }
            I1 /= 5;
            I1Ref /= 5;

            if (mA > 0) {
                Log.d(TAG, "$$$$ wavelength = xxxx" + ", I1 = " + I1 + ", gain = " + mA + ", I0 = " + mI0);
                int I0 = (I1Ref - mDarkRef[mA - 1]) * (mI0 - mDark[mA - 1]) / (mI0Ref - mDarkRef[mARef - 1]);
                trans = (float) (I1 - mDark[mA - 1]) / (float) I0;
                abs = (float) -Math.log10(trans);
                //get valid trans and abs
                trans = Utils.getValidTrans(trans);
                abs = Utils.getValidAbs(abs);
                trans *= 100.0f;
                updateAbs(abs);
                updateTrans(trans);
            }
        } else if (tag.startsWith(DeviceManager.TAG_GET_WAVELENGTH)) {
            msg[1] = msg[1].replaceAll(" ", "").replaceAll("\r", "").replaceAll("\n", "").trim();
            float wavelength = Float.parseFloat(msg[1]);
            updateWavelength(wavelength);
        }
    }

    private void work_entry_photometric_measure(String[] msgs) {
        String[] msg = msgs.clone();
        String tag = msg[0];

        if (tag.startsWith("ge2 6")) {
            int[] energies = new int[6];
            int[] energiesRef = new int[6];
            int I1 = 0;
            int I1Ref = 0;
            float trans = 0;
            float abs = 0;

            for (int i = 0; i < 6; i++) {
                msg[i + 1] = msg[i + 1].replaceAll("\\D+", "").replaceAll("\r", "").replaceAll("\n", "").trim();
                energies[i] = Integer.parseInt(msg[i + 1], 10);
                I1 += energies[i];
            }
            for (int i = 6; i < 12; i++) {
                msg[i + 1] = msg[i + 1].replaceAll("\\D+", "").replaceAll("\r", "").replaceAll("\n", "").trim();
                energiesRef[i - 6] = Integer.parseInt(msg[i + 1], 10);
                I1Ref += energiesRef[i - 6];
            }
            I1 /= 8;
            I1Ref /= 8;
            Log.d(TAG, "energy = " + I1);
            Log.d(TAG, "energyRef = " + I1Ref);

            if (mA > 0) {
                int I0 = (I1Ref - mDarkRef[mA - 1]) * (mI0 - mDark[mA - 1]) / (mI0Ref - mDarkRef[mARef - 1]);
                trans = (float) (I1 - mDark[mA - 1]) / (float) I0;
                abs = (float) -Math.log10(trans);
                trans = Utils.getValidTrans(trans);
                abs = Utils.getValidAbs(abs);
                trans *= 100.0f;
                Log.d(TAG, "trans = " + trans);
                Log.d(TAG, "abs = " + abs);
                UpdateFragmentEvent event = new UpdateFragmentEvent();
                event.setType(UpdateFragmentEvent.UPDATE_FRAGMENT_EVENT_TYPE_PHOTOMETRIC_MEASURE);
                event.setEnergy(I1);
                event.setTrans(trans);
                event.setAbs(abs);
                event.setWavelength(mWavelength);
                BusProvider.getInstance().post(event);
                mDeviceManager.setLoopThreadRestart();
            }
        }
    }

    private void work_entry_time_scan(String[] msgs) {
        String[] msg = msgs.clone();
        String tag = msg[0];

        if (tag.startsWith("ge2 6")) {
            int[] energies = new int[6];
            int I1 = 0;
            int[] energiesRef = new int[6];
            int I1Ref = 0;
            float trans = 0;
            float abs = 0;

            for (int i = 0; i < 6; i++) {
                msg[i + 1] = msg[i + 1].replaceAll("\\D+", "").replaceAll("\r", "").replaceAll("\n", "").trim();
                energies[i] = Integer.parseInt(msg[i + 1], 10);
                I1 += energies[i];
            }
            for (int i = 6; i < 12; i++) {
                msg[i + 1] = msg[i + 1].replaceAll("\\D+", "").replaceAll("\r", "").replaceAll("\n", "").trim();
                energiesRef[i - 6] = Integer.parseInt(msg[i + 1], 10);
                I1Ref += energiesRef[i - 6];
            }
            I1 /= 6;
            I1Ref /= 6;
            Log.d(TAG, "energy = " + I1);
            Log.d(TAG, "energyRef = " + I1Ref);

            if (mA > 0) {
                int I0 = (I1Ref - mDarkRef[mA - 1]) * (mI0 - mDark[mA - 1]) / (mI0Ref - mDarkRef[mARef - 1]);
                trans = (float) (I1 - mDark[mA - 1]) / (float) I0;
                abs = (float) -Math.log10(trans);
                trans = Utils.getValidTrans(trans);
                abs = Utils.getValidAbs(abs);
                trans *= 100.0f;
                Log.d(TAG, "trans = " + trans);
                Log.d(TAG, "abs = " + abs);
                UpdateFragmentEvent event = new UpdateFragmentEvent();
                event.setType(UpdateFragmentEvent.UPDATE_FRAGMENT_EVENT_TYPE_TIME_SCAN);
                event.setEnergy(I1);
                event.setTrans(trans);
                event.setAbs(abs);

                BusProvider.getInstance().post(event);
            }
        }
    }

    /*
    set wavelength, set gain, get energy
    1. if energy > 40000 && gain > 0, gain --, get energy
    2. if energy < 20000 && gain < 8, gain ++, get energy
    if 20000 <= energy <= 40000, wavelength--, loop.
    * */
    //+++
    private static int DIRECTION_UNKNOW = 0;
    private static int DIRECTION_INCREASE = 1;
    private static int DIRECTION_DECREASE = 2;
    private float mBaselineWavelength;
    private int mBaselineGain;
    private int mBaselineGainRef;
    private int mDirection;
    private int mDirectionRef;
    private boolean stopBaseline = true;
    private final int BASELINE_STEP_INIT = 0;
    private final int BASELINE_STEP_SAMPLE = 1;
    private final int BASELINE_STEP_REF_SAMPLE = 2;
    private int mBaselineStep = BASELINE_STEP_INIT;
    private int mBaselineEnergy;
    private int mBaselineEnergyRef;

    //---
    private void baselineInit() {
        mBaselineWavelength = DeviceManager.BASELINE_END;
        mBaselineGain = 8;
        mBaselineGainRef = 8;
        mDirection = DIRECTION_UNKNOW;
        mDirectionRef = DIRECTION_UNKNOW;
        mBaselineDialog.clear();
        stopBaseline = false;
        loadBaselineDialog();
    }

    private void baselineDoneCallback() {
        Log.d(TAG, "do baseline work done!");
        mDeviceManager.setLoopThreadRestart();
        dismissDialog();
        if (!stopBaseline) {
            mDeviceManager.saveBaseline();
            mBaselineDialog.doneCallback();
            toastShow(getString(R.string.baseline_done));
        }
    }

    private void baselineProcess(int energy) {
        Log.d(TAG, "baselineProcess");
        if (energy > DeviceManager.ENERGY_FIT_UP) {
            //> 40000
            if (mBaselineGain > 1) {
                if (mDirection == DIRECTION_INCREASE) {
                    //update
                    Log.d(TAG, "Update wavelength = " + mBaselineWavelength + ", gain = " + mBaselineGain + ", energy = " + energy);
                    //save gain
                    mDeviceManager.setGain((int) mBaselineWavelength, mBaselineGain);
//                    updateBaseline(mBaselineWavelength, mBaselineGain, energy);
                    mDirection = DIRECTION_UNKNOW;
//                    mBaselineWavelength = mBaselineWavelength - 1;
//                    if (mBaselineWavelength < DeviceManager.BASELINE_START) {
//                        baselineDoneCallback();
//                        return;
//                    }
//                    mDeviceManager.baselineWork((int) mBaselineWavelength, -1, -1);
                    mBaselineStep = BASELINE_STEP_REF_SAMPLE;
                    return;
                } else {
                    mBaselineGain--;
                    mDirection = DIRECTION_DECREASE;
                    mDeviceManager.baselineWork(-1, mBaselineGain, -1);
                }
            } else {
                //gain == 1
                Log.d(TAG, "Update wavelength = " + mBaselineWavelength + ", gain = 1, energy = " + energy);
                mDeviceManager.setGain((int) mBaselineWavelength, mBaselineGain);
//                updateBaseline(mBaselineWavelength, mBaselineGain, energy);
                mDirection = DIRECTION_UNKNOW;
//                mBaselineWavelength = mBaselineWavelength - 1;
//                if (mBaselineWavelength < DeviceManager.BASELINE_START) {
//                    baselineDoneCallback();
//                    return;
//                }
//                mDeviceManager.baselineWork((int) mBaselineWavelength, -1);
                mBaselineStep = BASELINE_STEP_REF_SAMPLE;
                return;
            }
        }
        if (energy < DeviceManager.ENERGY_FIT_DOWN) {
            //< 20000
            if (mBaselineGain < 8) {
                if (mDirection == DIRECTION_DECREASE) {
                    //update
                    Log.d(TAG, "Update wavelength = " + mBaselineWavelength + ", gain = " + mBaselineGain + ", energy = " + energy);
                    mDeviceManager.setGain((int) mBaselineWavelength, mBaselineGain);
//                    updateBaseline(mBaselineWavelength, mBaselineGain, energy);
                    mDirection = DIRECTION_UNKNOW;
//                    mBaselineWavelength = mBaselineWavelength - 1;
//                    if (mBaselineWavelength < DeviceManager.BASELINE_START) {
//                        baselineDoneCallback();
//                        return;
//                    }
//                    mDeviceManager.baselineWork((int) mBaselineWavelength, -1);
                    mBaselineStep = BASELINE_STEP_REF_SAMPLE;
                    return;
                } else {
                    mBaselineGain++;
                    mDirection = DIRECTION_INCREASE;
                    mDeviceManager.baselineWork(-1, mBaselineGain, -1);
                }
            } else {
                //gain == 8
                Log.d(TAG, "Update wavelength = " + mBaselineWavelength + ", gain = 1, energy = " + energy);
                mDeviceManager.setGain((int) mBaselineWavelength, mBaselineGain);
//                updateBaseline(mBaselineWavelength, mBaselineGain, energy);
                mDirection = DIRECTION_UNKNOW;
//                mBaselineWavelength = mBaselineWavelength - 1;
//                if (mBaselineWavelength < DeviceManager.BASELINE_START) {
//                    baselineDoneCallback();
//                    return;
//                }
//                mDeviceManager.baselineWork((int) mBaselineWavelength, -1);
                mBaselineStep = BASELINE_STEP_REF_SAMPLE;
                return;
            }
        }
        if (energy >= DeviceManager.ENERGY_FIT_DOWN && (energy <= DeviceManager.ENERGY_FIT_UP)) {
            //20000 <= energy <= 40000
            Log.d(TAG, "Update wavelength = " + mBaselineWavelength + ", gain = " + mBaselineGain + ", energy = " + energy);
            mDeviceManager.setGain((int) mBaselineWavelength, mBaselineGain);
//            updateBaseline(mBaselineWavelength, mBaselineGain, energy);
            mDirection = DIRECTION_UNKNOW;
//            mBaselineWavelength = mBaselineWavelength - 1;
//            if (mBaselineWavelength < DeviceManager.BASELINE_START) {
//                baselineDoneCallback();
//                return;
//            }
//            mDeviceManager.baselineWork((int) mBaselineWavelength, -1);
            mBaselineStep = BASELINE_STEP_REF_SAMPLE;
            return;
        }
    }

    private void baselineRefProcess(int energy1, int energy2) {
        Log.d(TAG, "baselineRefProcess");
        if (energy2 > DeviceManager.ENERGY_FIT_UP) {
            //> 40000
            if (mBaselineGainRef > 1) {
                if (mDirectionRef == DIRECTION_INCREASE) {
                    //save gain
                    mDeviceManager.setGainRef((int) mBaselineWavelength, mBaselineGainRef);
                    updateBaseline(mBaselineWavelength, mBaselineGain, mBaselineGainRef, energy1, energy2);
                    mDirectionRef = DIRECTION_UNKNOW;
                    mBaselineWavelength = mBaselineWavelength - 1;
                    if (mBaselineWavelength < DeviceManager.BASELINE_START) {
                        baselineDoneCallback();
                        return;
                    }
                    mDeviceManager.baselineWork((int) mBaselineWavelength, -1, -1);
                } else {
                    mBaselineGainRef--;
                    mDirectionRef = DIRECTION_DECREASE;
                    mDeviceManager.baselineWork(-1, -1, mBaselineGainRef);
                    mBaselineStep = BASELINE_STEP_SAMPLE;
                }
            } else {
                //gain == 1
                mDeviceManager.setGainRef((int) mBaselineWavelength, mBaselineGainRef);
                updateBaseline(mBaselineWavelength, mBaselineGain, mBaselineGainRef, energy1, energy2);
                mDirectionRef = DIRECTION_UNKNOW;
                mBaselineWavelength = mBaselineWavelength - 1;
                if (mBaselineWavelength < DeviceManager.BASELINE_START) {
                    baselineDoneCallback();
                    return;
                }
                mDeviceManager.baselineWork((int) mBaselineWavelength, -1, -1);
                mBaselineStep = BASELINE_STEP_SAMPLE;
            }
        }
        if (energy2 < DeviceManager.ENERGY_FIT_DOWN) {
            //< 20000
            if (mBaselineGainRef < 8) {
                if (mDirectionRef == DIRECTION_DECREASE) {
                    //update
                    mDeviceManager.setGainRef((int) mBaselineWavelength, mBaselineGainRef);
                    updateBaseline(mBaselineWavelength, mBaselineGain, mBaselineGainRef, energy1, energy2);
                    mDirectionRef = DIRECTION_UNKNOW;
                    mBaselineWavelength = mBaselineWavelength - 1;
                    if (mBaselineWavelength < DeviceManager.BASELINE_START) {
                        baselineDoneCallback();
                        return;
                    }
                    mDeviceManager.baselineWork((int) mBaselineWavelength, -1, -1);
                    mBaselineStep = BASELINE_STEP_SAMPLE;
                } else {
                    mBaselineGainRef++;
                    mDirectionRef = DIRECTION_INCREASE;
                    mDeviceManager.baselineWork(-1, -1, mBaselineGainRef);
                }
            } else {
                //gain == 8
                mDeviceManager.setGainRef((int) mBaselineWavelength, mBaselineGainRef);
                updateBaseline(mBaselineWavelength, mBaselineGain, mBaselineGainRef, energy1, energy2);
                mDirectionRef = DIRECTION_UNKNOW;
                mBaselineWavelength = mBaselineWavelength - 1;
                if (mBaselineWavelength < DeviceManager.BASELINE_START) {
                    baselineDoneCallback();
                    return;
                }
                mDeviceManager.baselineWork((int) mBaselineWavelength, -1, -1);
                mBaselineStep = BASELINE_STEP_SAMPLE;
            }
        }
        if (energy2 >= DeviceManager.ENERGY_FIT_DOWN && (energy2 <= DeviceManager.ENERGY_FIT_UP)) {
            //20000 <= energy <= 40000
            mDeviceManager.setGainRef((int) mBaselineWavelength, mBaselineGainRef);
            updateBaseline(mBaselineWavelength, mBaselineGain, mBaselineGainRef, energy1, energy2);
            mDirectionRef = DIRECTION_UNKNOW;
            mBaselineWavelength = mBaselineWavelength - 1;
            if (mBaselineWavelength < DeviceManager.BASELINE_START) {
                baselineDoneCallback();
                return;
            }
            mDeviceManager.baselineWork((int) mBaselineWavelength, -1, -1);
            mBaselineStep = BASELINE_STEP_SAMPLE;
        }
    }

    private void work_entry_baseline(String[] msgs) {
        String tag = msgs[0];
        int energy;
        int energy2;

        if (stopBaseline) {
            baselineDoneCallback();
            return;
        }

        if (tag.startsWith(DeviceManager.TAG_SET_WAVELENGTH)) {
            mBaselineStep = BASELINE_STEP_SAMPLE;
            Log.d(TAG, "set wl");
        } else if (tag.startsWith(DeviceManager.TAG_SET_A)) {

        } else if (tag.startsWith("ge2 1") && (!tag.startsWith("ge2 10"))) {
            Log.d(TAG, "get2 1, step = " + mBaselineStep);
            if (mBaselineStep == BASELINE_STEP_SAMPLE) {
                msgs[1] = msgs[1].replaceAll("\\D+", "").replaceAll("\r", "").replaceAll("\n", "").trim();
                energy = Integer.parseInt(msgs[1]);
                baselineProcess(energy);
                mBaselineEnergy = energy;
            }
            if (mBaselineStep == BASELINE_STEP_REF_SAMPLE) {
                msgs[1] = msgs[1].replaceAll("\\D+", "").replaceAll("\r", "").replaceAll("\n", "").trim();
                energy = Integer.parseInt(msgs[1]);
                msgs[2] = msgs[2].replaceAll("\\D+", "").replaceAll("\r", "").replaceAll("\n", "").trim();
                energy2 = Integer.parseInt(msgs[2]);
                baselineRefProcess(mBaselineEnergy, energy2);
            }
        }
    }

    //+++
    private float mDorezeroWavelength = 0;
    private int mDorezeroGain;
    private final int REZERO_STEP_SAMPLE = 1;
    private final int REZERO_STEP_REF_SAMPLE = 2;
    private int mRezeroStep;
    //---

    private void rezeroProcessSample(int energy) {
        mDorezeroGain = mDeviceManager.getGainFromBaseline((int) mDorezeroWavelength);
        Log.d(TAG, "Update get gain = " + mDorezeroGain);
        int I0 = energy;
        float trans = (energy - mDark[mDorezeroGain - 1]) / (I0 - mDark[mDorezeroGain - 1]);
        trans = Utils.getValidTrans(trans);
        Log.d(TAG, "Update wavelength = " + mDorezeroWavelength + ", I0 = " + I0 + ", trans = " + trans);
        //save dark
        mDeviceManager.setDark(mDorezeroWavelength, I0);
        float start = DeviceApplication.getInstance().getSpUtils().getWavelengthscanStart();
        float interval = DeviceApplication.getInstance().getSpUtils().getWavelengthscanInterval();
//        if (mDorezeroWavelength - interval < start) {
//            Log.d(TAG, "do rezero scan done!");
//            mDorezeroWavelength = 0;
//            dismissDialog();
//            mDeviceManager.setLoopThreadRestart();
//            BusProvider.getInstance().post(new WavelengthScanCallbackEvent((WavelengthScanCallbackEvent.EVENT_TYPE_REZERO_DONE)));
//            //reset the wavelength to the end wavelength
//            float end = DeviceApplication.getInstance().getSpUtils().getWavelengthscanEnd();
//            loadWavelengthDialog(end);
//        }
    }

    private void rezeroProcessRefSample(int energy) {
        mDorezeroGain = mDeviceManager.getGainFromBaselineRef((int) mDorezeroWavelength);
        Log.d(TAG, "Update get gain = " + mDorezeroGain);
        int I0 = energy;
        float trans = (energy - mDarkRef[mDorezeroGain - 1]) / (I0 - mDarkRef[mDorezeroGain - 1]);
        trans = Utils.getValidTrans(trans);
        Log.d(TAG, "Update wavelength = " + mDorezeroWavelength + ", I0 = " + I0 + ", trans = " + trans);
        //save dark
        mDeviceManager.setDarkRef(mDorezeroWavelength, I0);
        float start = DeviceApplication.getInstance().getSpUtils().getWavelengthscanStart();
        float interval = DeviceApplication.getInstance().getSpUtils().getWavelengthscanInterval();
        if (mDorezeroWavelength - interval < start) {
            Log.d(TAG, "do rezero scan done!");
            mDorezeroWavelength = 0;
            dismissDialog();
            mDeviceManager.setLoopThreadRestart();
            BusProvider.getInstance().post(new WavelengthScanCallbackEvent((WavelengthScanCallbackEvent.EVENT_TYPE_REZERO_DONE)));
            //reset the wavelength to the end wavelength
            float end = DeviceApplication.getInstance().getSpUtils().getWavelengthscanEnd();
            loadWavelengthDialog(end);
        }
    }

    private void work_entry_dorezero(String[] msgs) {
        String tag = msgs[0];
        int energy;
        int energyRef;

        if (tag.startsWith(DeviceManager.TAG_SET_WAVELENGTH)) {
            String wl = msgs[0].substring(3);
            wl = wl.replaceAll(" ", "").replaceAll("\r", "").replaceAll("\n", "").trim();
            Log.d(TAG, "rezero wl = " + wl);
            mDorezeroWavelength = Float.parseFloat(wl);
            mWaitDialog.setMessage(getString(R.string.rezero_message)
                    + "(" + mDorezeroWavelength + getString(R.string.nm) + ")");
        } else if (tag.startsWith(DeviceManager.TAG_SET_A)) {
            mRezeroStep = REZERO_STEP_SAMPLE;
        } else if (tag.startsWith(DeviceManager.TAG_SET_R)) {
            mRezeroStep = REZERO_STEP_REF_SAMPLE;
        } else if (tag.startsWith("ge2 1")) {
            Log.d(TAG, "mDorezeroWavelength = " + mDorezeroWavelength);
            if (mDorezeroWavelength == 0) {
                return;
            }
            //get energy
            msgs[1] = msgs[1].replaceAll("\\D+", "").replaceAll("\r", "").replaceAll("\n", "").trim();
            energy = Integer.parseInt(msgs[1]);
            msgs[2] = msgs[2].replaceAll("\\D+", "").replaceAll("\r", "").replaceAll("\n", "").trim();
            energyRef = Integer.parseInt(msgs[2]);
            rezeroProcessSample(energy);
            rezeroProcessRefSample(energyRef);
        }
    }

    private float mWavelengthScanWavelength = 0;

    private void work_entry_wavelength_scan(String[] msgs) {
        String tag = msgs[0];
        int energy;
        int energyRef;

        Log.d(TAG, "msgs[0] = " + msgs[0]);

        if (tag.startsWith(DeviceManager.TAG_SET_WAVELENGTH)) {
            String wl = msgs[0].substring(3);
            wl = wl.replaceAll(" ", "").replaceAll("\r", "").replaceAll("\n", "").trim();
            mWavelengthScanWavelength = Float.parseFloat(wl);
            dismissDialog();
        } else if (tag.startsWith(DeviceManager.TAG_SET_A)) {

        } else if (tag.startsWith("ge2 1")) {
            if (mWavelengthScanWavelength == 0) {
                return;
            }
            //get energy
            msgs[1] = msgs[1].replaceAll("\\D+", "").replaceAll("\r", "").replaceAll("\n", "").trim();
            energy = Integer.parseInt(msgs[1]);
            msgs[2] = msgs[2].replaceAll("\\D+", "").replaceAll("\r", "").replaceAll("\n", "").trim();
            energyRef = Integer.parseInt(msgs[2]);

            int gain = mDeviceManager.getGainFromBaseline((int) mWavelengthScanWavelength);
            int gainRef = mDeviceManager.getGainFromBaselineRef((int) mWavelengthScanWavelength);

            int I0 = (energyRef - mDarkRef[gainRef - 1]) * (mDeviceManager.getDarkFromWavelength(mWavelengthScanWavelength) - mDark[gain - 1])
                    / (mDeviceManager.getDarkRefFromWavelength(mWavelengthScanWavelength) - mDarkRef[gainRef - 1]);
            float trans = (float) (energy - mDark[gain - 1]) / (float) I0;

//            float trans = (float) (I1 - mDark[gain - 1]) /
//                    (float) (mDeviceManager.getDarkFromWavelength(mWavelengthScanWavelength) - mDark[gain - 1]);
            float abs = (float) -Math.log10(trans);
            trans = Utils.getValidTrans(trans);
            abs = Utils.getValidAbs(abs);
            trans *= 100.0f;

            BusProvider.getInstance().post(new WavelengthScanCallbackEvent(WavelengthScanCallbackEvent.EVENT_TYPE_WORKING,
                    mWavelengthScanWavelength, abs, trans, energy, energyRef));
            float start = DeviceApplication.getInstance().getSpUtils().getWavelengthscanStart();
            float end = DeviceApplication.getInstance().getSpUtils().getWavelengthscanEnd();
            float interval = DeviceApplication.getInstance().getSpUtils().getWavelengthscanInterval();
            if (mWavelengthScanWavelength - interval < start) {
                Log.d(TAG, "do wavelength scan done!");
                mWavelengthScanWavelength = 0;
                BusProvider.getInstance().post(new WavelengthScanCallbackEvent(WavelengthScanCallbackEvent.EVENT_TYPE_WORK_DONE));
                mDeviceManager.setLoopThreadRestart();
                //reset the wavelength to the end wavelength
                loadWavelengthDialog(end);
            }
        }
    }

    //++
    private float mMultipleWavelength;

    //--
    private void work_entry_multiple_wavelength_rezero(String[] msgs) {
        String tag = msgs[0];
        int energy;

        if (tag.startsWith(DeviceManager.TAG_SET_WAVELENGTH)) {
            String wl = msgs[0].substring(3);
            wl = wl.replaceAll(" ", "").replaceAll("\r", "").replaceAll("\n", "").trim();
            Log.d(TAG, "rezero wl = " + wl);
            mMultipleWavelength = Float.parseFloat(wl);
        } else if (tag.startsWith(DeviceManager.TAG_REZERO)) {
            if (mMultipleWavelength == 0) {
                return;
            }
            msgs[1] = msgs[1].replaceAll(" ", "").replaceAll("\r", "").replaceAll("\n", "").trim();
            msgs[2] = msgs[2].replaceAll(" ", "").replaceAll("\r", "").replaceAll("\n", "").trim();
            msgs[3] = msgs[3].replaceAll(" ", "").replaceAll("\r", "").replaceAll("\n", "").trim();
            msgs[4] = msgs[4].replaceAll(" ", "").replaceAll("\r", "").replaceAll("\n", "").trim();
            mI0 = Integer.parseInt(msgs[1]);
            mA = Integer.parseInt(msgs[2]);
            mI0Ref = Integer.parseInt(msgs[3]);
            mARef = Integer.parseInt(msgs[4]);

            int I0 = Integer.parseInt(msgs[1]);
            mDeviceManager.setDark(mMultipleWavelength, I0);
            int gain = Integer.parseInt(msgs[2]);
            mDeviceManager.setGain((int) mMultipleWavelength, gain);
            mDeviceManager.setDarkRef(mMultipleWavelength, mI0Ref);
            mDeviceManager.setGainRef((int)mMultipleWavelength, mARef);

            if (mMultipleWavelength ==
                    MultipleWavelengthFragment.mOrderWavelengths[MultipleWavelengthFragment.mOrderWavelengths.length - 1]) {
                Log.d(TAG, "do multiple wavelength rezero done!");
                mMultipleWavelength = 0;
                dismissDialog();
                BusProvider.getInstance().post(new MultipleWavelengthCallbackEvent(MultipleWavelengthCallbackEvent.EVENT_TYPE_REZERO_DONE));
                mDeviceManager.setLoopThreadRestart();
                loadWavelengthDialog(MultipleWavelengthFragment.mOrderWavelengths[0]);
                mI0 = mDeviceManager.getDarkFromWavelength(MultipleWavelengthFragment.mOrderWavelengths[0]);
                mA = mDeviceManager.getGainFromBaseline((int) MultipleWavelengthFragment.mOrderWavelengths[0]);
                mI0Ref = mDeviceManager.getDarkRefFromWavelength(MultipleWavelengthFragment.mOrderWavelengths[0]);
                mARef = mDeviceManager.getGainFromBaselineRef((int) MultipleWavelengthFragment.mOrderWavelengths[0]);
            }
        }

    }

    private void work_entry_multiple_wavelength_test(String[] msgs) {
        String tag = msgs[0];
        int energy;
        int energyRef;

        Log.d(TAG, "msgs[0] = " + msgs[0]);

        if (tag.startsWith(DeviceManager.TAG_SET_WAVELENGTH)) {
            String wl = msgs[0].substring(3);
            wl = wl.replaceAll(" ", "").replaceAll("\r", "").replaceAll("\n", "").trim();
            mMultipleWavelength = Float.parseFloat(wl);
        } else if (tag.startsWith(DeviceManager.TAG_SET_A)) {

        } else if (tag.startsWith("ge2 6")) {
            if (mMultipleWavelength == 0) {
                return;
            }
            int[] energies = new int[6];
            int[] energiesRef = new int[6];
            //get energy
            energy = 0;
            energyRef = 0;
            for (int i = 0; i < 6; i++) {
                msgs[i + 1] = msgs[i + 1].replaceAll("\\D+", "").replaceAll("\r", "").replaceAll("\n", "").trim();
                energies[i] = Integer.parseInt(msgs[i + 1], 10);
                energy += energies[i];
            }
            for (int i = 6; i < 12; i++) {
                msgs[i + 1] = msgs[i + 1].replaceAll("\\D+", "").replaceAll("\r", "").replaceAll("\n", "").trim();
                energiesRef[i - 6] = Integer.parseInt(msgs[i + 1], 10);
                energyRef += energiesRef[i - 6];
            }
            energy /= 6;
            energyRef /= 6;

            int gain = mDeviceManager.getGainFromBaseline((int) mMultipleWavelength);
            int i0 = mDeviceManager.getDarkFromWavelength(mMultipleWavelength);
            int gainRef = mDeviceManager.getGainFromBaselineRef((int) mMultipleWavelength);
            int i0Ref = mDeviceManager.getDarkRefFromWavelength(mMultipleWavelength);
            Log.d(TAG, "$$$$ wavelength = " + mMultipleWavelength + ", energy = " + energy + ", gain = " + gain + ", I0 = " + i0);
            int I1 = energy;
            int I1Ref = energyRef;

            int I0 = (I1Ref - mDarkRef[gainRef - 1]) * (i0 - mDark[gain - 1]) / (i0Ref - mDarkRef[gainRef - 1]);
            float trans = (float) (I1 - mDark[gain - 1]) / (float) I0;

            float abs = (float) -Math.log10(trans);
            trans = Utils.getValidTrans(trans);
            abs = Utils.getValidAbs(abs);
            trans *= 100.0f;
            Log.d(TAG, "Update work_entry_multiple_wavelength_test wavelength = " + mMultipleWavelength + ", I1 = " + I1 + ", trans = " + trans);

            BusProvider.getInstance().post(new MultipleWavelengthCallbackEvent(MultipleWavelengthCallbackEvent.EVENT_TYPE_UPDATE,
                    mMultipleWavelength, abs, trans, I1));

            if (mMultipleWavelength ==
                    MultipleWavelengthFragment.mOrderWavelengths[MultipleWavelengthFragment.mOrderWavelengths.length - 1]) {
                Log.d(TAG, "do multiple wavelength test done!");
                mMultipleWavelength = 0;
                dismissDialog();
                BusProvider.getInstance().post(new MultipleWavelengthCallbackEvent(MultipleWavelengthCallbackEvent.EVENT_TYPE_TEST_DONE));
                mDeviceManager.setLoopThreadRestart();
                loadWavelengthDialog(MultipleWavelengthFragment.mOrderWavelengths[0]);
                mI0 = mDeviceManager.getDarkFromWavelength(MultipleWavelengthFragment.mOrderWavelengths[0]);
                mA = mDeviceManager.getGainFromBaseline((int) MultipleWavelengthFragment.mOrderWavelengths[0]);
                mI0Ref = mDeviceManager.getDarkRefFromWavelength(MultipleWavelengthFragment.mOrderWavelengths[0]);
                mARef = mDeviceManager.getGainFromBaselineRef((int) MultipleWavelengthFragment.mOrderWavelengths[0]);
            }
        }
    }

    //++
    private float mDnaWavelength;

    //--
    private void work_entry_dna_rezero(String[] msgs) {
        String tag = msgs[0];

        if (tag.startsWith(DeviceManager.TAG_SET_WAVELENGTH)) {
            String wl = msgs[0].substring(3);
            wl = wl.replaceAll(" ", "").replaceAll("\r", "").replaceAll("\n", "").trim();
            Log.d(TAG, "dna rezero wl = " + wl);
            mDnaWavelength = Float.parseFloat(wl);
        } else if (tag.startsWith(DeviceManager.TAG_REZERO)) {
            if (mDnaWavelength == 0) {
                return;
            }
            msgs[1] = msgs[1].replaceAll(" ", "").replaceAll("\r", "").replaceAll("\n", "").trim();
            msgs[2] = msgs[2].replaceAll(" ", "").replaceAll("\r", "").replaceAll("\n", "").trim();
            msgs[3] = msgs[3].replaceAll(" ", "").replaceAll("\r", "").replaceAll("\n", "").trim();
            msgs[4] = msgs[4].replaceAll(" ", "").replaceAll("\r", "").replaceAll("\n", "").trim();
            mI0 = Integer.parseInt(msgs[1]);
            mA = Integer.parseInt(msgs[2]);
            mI0Ref = Integer.parseInt(msgs[3]);
            mARef = Integer.parseInt(msgs[4]);

            int I0 = Integer.parseInt(msgs[1]);
            mDeviceManager.setDark(mDnaWavelength, I0);
            int gain = Integer.parseInt(msgs[2]);
            mDeviceManager.setGain((int) mDnaWavelength, gain);
            int I0Ref = Integer.parseInt(msgs[3]);
            mDeviceManager.setDarkRef(mDnaWavelength, I0Ref);
            int gainRef = Integer.parseInt(msgs[4]);
            mDeviceManager.setGainRef((int) mDnaWavelength, gainRef);
            if (mDnaWavelength == DnaFragment.refWavelength) {
                Log.d(TAG, "do dna wavelength rezero done!");
                mDnaWavelength = 0;
                dismissDialog();
                BusProvider.getInstance().post(new DnaCallbackEvent(DnaCallbackEvent.EVENT_TYPE_REZERO_DONE));
                mDeviceManager.setLoopThreadRestart();
            }
        }
    }

    private void work_entry_dna_test(String[] msgs) {
        String tag = msgs[0];
        int energy;
        int energyRef;

        Log.d(TAG, "msgs[0] = " + msgs[0]);

        if (tag.startsWith(DeviceManager.TAG_SET_WAVELENGTH)) {
            String wl = msgs[0].substring(3);
            wl = wl.replaceAll(" ", "").replaceAll("\r", "").replaceAll("\n", "").trim();
            mDnaWavelength = Float.parseFloat(wl);
        } else if (tag.startsWith(DeviceManager.TAG_SET_A)) {

        } else if (tag.startsWith("ge2 1")) {
            if (mDnaWavelength == 0) {
                return;
            }
            //get energy
            msgs[1] = msgs[1].replaceAll("\\D+", "").replaceAll("\r", "").replaceAll("\n", "").trim();
            msgs[2] = msgs[1].replaceAll("\\D+", "").replaceAll("\r", "").replaceAll("\n", "").trim();
            energy = Integer.parseInt(msgs[1]);
            energyRef = Integer.parseInt(msgs[2]);
            int gain = mDeviceManager.getGainFromBaseline((int) mDnaWavelength);
            int i0 = mDeviceManager.getDarkFromWavelength(mDnaWavelength);
            int gainRef = mDeviceManager.getGainFromBaselineRef((int) mDnaWavelength);
            int i0Ref = mDeviceManager.getDarkRefFromWavelength(mDnaWavelength);

            int I1 = energy;
            int I1Ref = energyRef;

            int I0 = (I1Ref - mDarkRef[gainRef - 1]) * (i0 - mDark[gain - 1]) / (i0Ref - mDarkRef[gainRef - 1]);
            float trans = (float) (I1 - mDark[gain - 1]) / (float) I0;
            float abs = (float) -Math.log10(trans);
            abs = Utils.getValidAbs(abs);

            Log.d(TAG, "Update abs = " + abs);

            BusProvider.getInstance().post(new DnaCallbackEvent(DnaCallbackEvent.EVENT_TYPE_UPDATE,
                    mDnaWavelength, abs));

            if (mDnaWavelength == DnaFragment.refWavelength) {
                Log.d(TAG, "do dna test done!");
                mDnaWavelength = 0;
                dismissDialog();
                BusProvider.getInstance().post(new DnaCallbackEvent(DnaCallbackEvent.EVENT_TYPE_TEST_DONE));
                mDeviceManager.setLoopThreadRestart();
            }
        }
    }

    private void work_entry_quantitative_analysis(String[] msgs) {
        String[] msg = msgs.clone();
        String tag = msg[0];

        if (tag.startsWith("ge 5")) {
            int[] energies = new int[5];
            int I1 = 0;
            float trans = 0;
            float abs = 0;

            for (int i = 0; i < 5; i++) {
                msg[i + 1] = msg[i + 1].replaceAll("\\D+", "").replaceAll("\r", "").replaceAll("\n", "").trim();
                energies[i] = Integer.parseInt(msg[i + 1], 10);
                I1 += energies[i];
            }
            I1 /= 5;

            if (mA > 0) {
                trans = (float) (I1 - mDark[mA - 1]) / (float) (mI0 - mDark[mA - 1]);
                abs = (float) -Math.log10(trans);
//                trans = Utils.getValidTrans(trans);
                abs = Utils.getValidAbs(abs);
//                trans *= 100.0f;
                Log.d(TAG, "Update abs = " + abs);
            }
            BusProvider.getInstance().post(new QaUpdateEvent(abs));
            mDeviceManager.setLoopThreadRestart();
        }
        //done
    }

    private float mSampleWavelength = 0;
    private float mSampleAbs1 = 0;
    private float mSampleAbs2 = 0;
    private float mSampleAbs3 = 0;

    private void work_entry_quantitative_analysis_sample(String[] msgs) {
        String[] msg = msgs.clone();
        String tag = msg[0];
        SharedPreferenceUtils sp = DeviceApplication.getInstance().getSpUtils();
        int wavelength_setting = sp.getQAWavelengthSetting();
        float wavelength1 = sp.getQAWavelength1();
        float wavelength2 = sp.getQAWavelength2();
        float wavelength3 = sp.getQAWavelength3();
        float ratio1 = sp.getQARatio1();
        float ratio2 = sp.getQARatio2();
        float ratio3 = sp.getQARatio3();

        if (tag.startsWith(DeviceManager.TAG_SET_WAVELENGTH)) {
            String wl = msgs[0].substring(3);
            wl = wl.replaceAll(" ", "").replaceAll("\r", "").replaceAll("\n", "").trim();
            Log.d(TAG, "rezero wl = " + wl);
            mSampleWavelength = Float.parseFloat(wl);
            if (mSampleWavelength == wavelength1) {
                mSampleAbs1 = 0;
                mSampleAbs2 = 0;
                mSampleAbs3 = 0;
            } else if (mSampleWavelength == wavelength2) {
                mSampleAbs2 = 0;
                mSampleAbs3 = 0;
            } else if (mSampleWavelength == wavelength3) {
                mSampleAbs3 = 0;
            }
        } else if (tag.startsWith("ge2 5")) {
            int[] energies = new int[5];
            int I1 = 0;
            int[] energiesRef = new int[5];
            int I1Ref = 0;
            float abs = 0;

            for (int i = 0; i < 5; i++) {
                msg[i + 1] = msg[i + 1].replaceAll("\\D+", "").replaceAll("\r", "").replaceAll("\n", "").trim();
                energies[i] = Integer.parseInt(msg[i + 1], 10);
                I1 += energies[i];
            }
            for (int i = 5; i < 10; i++) {
                msg[i + 1] = msg[i + 1].replaceAll("\\D+", "").replaceAll("\r", "").replaceAll("\n", "").trim();
                energiesRef[i - 5] = Integer.parseInt(msg[i + 1], 10);
                I1Ref += energiesRef[i - 5];
            }
            I1 /= 5;
            I1Ref /= 5;

            int gain = mDeviceManager.getGainFromBaseline((int) mSampleWavelength);
            int i0 = mDeviceManager.getDarkFromWavelength(mSampleWavelength);
            int gainRef = mDeviceManager.getGainFromBaselineRef((int) mSampleWavelength);
            int i0Ref = mDeviceManager.getDarkRefFromWavelength(mSampleWavelength);

            int I0 = (I1Ref - mDarkRef[gainRef - 1]) * (i0 - mDark[gain - 1]) / (i0Ref - mDarkRef[gainRef - 1]);
            float trans = (float) (I1 - mDark[gain - 1]) / (float) I0;

            abs = (float) -Math.log10(trans);
            abs = Utils.getValidAbs(abs);
            if (mSampleWavelength == wavelength1) {
                mSampleAbs1 = abs;
            } else if (mSampleWavelength == wavelength2) {
                mSampleAbs2 = abs;
            } else if (mSampleWavelength == wavelength3) {
                mSampleAbs3 = abs;
            }
            if (mQuantitativeAnalysisFragment.isLastWavelength(mSampleWavelength)) {
                Log.d(TAG, "do qa sample done!");
                mSampleWavelength = 0;
                dismissDialog();
                mDeviceManager.setLoopThreadRestart();
                Log.d(TAG, "Update abs = " + abs);
                float updateAbs = mSampleAbs1 * ratio1 + mSampleAbs2 * ratio2 + mSampleAbs3 * ratio3;
                BusProvider.getInstance().post(new QaUpdateEvent(updateAbs));
                mDeviceManager.setLoopThreadRestart();
            }
        }
        //done
    }

    float mQaWavelength = 0;

    private void work_entry_quantitative_analysis_rezero(String[] msgs) {
        String tag = msgs[0];

        if (tag.startsWith(DeviceManager.TAG_SET_WAVELENGTH)) {
            String wl = msgs[0].substring(3);
            wl = wl.replaceAll(" ", "").replaceAll("\r", "").replaceAll("\n", "").trim();
            Log.d(TAG, "rezero wl = " + wl);
            mQaWavelength = Float.parseFloat(wl);
        } else if (tag.startsWith(DeviceManager.TAG_REZERO)) {
            if (mQaWavelength == 0) {
                return;
            }
            msgs[1] = msgs[1].replaceAll(" ", "").replaceAll("\r", "").replaceAll("\n", "").trim();
            msgs[2] = msgs[2].replaceAll(" ", "").replaceAll("\r", "").replaceAll("\n", "").trim();
            msgs[3] = msgs[3].replaceAll(" ", "").replaceAll("\r", "").replaceAll("\n", "").trim();
            msgs[4] = msgs[4].replaceAll(" ", "").replaceAll("\r", "").replaceAll("\n", "").trim();
            mI0 = Integer.parseInt(msgs[1]);
            mA = Integer.parseInt(msgs[2]);
            mI0Ref = Integer.parseInt(msgs[3]);
            mARef = Integer.parseInt(msgs[4]);

            int I0 = Integer.parseInt(msgs[1]);
            mDeviceManager.setDark(mQaWavelength, I0);
            int gain = Integer.parseInt(msgs[2]);
            mDeviceManager.setGain((int) mQaWavelength, gain);
            int I0Ref = Integer.parseInt(msgs[3]);
            mDeviceManager.setDarkRef(mQaWavelength, I0Ref);
            int gainRef = Integer.parseInt(msgs[4]);
            mDeviceManager.setGainRef((int) mQaWavelength, gainRef);

            if (mQuantitativeAnalysisFragment.isLastWavelength(mQaWavelength)) {
                Log.d(TAG, "do qa rezero done!");
                mQaWavelength = 0;
                dismissDialog();
                mDeviceManager.setLoopThreadRestart();
            }
        }
    }

    private void work_entry_single_command(String[] msgs) {
        String[] msg = msgs.clone();
        String tag = msg[0];
        Log.d(TAG, "single command -> " + tag);

        //restart main loop
        mDeviceManager.setLoopThreadRestart();

        if (tag.startsWith(DeviceManager.TAG_RESET_DARK)) {
            dismissDialog();
            //get dark
            for (int i = 0; i < 8; i++) {
                msg[i + 1] = msg[i + 1].replaceAll("\\D+", "").replaceAll("\r", "").replaceAll("\n", "").trim();
                mDark[i] = Integer.parseInt(msg[i + 1]);
            }
            for (int i = 8; i < 16; i++) {
                msg[i + 1] = msg[i + 1].replaceAll("\\D+", "").replaceAll("\r", "").replaceAll("\n", "").trim();
                mDarkRef[i - 8] = Integer.parseInt(msg[i + 1]);
            }
        } else if (tag.startsWith(DeviceManager.TAG_SET_A)) {
            dismissDialog();
        }
    }

    private void work_entry_set_wavelength(String[] msgs) {
        if (msgs[0].startsWith(DeviceManager.TAG_SET_WAVELENGTH)) {
//            String wl = msgs[0].substring(3);
//            wl = wl.replaceAll("\\D+", "").replaceAll("\r", "").replaceAll("\n", "").trim();
//            float w = Float.parseFloat(wl);
            dismissDialog();
            mDeviceManager.setLoopThreadRestart();
            mDeviceManager.doSingleCommand(DeviceManager.DEVICE_CMD_LIST_SET_A, mA);
        }
    }

    private void work_entry_set_lamp_wavelength(String[] msgs) {
        if (msgs[0].startsWith(DeviceManager.TAG_SET_LAMP_WAVELENGTH)) {
            //TODO: need to do something?
            mDeviceManager.setLoopThreadRestart();
        }
    }

    private void work_entry_rezero(String[] msgs) {
        if (msgs[0].startsWith(DeviceManager.TAG_SET_WAVELENGTH)) {
            Log.d(TAG, "##dismiss dialog");
            dismissDialog();
//            mDeviceManager.setLoopThreadRestart();
        } else if (msgs[0].startsWith(DeviceManager.TAG_REZERO)) {
            msgs[1] = msgs[1].replaceAll(" ", "").replaceAll("\r", "").replaceAll("\n", "").trim();
            msgs[2] = msgs[2].replaceAll(" ", "").replaceAll("\r", "").replaceAll("\n", "").trim();
            msgs[3] = msgs[3].replaceAll(" ", "").replaceAll("\r", "").replaceAll("\n", "").trim();
            msgs[4] = msgs[4].replaceAll(" ", "").replaceAll("\r", "").replaceAll("\n", "").trim();
            mI0 = Integer.parseInt(msgs[1]);
            mA = Integer.parseInt(msgs[2]);
            mI0Ref = Integer.parseInt(msgs[3]);
            mARef = Integer.parseInt(msgs[4]);
            mDeviceManager.setLoopThreadRestart();
        }
    }

    @Override
    public void onWavelengthInputComplete(String wavelength) {
        if (wavelength.length() > 0) {
            float wl = Float.parseFloat(wavelength);
            if (Utils.checkWavelengthInvalid(this, wl)) {
                mDeviceManager.setWavelengthWork(wl);
                loadSetWavelengthDialog();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        String input = "74ODANEALACA85B5AE";
//
//        String enc = Utils.encode(KEY, input);
//        String dec = Utils.encode(KEY, enc);
//        Log.d(TAG, "##enc = " + enc);
//        Log.d(TAG, "##dec = " + dec);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate");
        initToolbar();
        initView();
        mIsInitialized = false;

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            toastShow(getString(R.string.ble_not_support));
            finish();
        }
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        mIsBluetoothConnected = false;
        mWavelengthDialog = new WavelengthDialog();
        mWavelengthDialog.setListener(this);

        mPeakDialog = new SettingEditDialog();
        mPeakDialog.init(-1, getString(R.string.peak_setting), getString(R.string.peak_distance), new SettingEditDialog.SettingInputListern() {
            @Override
            public void onSettingInputComplete(int index, String distance) {
                if (distance.length() < 1) {
                    toastShow(getString(R.string.notice_edit_null));
                } else {
                    //save distance
                    DeviceApplication.getInstance().getSpUtils().setPeakDistance(Float.parseFloat(distance));
                }
            }
        });

        mDeviceSelectDialog = new DevicesSelectDialog();
        mDeviceSelectDialog.setDialog(mWaitDialog);
        mDeviceSelectDialog.setContext(this);

        mDeviceCheckDialog = new DeviceCheckDialog();

        mBaselineDialog = new BaselineDialog();
        mBaselineDialog.setListener(new BaselineOperateListener() {
            @Override
            public void onStart() {
                baselineInit();
                mDeviceManager.baselineWork((int) DeviceManager.BASELINE_END, DeviceManager.GAIN_MAX, DeviceManager.GAIN_MAX);
            }

            @Override
            public void onStop() {
                DeviceManager.getInstance().stopWork();
                stopBaseline = true;
            }
        });

        mLightMgrDialog = new LightMgrDialog();

        mDeviceManager = DeviceManager.getInstance();
        mDeviceManager.init(this, mUiHandler);

        checkPermissions();
        mDark = new int[8];
        mDarkRef = new int[8];
        //check the directory is exist?
        //if not exist, create the directory
        Utils.checkDirectory();
        if (Utils.isFake()) {
            Log.d(TAG, "##isFake!!");
            return;
        }
        //check license
        String address = DeviceApplication.getInstance().getSpUtils().getMacAddress();
        Log.d(TAG, "##Check License: " + address);
        if (address.length() == 17) {
            mWaitDialog.setMessage(getString(R.string.attempt_connecting_device));
            mWaitDialog.show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                        mDeviceManager.scan();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {
            addRegistCode();
        }
        View view = View.inflate(getApplicationContext(), R.layout.dialog_text, null);
        mWarmAlertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.warm)
                .setView(view)
                .setPositiveButton(R.string.ok_string, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDeviceManager.skip();
                        if (!mIsInitialized) {
                            initDialog();
                            mDeviceManager.initializeWork();
                        }
                        mSkipWarm = true;
                    }
                })
                .setCancelable(false).create();
    }

    private static final int RC_ROOT = 102;

    @AfterPermissionGranted(RC_ROOT)
    public void checkPermissions() {
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION};
        boolean[] allows = new boolean[permissions.length];
        for (int i = 0; i < permissions.length; i++) {
            if (EasyPermissions.hasPermissions(this, permissions[i])) {
                allows[i] = true;
            } else {
                allows[i] = false;
            }
        }
        boolean result = true;
        for (int i = 0; i < permissions.length; i++) {
            result &= allows[i];
        }

        if (!result) {
            EasyPermissions.requestPermissions(this, "Root", RC_ROOT, permissions[0], permissions[1], permissions[2]);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Main onDestroy!");
        if (mDeviceManager != null) {
            mDeviceManager.stopWork();
            Log.d(TAG, "send quit");
            mDeviceManager.doSingleCommand(DeviceManager.DEVICE_CMD_LIST_SET_QUIT);
        }
        try {
            //delay some time for quit cmd done~
            //TODO: ???
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (mDeviceManager != null) {
            mDeviceManager.release();
        }
        mDeviceManager = null;
    }

    @Override
    public void onBackPressed() {

        Log.d(TAG, "operateMode = " + mOperateMode + ", needToSave = " + Utils.needToSave);
        if (mOperateMode) {
            BusProvider.getInstance().post(new SetOperateModeEvent(false));
            setOperateMode(false);
            return;
        }

        if (Utils.needToSave) {
            BusProvider.getInstance().post(new FileOperateEvent(FileOperateEvent.OP_EVENT_SAVE));
            return;
        }

        int backStackCount = getFragmentManager().getBackStackEntryCount();
        Log.d(TAG, "back stack count = " + backStackCount);
        // the main fragment is always on the bottom stack
        if (backStackCount > 1) {
            setTitle(getString(R.string.main));
            super.onBackPressed();
            return;
        }

        if (!isExit) {
            isExit = true;
            toastShow(getString(R.string.retry_to_exit));
            new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    isExit = false;
                }
            }.sendEmptyMessageDelayed(0, 2000);
        } else {
            if (mDeviceManager != null) {
                mDeviceManager.stopWork();
                Log.d(TAG, "send quit");
                mDeviceManager.doSingleCommand(DeviceManager.DEVICE_CMD_LIST_SET_QUIT);
            }
            try {
                //delay some time for quit cmd done~
                //TODO: ???
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (mDeviceManager != null) {
                mDeviceManager.release();
            }
            mDeviceManager = null;
            finish();
            System.exit(0);
        }
    }

    public boolean getOperateMode() {
        return mOperateMode;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        setBluetoothConnected(mIsBluetoothConnected);

        return true;
    }

    @Override
    public void onMainClick(int id) {

//        if (!checkConnected()) {
//            return;
//        }

        switch (id + 100) {
            case MainFragment.ITEM_PHOTOMETRIC_MEASURE:
                addContentFragment(mPhotometricFragment);
                break;
            case MainFragment.ITEM_TIME_SCAN:
                addContentFragment(mTimeScanFragment);
                break;
            case MainFragment.ITEM_WAVELENGTH_SCAN:
                addContentFragment(mWavelengthScanFragment);
                break;
            case MainFragment.ITEM_QUANTITATIVE_ANALYSIS:
                addContentFragment(mQuantitativeAnalysisFragment);
                break;
            case MainFragment.ITEM_MULTI_WAVELENGTH:
                addContentFragment(mMultipleWavelengthFragment);
                break;
            case MainFragment.ITEM_SYSTEM_SETTING:
                showSystemSettingDialog();
                break;
            case MainFragment.ITEM_ABOUT:
                mStatusToolbar.setVisibility(View.GONE);
                addContentFragment(mAboutFragment);
                break;
            case MainFragment.ITEM_DNA:
                addContentFragment(mDnaFragment);
                break;
            default:
                break;
        }
        setTitle(getResources().getStringArray(R.array.titles)[id]);
    }

    @Subscribe
    public void onAboutExitEvent(AboutExitEvent event) {
        mStatusToolbar.setVisibility(View.VISIBLE);
    }

    @Override
    public void loadFile(int id, int fileIndex) {
        switch (id + 100) {
            case MainFragment.ITEM_PHOTOMETRIC_MEASURE:
                mPhotometricFragment.prepareLoadFile(fileIndex);
                addContentFragment(mPhotometricFragment);
                break;
            case MainFragment.ITEM_TIME_SCAN:
                mTimeScanFragment.prepareLoadFile(fileIndex);
                addContentFragment(mTimeScanFragment);
                break;
            case MainFragment.ITEM_WAVELENGTH_SCAN:
                mWavelengthScanFragment.prepareLoadFile(fileIndex);
                addContentFragment(mWavelengthScanFragment);
                break;
            case MainFragment.ITEM_QUANTITATIVE_ANALYSIS:
                mQuantitativeAnalysisFragment.prepareLoadFile(fileIndex);
                addContentFragment(mQuantitativeAnalysisFragment);
                break;
            case MainFragment.ITEM_MULTI_WAVELENGTH:
                mMultipleWavelengthFragment.prepareLoadFile(fileIndex);
                addContentFragment(mMultipleWavelengthFragment);
                break;
            case MainFragment.ITEM_DNA:
                mDnaFragment.prepareLoadFile(fileIndex);
                addContentFragment(mDnaFragment);
                break;

            default:
                break;
        }
        setTitle(getResources().getStringArray(R.array.titles)[id]);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_setting:
                BusProvider.getInstance().post(new SettingEvent(0));
                break;
            case R.id.action_open:
//                toastShow("open");
                BusProvider.getInstance().post(new FileOperateEvent(FileOperateEvent.OP_EVENT_OPEN));
                break;
            case R.id.action_save:
//                toastShow("save");
                BusProvider.getInstance().post(new FileOperateEvent(FileOperateEvent.OP_EVENT_SAVE));
                break;
            case R.id.action_file_export:
                BusProvider.getInstance().post(new FileOperateEvent(FileOperateEvent.OP_EVENT_FILE_EXPORT));
                break;
            case R.id.action_set_wavelength:
                if (!checkConnected()) {
                    return super.onOptionsItemSelected(item);
                }
                mWavelengthDialog.show(getFragmentManager(), getString(R.string.wavelength));
                break;
            case R.id.action_rezero:
                BusProvider.getInstance().post(new FileOperateEvent(FileOperateEvent.OP_EVENT_REZERO));
                break;
            case R.id.action_start_test:
                BusProvider.getInstance().post(new FileOperateEvent(FileOperateEvent.OP_EVENT_START_TEST));
                break;
            case R.id.action_dark_current:
                mDeviceManager.doSingleCommand(DeviceManager.DEVICE_CMD_LIST_SET_DARK);
                loadResetDarkDialog();
                break;
            case R.id.action_peak_distance:
                mPeakDialog.show(getFragmentManager(), "peak");
                break;
            case R.id.action_baseline:
                if (!checkConnected()) {
                    return super.onOptionsItemSelected(item);
                }
                //check if baseline is available
                List<String> saveFileList = DeviceApplication.getInstance().getBaselineDb().getTables();

                if (saveFileList.size() == 0) {
                    toastShow(getString(R.string.notice_null_baseline_available));
                    mBaselineDialog.setLoadFileId(-1);
                    mBaselineDialog.show(getFragmentManager(), "baseline");
                } else {
                    Utils.showItemSelectDialog(this, getString(R.string.action_open)
                            , saveFileList.toArray(new String[saveFileList.size()]), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //load the baseline
                                    mBaselineDialog.setLoadFileId(which);
                                    mBaselineDialog.show(getFragmentManager(), "baseline");
                                }
                            });
                }
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initToolbar() {
        mTopToolbar = (Toolbar) findViewById(R.id.tb_top);
        mStatusToolbar = (Toolbar) findViewById(R.id.tb_status);
        setSupportActionBar(mTopToolbar);

        mTopToolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        mTopToolbar.setTitleTextColor(getResources().getColor(R.color.colorTitle));
        mTopToolbar.setTitle(getResources().getString(R.string.bluetooth_disconnected));
        mTopToolbar.setNavigationIcon(R.mipmap.bluetooth_disabled);

        mTopToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "on Navigation Click!");
                if (!mIsBluetoothConnected) {
                    //check license
                    String address = DeviceApplication.getInstance().getSpUtils().getMacAddress();
                    Log.d(TAG, "##Check License: " + address);
                    if (address.length() == 17) {
                        mWaitDialog.setMessage(getString(R.string.attempt_connecting_device));
                        mWaitDialog.show();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(1000);
                                    mDeviceManager.scan();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    } else {
                        addRegistCode();
                    }
                } else {
                    //alert dialog to disconnect current connection
//                    Utils.showAlertDialog(MainActivity.this, getString(R.string.notice), getString(R.string.sure_to_disconnect),
//                            new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    BtleManager.getInstance().disconnect();
//                                }
//                            });
                }
            }
        });

    }

    private void setOperateMode(boolean enable) {
        if (enable) {
            mTopToolbar.setBackgroundColor(getResources().getColor(R.color.colorOperate));
            mSelectall.setVisibility(View.VISIBLE);
            mDelete.setVisibility(View.VISIBLE);
        } else {
            mTopToolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            mSelectall.setVisibility(View.GONE);
            mDelete.setVisibility(View.GONE);
        }
        mOperateMode = enable;
    }

    private void initView() {
        mBottomWavelength = (TextView) findViewById(R.id.tv_bottom_wavelength);
        mBottomAbs = (TextView) findViewById(R.id.tv_bottom_abs);
        mBottomTrans = (TextView) findViewById(R.id.tv_bottom_trans);
        mTitleTextView = (TextView) findViewById(R.id.tb_title);
        mSelectall = (LinearLayout) findViewById(R.id.layout_selectall);
        mDelete = (LinearLayout) findViewById(R.id.layout_delete);
        mSelectall.setVisibility(View.GONE);
        mDelete.setVisibility(View.GONE);
        mSelectall.setOnClickListener(this);
        mDelete.setOnClickListener(this);

        mToast = Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT);
        mMain = new MainFragment();
        mPhotometricFragment = new PhotometricMeasureFragment();
        mTimeScanFragment = new TimeScanFragment();
        mWavelengthScanFragment = new WavelengthScanFragment();
        mQuantitativeAnalysisFragment = new QuantitativeAnalysisFragment();
        mMultipleWavelengthFragment = new MultipleWavelengthFragment();
        mAboutFragment = new AboutFragment();
        mDnaFragment = new DnaFragment();
        mHelloChart = new HelloChartFragment();

        mWaitDialog = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
        mWaitDialog.setCancelable(false);
        mWaitDialog.setButton(getString(R.string.cancel_string), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "CANCEL DIALOG!!");
                BusProvider.getInstance().post(new CancelEvent());
            }
        });

        if (getFragmentManager().getBackStackEntryCount() == 0) {
            addContentFragment(mMain);
        } else {
            Log.w(TAG, "Main fragment is add!!!");
        }

    }

    private void addContentFragment(Fragment fragment) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        Log.d(TAG, "fragment count = " + fm.getBackStackEntryCount());
        Log.d(TAG, "addContentFragment");

        if (fragment != mMain) {
            transaction.setCustomAnimations(
                    R.animator.fragment_slide_left_enter,
                    R.animator.fragment_slide_right_exit);
        }

        transaction.add(R.id.layout_content, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void setBluetoothConnected(boolean enable) {
        if (enable) {
            mTopToolbar.setTitle(getResources().getString(R.string.bluetooth_connected));
            mTopToolbar.setNavigationIcon(R.mipmap.bluetooth_connected);
        } else {
            mTopToolbar.setTitle(getResources().getString(R.string.bluetooth_disconnected));
            mTopToolbar.setNavigationIcon(R.mipmap.bluetooth_disabled);
        }
    }

    private void setTitle(String title) {
        if (mTitleTextView != null) {
            mTitleTextView.setText(title);
        }
    }

    private void toastShow(String msg) {
        if (mToast != null) {
            mToast.setText(msg);
            mToast.show();
        }
    }

    private void updateAbs(float abs) {
        String absString = getString(R.string.abs) + ": " + Utils.formatAbs(abs);
        mBottomAbs.setText(absString);
    }

    private void updateTrans(float trans) {
        String transString = getString(R.string.trans) + ": " + Utils.formatTrans(trans) + "%";
        mBottomTrans.setText(transString);
    }

    private void updateWavelength(float wavelength) {
        mWavelength = wavelength;
        String wavelengthString = getString(R.string.wavelength) + ": " + String.format("%.1f", wavelength);
        mBottomWavelength.setText(wavelengthString);
    }

    public void loadSetWavelengthDialog() {
        final Runnable callback = new Runnable() {
            @Override
            public void run() {
                mWaitDialog.dismiss();
                toastShow(getString(R.string.timeout_set_wavelength));
            }
        };
        if (!mWaitDialog.isShowing()) {
            mWaitDialog.setMessage(getString(R.string.set_wavelength_message));
            mWaitDialog.show();
            mHandler.postDelayed(callback, WAVELENGTH_TIMEOUT);
            mWaitDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    mHandler.removeCallbacks(callback);
                }
            });
        }
    }

    private void loadDataprocessDialog() {
        final Runnable callback = new Runnable() {
            @Override
            public void run() {
                mWaitDialog.dismiss();
                toastShow(getString(R.string.timeout_process));
            }
        };
        if (!mWaitDialog.isShowing()) {
            mWaitDialog.setMessage(getString(R.string.data_processing_message));
            mWaitDialog.show();
            mHandler.postDelayed(callback, PROCESS_TIMEOUT);
            mWaitDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    mHandler.removeCallbacks(callback);
                }
            });
        }
    }

    private void loadResetDarkDialog() {
        if (!mWaitDialog.isShowing()) {
            mWaitDialog.setMessage(getString(R.string.reset_dark));
            mWaitDialog.show();
        }
    }

    private void loadBaselineDialog() {
        if (!mWaitDialog.isShowing()) {
            mWaitDialog.setMessage(getString(R.string.baseline_message));
            mWaitDialog.show();
        }
    }

    private void updateBaseline(float wavelength, int gain, int gainRef, int energy, int enertyRef) {
        Log.d(TAG, "update wavelength = " + wavelength + ", energy = " + energy + ", gain = " + mBaselineGain + ", ref gain = " + mBaselineGainRef);
        mBaselineDialog.addItem(wavelength, gain, energy, gainRef, enertyRef);
        if (mWaitDialog.isShowing()) {
            mWaitDialog.setMessage(getString(R.string.baseline_message) + "\n"
                    + getString(R.string.wavelength) + ": " + wavelength + " " + getString(R.string.nm)
                    + "  " + getString(R.string.gain) + ": " + gain);
//            mWaitDialog.show();
        }
    }

    private Handler mInitDialogHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                initDialog();
            } else {

                mWaitDialog.setMessage(getString(R.string.attempt_connecting_device));
                mWaitDialog.show();
            }
        }
    };

    private void initDialog() {
        final Runnable mCallback = new Runnable() {
            @Override
            public void run() {
                if (mWaitDialog.isShowing()) {
                    mWaitDialog.dismiss();
                    if (!mIsInitialized) {
                        //timeout
//                    BtleManager.getInstance().disconnect();
//                    Toast.makeText(MainActivity.this, getString(R.string.connect_timeout), Toast.LENGTH_SHORT).show();
                        mDeviceManager.initializeWork();
//                        initDialog();
                        mInitDialogHandle.sendEmptyMessage(0);
                    }
                }
            }
        };
        if (mWaitDialog != null && (!mWaitDialog.isShowing())) {
            mInitDialogHandle.sendEmptyMessage(1);
            mHandler.postDelayed(mCallback, 10000);
            mWaitDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    mHandler.removeCallbacks(mCallback);
                }
            });
        }
    }

    public void doRezeroDialog() {
        if (!mWaitDialog.isShowing()) {
            mWaitDialog.setMessage(getString(R.string.rezero_message));
            mWaitDialog.show();
        }
    }

    public void doTestDialog() {
        if (!mWaitDialog.isShowing()) {
            mWaitDialog.setMessage(getString(R.string.test_message));
            mWaitDialog.show();
        }
    }

    private void dismissDialog() {
        if (mWaitDialog.isShowing()) {
            mWaitDialog.dismiss();
        }
    }

    private boolean checkConnected() {
        if (!mIsInitialized) {
            toastShow(getString(R.string.notice_not_init));
        }
        return mIsInitialized;
    }

    public void loadWavelengthDialog(float wavelength) {
        if (checkConnected()) {
            mDeviceManager.setWavelengthWork((int) (wavelength));
            loadSetWavelengthDialog();
        }
    }

    public void showSystemSettingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.system_setting));
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setItems(R.array.system_settings, this);
        builder.create().show();
    }

    public final static int ACC_HIGH = 0;
    public final static int ACC_LOW = 1;

    private void showAcurrencyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.acurrency_setting));
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setItems(R.array.acurrencys, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == ACC_HIGH) {
                    DeviceApplication.getInstance().getSpUtils().setKeyAcc(ACC_HIGH);
                } else if (which == ACC_LOW) {
                    DeviceApplication.getInstance().getSpUtils().setKeyAcc(ACC_LOW);
                }
            }
        });
        builder.create().show();
    }

    @Subscribe
    public void onWaitProgressEvent(WaitProgressEvent event) {
        if (event.start) {
            loadDataprocessDialog();
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    dismissDialog();
                }
            }, 200);
        }
    }

    @Subscribe
    public void onSetWavelengthEvent(SetWavelengthEvent event) {
        mWavelengthDialog.show(getFragmentManager(), getString(R.string.wavelength));
    }

    @Subscribe
    public void onSetOperateModeEvent(SetOperateModeEvent event) {
        setOperateMode(event.isOperateMode);
    }

    @Subscribe
    public void onWavelengthScanCancelEvent(WavelengthScanCancelEvent event) {
        mWavelengthScanRezero = false;
        mDeviceManager.stopWork();
    }

    @Subscribe
    public void onDozeroEvent(RezeroEvent event) {
        doRezeroDialog();
        mWavelengthScanRezero = true;
        mDeviceManager.dorezeroWork(event.start, event.end, event.interval);
    }

    @Subscribe
    public void onMultipleWavelengthEvent(MultipleWavelengthCallbackEvent event) {
        if (event.event_type == MultipleWavelengthCallbackEvent.EVENT_TYPE_DO_REZERO) {
            doRezeroDialog();
            mDeviceManager.doMultipleWavelengthRezero(event.wavelengths);
        } else if (event.event_type == MultipleWavelengthCallbackEvent.EVENT_TYPE_DO_TEST) {
            doTestDialog();
            mDeviceManager.doMultipleWavelengthTest(event.wavelengths);
        }
    }

    @Subscribe
    public void onDnaEvent(DnaCallbackEvent event) {
        if (event.event_type == DnaCallbackEvent.EVENT_TYPE_DO_REZERO) {
            doRezeroDialog();
            mDeviceManager.doDnaRezero(event.wl1, event.wl2, event.wlRef);
        } else if (event.event_type == DnaCallbackEvent.EVENT_TYPE_DO_TEST) {
            doTestDialog();
            mDeviceManager.doDnaTest(event.wl1, event.wl2, event.wlRef);
        }
    }

//    @Subscribe void onLoadWavelegthDialogEvnet(LoadWavelengthDialogEvent event) {
//        Log.d(TAG, "####get wavelegth = " + event.wavelength);
//        mDeviceManager.setWavelengthWork((int)(event.wavelength));
//        loadSetWavelengthDialog();
//    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.layout_selectall) {
            BusProvider.getInstance().post(new SetOperateEvent(SetOperateEvent.OP_MODE_SELECTALL));
        } else if (v.getId() == R.id.layout_delete) {
            BusProvider.getInstance().post(new SetOperateEvent(SetOperateEvent.OP_MODE_DELETE));
        }
    }

    /*
    * */
    private final int SYSTEM_SETTING_ITEM_WAVELENGTH_ADJUST = 0;
    private final int SYSTEM_SETTING_ITEM_DARK_CURRENT_ADJUST = 1;
    private final int SYSTEM_SETTING_ITEM_LIGHT_MANAGERMENT = 2;
    private final int SYSTEM_SETTING_SYSTEM_BASELINE = 3;
    private final int SYSTEM_SETTING_PEAK_DISTANCE_SETTING = 4;
    private final int SYSTEM_SETTING_ITEM_ACURRENCY = 5;
    private final int SYSTEM_SETTING_ADD_REGIST_CODE = 6;
    private final int SYSTEM_SETTING_FILE_MANAGERMENT = 7;
    private final int SYSTEM_SETTING_ITEM_FACTORY_RESET = 8;
    private final int SYSTEM_SETTING_ITEM_SYSTEM_VERSION = 9;

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case SYSTEM_SETTING_ITEM_WAVELENGTH_ADJUST:
//                mWavelengthDialog.show(getFragmentManager(), getString(R.string.wavelength));
                mDeviceManager.doSingleCommand(DeviceManager.DEVICE_CMD_LIST_ADJUST_WL);
                break;
            case SYSTEM_SETTING_ITEM_DARK_CURRENT_ADJUST:
                mDeviceManager.doSingleCommand(DeviceManager.DEVICE_CMD_LIST_SET_DARK);
                loadResetDarkDialog();
                break;
            case SYSTEM_SETTING_ITEM_LIGHT_MANAGERMENT:
                mLightMgrDialog.show(getFragmentManager(), "light_mgr");
                break;
            case SYSTEM_SETTING_SYSTEM_BASELINE:
                if (!checkConnected()) {
                    return;
                }
                //check if baseline is available
                List<String> saveFileList = DeviceApplication.getInstance().getBaselineDb().getTables();

                if (saveFileList.size() == 0) {
                    toastShow(getString(R.string.notice_null_baseline_available));
                    mBaselineDialog.setLoadFileId(-1);
                    mBaselineDialog.show(getFragmentManager(), "baseline");
                } else {
                    Utils.showItemSelectDialog(this, getString(R.string.action_open)
                            , saveFileList.toArray(new String[saveFileList.size()]), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //load the baseline
                                    mBaselineDialog.setLoadFileId(which);
                                    mBaselineDialog.show(getFragmentManager(), "baseline");
                                }
                            });
                }
                break;
            case SYSTEM_SETTING_PEAK_DISTANCE_SETTING:
                mPeakDialog.show(getFragmentManager(), "peak");
                break;
            case SYSTEM_SETTING_ITEM_ACURRENCY:
                showAcurrencyDialog();
                break;
            case SYSTEM_SETTING_ADD_REGIST_CODE:
                addRegistCode();
                break;
            case SYSTEM_SETTING_FILE_MANAGERMENT:
                fileManagerment();
                break;
            case SYSTEM_SETTING_ITEM_FACTORY_RESET:
                break;
            case SYSTEM_SETTING_ITEM_SYSTEM_VERSION:
                Toast.makeText(this, getString(R.string.version_string), Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }
    }


    private void fileManagerment() {
        String[] items = getResources().getStringArray(R.array.open_titles);
        Utils.showItemSelectDialog(this, getString(R.string.file_manager)
                , items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, final int which) {
                        switch (which + 100) {
                            case MainFragment.ITEM_PHOTOMETRIC_MEASURE:
                                final List<String> saveFileList0 = DeviceApplication.getInstance().getPhotometricMeasureDb().getTables();
                                final boolean[] selected0 = new boolean[saveFileList0.size()];
                                for (int i = 0; i < selected0.length; i++) {
                                    selected0[i] = false;
                                }
                                Utils.showMultipleSelectDialog(MainActivity.this, getResources().getStringArray(R.array.open_titles)[0]
                                        , saveFileList0.toArray(new String[saveFileList0.size()]),
                                        new DialogInterface.OnMultiChoiceClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                                selected0[which] = isChecked;
                                            }
                                        },
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int id) {

                                                Utils.showAlertDialog(MainActivity.this, getString(R.string.notice),
                                                        getString(R.string.sure_to_delete), new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                for (int i = 0; i < saveFileList0.size(); i++) {
                                                                    if (selected0[i]) {
                                                                        Log.d(TAG, "delete -> " + saveFileList0.get(i));
                                                                        DeviceApplication.getInstance().getPhotometricMeasureDb().delRecord(saveFileList0.get(i));
                                                                    }
                                                                }
                                                            }
                                                        });
                                            }
                                        });
                                break;
                            case MainFragment.ITEM_QUANTITATIVE_ANALYSIS:
                                final List<String> saveFileList1 = DeviceApplication.getInstance().getQuantitativeAnalysisDb().getTables();
                                final boolean[] selected1 = new boolean[saveFileList1.size()];
                                for (int i = 0; i < selected1.length; i++) {
                                    selected1[i] = false;
                                }
                                Utils.showMultipleSelectDialog(MainActivity.this, getResources().getStringArray(R.array.open_titles)[0]
                                        , saveFileList1.toArray(new String[saveFileList1.size()]),
                                        new DialogInterface.OnMultiChoiceClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                                selected1[which] = isChecked;
                                            }
                                        },
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int id) {
                                                Utils.showAlertDialog(MainActivity.this, getString(R.string.notice),
                                                        getString(R.string.sure_to_delete), new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                for (int i = 0; i < saveFileList1.size(); i++) {
                                                                    if (selected1[i]) {
                                                                        Log.d(TAG, "delete -> " + saveFileList1.get(i));
                                                                        DeviceApplication.getInstance().getQuantitativeAnalysisDb().delRecord(saveFileList1.get(i));
                                                                    }
                                                                }
                                                            }
                                                        });

                                            }
                                        });
                                break;
                            case MainFragment.ITEM_WAVELENGTH_SCAN:
                                final List<String> saveFileList2 = DeviceApplication.getInstance().getWavelengthScanDb().getTables();
                                final boolean[] selected2 = new boolean[saveFileList2.size()];
                                for (int i = 0; i < selected2.length; i++) {
                                    selected2[i] = false;
                                }
                                Utils.showMultipleSelectDialog(MainActivity.this, getResources().getStringArray(R.array.open_titles)[0]
                                        , saveFileList2.toArray(new String[saveFileList2.size()]),
                                        new DialogInterface.OnMultiChoiceClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                                selected2[which] = isChecked;
                                            }
                                        },
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int id) {
                                                Utils.showAlertDialog(MainActivity.this, getString(R.string.notice),
                                                        getString(R.string.sure_to_delete), new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                for (int i = 0; i < saveFileList2.size(); i++) {
                                                                    if (selected2[i]) {
                                                                        Log.d(TAG, "delete -> " + saveFileList2.get(i));
                                                                        DeviceApplication.getInstance().getWavelengthScanDb().delRecord(saveFileList2.get(i));
                                                                    }
                                                                }
                                                            }
                                                        });

                                            }
                                        });
                                break;
                            case MainFragment.ITEM_TIME_SCAN:
                                final List<String> saveFileList3 = DeviceApplication.getInstance().getTimeScanDb().getTables();
                                final boolean[] selected3 = new boolean[saveFileList3.size()];
                                for (int i = 0; i < selected3.length; i++) {
                                    selected3[i] = false;
                                }
                                Utils.showMultipleSelectDialog(MainActivity.this, getResources().getStringArray(R.array.open_titles)[0]
                                        , saveFileList3.toArray(new String[saveFileList3.size()]),
                                        new DialogInterface.OnMultiChoiceClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                                selected3[which] = isChecked;
                                            }
                                        },
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int id) {
                                                Utils.showAlertDialog(MainActivity.this, getString(R.string.notice),
                                                        getString(R.string.sure_to_delete), new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                for (int i = 0; i < saveFileList3.size(); i++) {
                                                                    if (selected3[i]) {
                                                                        Log.d(TAG, "delete -> " + saveFileList3.get(i));
                                                                        DeviceApplication.getInstance().getTimeScanDb().delRecord(saveFileList3.get(i));
                                                                    }
                                                                }
                                                            }
                                                        });

                                            }
                                        });
                                break;
                        }
                    }
                });
    }

    private void checkLicense(String inputLicense) {
        if (inputLicense.charAt(2) == 'O' ||
                inputLicense.charAt(5) == 'N' ||
                inputLicense.charAt(8) == 'L' ||
                inputLicense.charAt(11) == 'A' ||
                inputLicense.charAt(14) == 'B' ||
                inputLicense.charAt(17) == 'E') {
            String macAddress = inputLicense.substring(0, 2) + ":" +
                    inputLicense.substring(3, 5) + ":" +
                    inputLicense.substring(6, 8) + ":" +
                    inputLicense.substring(9, 11) + ":" +
                    inputLicense.substring(12, 14) + ":" +
                    inputLicense.substring(15, 17);
            Log.d(TAG, "MAC ADDRESS: " + macAddress);
            DeviceApplication.getInstance().getSpUtils().setKeyMacAddress(macAddress);
        }
    }

    public String KEY = "0123456789ABCDEFGH";

    private void addRegistCode() {
//        TextView registCode;
        final EditText license;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_regist_code, null);
//        registCode = (TextView) view.findViewById(R.id.tv_regist_code);
        license = (EditText) view.findViewById(R.id.dialog_et_license);
        builder.setView(view).setPositiveButton(getString(R.string.ok_string),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String inputLicense = license.getEditableText().toString();
                        if (inputLicense.length() != 18) {
                            toastShow(getString(R.string.notice_invalid_license));
                            return;
                        }
                        //parse password
                        inputLicense = Utils.decode(KEY, inputLicense);
                        checkLicense(inputLicense);
                    }
                }).setNegativeButton(getString(R.string.cancel_string), null)
                .setTitle(R.string.regist_code_title).setIcon(R.mipmap.ic_launcher);

        builder.create().show();
    }
}
