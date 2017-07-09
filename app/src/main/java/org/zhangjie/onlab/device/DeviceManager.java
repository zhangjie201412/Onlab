package org.zhangjie.onlab.device;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.zhangjie.onlab.DeviceApplication;
import org.zhangjie.onlab.ble.BtleListener;
import org.zhangjie.onlab.ble.BtleManager;
import org.zhangjie.onlab.device.work.WorkTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2016/5/26.
 */
public class DeviceManager implements BtleListener {

    private boolean isFake = false;
    private static final String TAG = "Onlab.DeviceManager";
    private static DeviceManager instance = null;

    static {
        instance = new DeviceManager();
    }

    private DeviceManager() {
    }

    public static DeviceManager getInstance() {
        return instance;
    }

    public static final int UI_MSG_DEVICE_CONNECTED = 0;
    public static final int UI_MSG_DEVICE_DISCONNECTED = 1;
    public static final int UI_MSG_DEVICE_SCAN = 2;
    public static final int UI_MSG_DEVICE_DATA = 3;

    //++++cmd list
    public static final int DEVICE_CMD_LIST_START = 0x1001;
    public static final int DEVICE_CMD_LIST_CONNECT = 0x1001;
    public static final int DEVICE_CMD_LIST_GET_STATUS = 0x1002;
    public static final int DEVICE_CMD_LIST_GET_ENERGY = 0x1003;
    public static final int DEVICE_CMD_LIST_GET_WAVELENGTH = 0x1004;
    public static final int DEVICE_CMD_LIST_GET_DARK = 0x1005;
    public static final int DEVICE_CMD_LIST_GET_A = 0x1006;
    public static final int DEVICE_CMD_LIST_REZERO = 0x1007;
    public static final int DEVICE_CMD_LIST_SET_DARK = 0x1008;
    public static final int DEVICE_CMD_LIST_SET_WAVELENGTH = 0x1009;
    public static final int DEVICE_CMD_LIST_SET_A = 0x100A;
    public static final int DEVICE_CMD_LIST_SET_QUIT = 0x100B;
    public static final int DEVICE_CMD_LIST_SET_LAMP_WAVELENGTH = 0x100C;
    public static final int DEVICE_CMD_LIST_GET_LAMP_WAVELENGTH = 0x100D;
    public static final int DEVICE_CMD_LIST_SET_D2ON = 0x100E;
    public static final int DEVICE_CMD_LIST_SET_D2OFF = 0x100F;
    public static final int DEVICE_CMD_LIST_SET_WUON = 0x1010;
    public static final int DEVICE_CMD_LIST_SET_WUOFF = 0x1011;
    public static final int DEVICE_CMD_LIST_SET_LAMP = 0x1012;
    public static final int DEVICE_CMD_LIST_SET_FILTER = 0x1013;
    public static final int DEVICE_CMD_LIST_ADJUST_WL = 0x1014;
    public static final int DEVICE_CMD_LIST_GET_R = 0x1015;
    public static final int DEVICE_CMD_LIST_SET_R = 0x1016;
    public static final int DEVICE_CMD_LIST_END = 0x1017;
    public static String[] CMD_LIST;
    //----cmd list

    public static final String TAG_CONNECT = "connect";
    public static final String TAG_GET_STATUS = "getstatus";
    public static final String TAG_GET_WAVELENGTH = "getwl";
    public static final String TAG_GET_DARK = "getdark2";
    public static final String TAG_GET_A = "ga";
    public static final String TAG_GET_R = "gr";
    public static final String TAG_GET_ENERGY = "ge2";
    public static final String TAG_SET_WAVELENGTH = "swl";
    public static final String TAG_SET_A = "sa";
    public static final String TAG_SET_R = "sr";
    public static final String TAG_REZERO = "rezero2";
    public static final String TAG_CHECK_LAMP_START = "lamp start";
    public static final String TAG_CHECK_LAMP_DONE = "lamp ok";
    public static final String TAG_CHECK_AD_START = "ad start";
    public static final String TAG_CHECK_AD_DONE = "ad ok";
    public static final String TAG_CHECK_DEUTERIUM_START = "deuterium start";
    public static final String TAG_CHECK_DEUTERIUM_DONE = "deuterium ok";
    public static final String TAG_CHECK_TUNGSTEN_START = "tungsten start";
    public static final String TAG_CHECK_TUNGSTEN_OK = "tungsten ok";
    public static final String TAG_CHECK_WAVE_START = "wave staart";
    public static final String TAG_CHECK_WAVE_DONE = "wave ok";
    public static final String TAG_CHECK_PARA_START = "para start";
    public static final String TAG_CHECK_PARA_DONE = "para ok";
    public static final String TAG_CHECK_DARK_START = "dark start";
    public static final String TAG_CHECK_DARK_DONE = "dark ok";
    public static final String TAG_WARM = "warm";
    public static final String TAG_READY = "ready";
    public static final String TAG_ONLINE = "online";
    public static final String TAG_GET_LAMP_WAVELENGTH = "getlampwl";
    public static final String TAG_SET_LAMP_WAVELENGTH = "setlampwl";
    public static final String TAG_RESET_DARK = "resetdar2";

    public static final float BASELINE_END = 1100;//1100;
    public static final float BASELINE_START = 190;//190;
    public static int[] mBaseline;
    public static int[] mBaselineRef;
    public static int[] mI0;
    public static int[] mI0Ref;
    public static int ENERGY_FIT_UP = 40000;
    public static int ENERGY_FIT_DOWN = 20000;
    public static final int GAIN_MAX = 8;

    private Context mContext;
    private Handler mUiHandler = null;
    private WorkTask mWorkThread;
    private DeviceWork mWork;
    private final int BUF_SIZE = 2048;
    private byte[] buffer;
    private int position;
    private int last_flag_pos;
    private int flag_pos;
    private boolean mIsConnected = false;
    private UpdateThread mUpdateThread;

    private void initCmdList() {
        CMD_LIST = new String[DEVICE_CMD_LIST_END - DEVICE_CMD_LIST_START];
        CMD_LIST[DEVICE_CMD_LIST_CONNECT - DEVICE_CMD_LIST_START] = "connect";
        CMD_LIST[DEVICE_CMD_LIST_GET_STATUS - DEVICE_CMD_LIST_START] = "getstatus";
        CMD_LIST[DEVICE_CMD_LIST_GET_ENERGY - DEVICE_CMD_LIST_START] = TAG_GET_ENERGY;
        CMD_LIST[DEVICE_CMD_LIST_GET_WAVELENGTH - DEVICE_CMD_LIST_START] = "getwl";
        CMD_LIST[DEVICE_CMD_LIST_GET_DARK - DEVICE_CMD_LIST_START] = TAG_GET_DARK;
        CMD_LIST[DEVICE_CMD_LIST_GET_A - DEVICE_CMD_LIST_START] = TAG_GET_A;
        CMD_LIST[DEVICE_CMD_LIST_GET_R - DEVICE_CMD_LIST_START] = TAG_GET_R;
        CMD_LIST[DEVICE_CMD_LIST_REZERO - DEVICE_CMD_LIST_START] = TAG_REZERO;
        CMD_LIST[DEVICE_CMD_LIST_SET_DARK - DEVICE_CMD_LIST_START] = TAG_RESET_DARK;
        CMD_LIST[DEVICE_CMD_LIST_SET_WAVELENGTH - DEVICE_CMD_LIST_START] = "swl";
        CMD_LIST[DEVICE_CMD_LIST_SET_A - DEVICE_CMD_LIST_START] = TAG_SET_A;
        CMD_LIST[DEVICE_CMD_LIST_SET_R - DEVICE_CMD_LIST_START] = TAG_SET_R;
        CMD_LIST[DEVICE_CMD_LIST_SET_QUIT - DEVICE_CMD_LIST_START] = "quit";
        CMD_LIST[DEVICE_CMD_LIST_SET_LAMP_WAVELENGTH - DEVICE_CMD_LIST_START] = "setlampwl";
        CMD_LIST[DEVICE_CMD_LIST_GET_LAMP_WAVELENGTH - DEVICE_CMD_LIST_START] = "getlampwl";
        CMD_LIST[DEVICE_CMD_LIST_SET_D2ON - DEVICE_CMD_LIST_START] = "d2on";
        CMD_LIST[DEVICE_CMD_LIST_SET_D2OFF - DEVICE_CMD_LIST_START] = "d2off";
        CMD_LIST[DEVICE_CMD_LIST_SET_WUON - DEVICE_CMD_LIST_START] = "wuon";
        CMD_LIST[DEVICE_CMD_LIST_SET_WUOFF - DEVICE_CMD_LIST_START] = "wuoff";
        CMD_LIST[DEVICE_CMD_LIST_SET_LAMP - DEVICE_CMD_LIST_START] = "setlamp";
        CMD_LIST[DEVICE_CMD_LIST_SET_FILTER - DEVICE_CMD_LIST_START] = "setfilter";
        CMD_LIST[DEVICE_CMD_LIST_ADJUST_WL - DEVICE_CMD_LIST_START] = "adjustwl";
    }

    @Override
    public void onDeviceConnected() {
        mUiHandler.obtainMessage(UI_MSG_DEVICE_CONNECTED).sendToTarget();
//        Toast.makeText(mContext, "Connected", Toast.LENGTH_SHORT).show();
        mIsConnected = true;
    }

    @Override
    public void onDeviceScan(String name, String addr) {
        Message msg = mUiHandler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putString("name", name);
        bundle.putString("addr", addr);
        msg.what = UI_MSG_DEVICE_SCAN;
        msg.setData(bundle);
        mUiHandler.sendMessage(msg);
    }

    @Override
    public void onDeviceDisconnected() {
        mUiHandler.obtainMessage(UI_MSG_DEVICE_DISCONNECTED).sendToTarget();
        mIsConnected = false;
    }

    @Override
    public void onDataAvailable(byte[] data) {
        String[] recvMsg;
        if (handlerBuffer(data)) {
            recvMsg = process();
            for (int i = 0; i < recvMsg.length; i++) {
                //Log.v(TAG, String.format("[%d] = %s\n", i, recvMsg[i]));
            }
        } else {
            return;
        }

        Message msg = mUiHandler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putStringArray("MSG", recvMsg);
        msg.setData(bundle);
        msg.what = UI_MSG_DEVICE_DATA;
        msg.arg1 = mEntryFlag;
        mUiHandler.sendMessage(msg);
        Log.v(TAG, "TASK DONE!");
    }

    private synchronized boolean handlerBuffer(byte[] data) {
        boolean retVal = false;
        for (int i = 0; i < data.length; i++) {
            buffer[position++] = data[i];
            if (position == BUF_SIZE) {
                position = 0;
            }
            //get '>'
            if (data[i] == 0x3e) {
                if (position != 0) {
                    flag_pos = position - 1;
                } else {
                    flag_pos = BUF_SIZE - 1;
                }
                //process data
                retVal = true;
                this.notify();
            }
        }

        return retVal;
    }

    private synchronized String[] process() {
        int validBufLength;

        if (flag_pos > last_flag_pos) {
            validBufLength = flag_pos - last_flag_pos;
        } else {
            validBufLength = BUF_SIZE - last_flag_pos + flag_pos;
        }
        byte[] validBuf = new byte[validBufLength];
        if (flag_pos > last_flag_pos) {
            for (int i = last_flag_pos; i < flag_pos; i++) {
                validBuf[i - last_flag_pos] = buffer[i];
            }
        } else {
            for (int i = last_flag_pos; i < BUF_SIZE; i++) {
                validBuf[i - last_flag_pos] = buffer[i];
            }
            for (int i = 0; i < flag_pos; i++) {
                validBuf[BUF_SIZE - last_flag_pos + i] = buffer[i];
            }
        }
        flag_pos += 1;
        if (flag_pos == BUF_SIZE) {
            flag_pos = 0;
        }
        last_flag_pos = flag_pos;
        String validString = new String(validBuf);
        Log.v(TAG, "VALID BUF = " + validString);

        return validString.split("\n");
    }

    public void init(Context context, Handler handler) {
        BtleManager.getInstance().init(context);
        BtleManager.getInstance().register(this);
        mContext = context;
        mUiHandler = handler;
        initCmdList();
        buffer = new byte[BUF_SIZE];
        position = 0;
        last_flag_pos = 0;
        flag_pos = 0;
        mIsConnected = false;
        mUpdateThread = new UpdateThread();
        mBaseline = new int[(int) (BASELINE_END - BASELINE_START + 1)];
        mBaselineRef = new int[(int) (BASELINE_END - BASELINE_START + 1)];
        mI0 = new int[(int) (BASELINE_END - BASELINE_START + 1) * 10];
        mI0Ref = new int[(int) (BASELINE_END - BASELINE_START + 1) * 10];

        if (DeviceApplication.getInstance().getSpUtils().getBaselineAvailable()) {
            mBaseline = DeviceApplication.getInstance().getSpUtils().getBaseline((int) (BASELINE_END - BASELINE_START + 1));
            mBaselineRef = DeviceApplication.getInstance().getSpUtils().getBaselineRef((int) (BASELINE_END - BASELINE_START + 1));
        }
    }

    public int getGainFromBaseline(int wavelength) {
//        Log.d(TAG, "get " + wavelength);
        return mBaseline[wavelength - (int) BASELINE_START];
    }

    public void setGain(int wavelength, int gain) {
        Log.d(TAG, "baselineWork: set " + (wavelength - (int) BASELINE_START) + " = " + gain);
        mBaseline[wavelength - (int) BASELINE_START] = gain;
    }

    public int getGainFromBaselineRef(int wavelength) {
//        Log.d(TAG, "get " + wavelength);
        return mBaselineRef[wavelength - (int) BASELINE_START];
    }

    public void setGainRef(int wavelength, int gain) {
        Log.d(TAG, "baselineWork: set " + (wavelength - (int) BASELINE_START) + " = " + gain);
        mBaselineRef[wavelength - (int) BASELINE_START] = gain;
    }

    public int getDarkFromWavelength(float wavelength) {
        return mI0[(int) ((wavelength - BASELINE_START) * 10)];
    }

    public void setDark(float wavelength, int dark) {
        mI0[(int) ((wavelength - BASELINE_START) * 10)] = dark;
    }

    public int getDarkRefFromWavelength(float wavelength) {
        return mI0Ref[(int) ((wavelength - BASELINE_START) * 10)];
    }

    public void setDarkRef(float wavelength, int dark) {
        mI0Ref[(int) ((wavelength - BASELINE_START) * 10)] = dark;
    }

    public void start() {
        if (!mUpdateThread.isAlive()) {
            mUpdateThread.start();
        }
    }

    public synchronized void release() {
        setLoopThreadPause();
        mEntryFlag = 0x00000000;
        this.notifyAll();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        BtleManager.getInstance().send("quit\r");
        BtleManager.getInstance().unregister();
        BtleManager.getInstance().release();
        mIsConnected = false;
        this.notifyAll();
    }

    public void scan() {
        BtleManager.getInstance().scan(true);
    }

    private void clearCmd(List<HashMap<String, Cmd>> cmdList) {
        cmdList.clear();
    }

    private void addCmd(List<HashMap<String, Cmd>> cmdList, int cmd, int param) {
        Cmd c = new Cmd(cmd, param);
        HashMap<String, Cmd> item = new HashMap<>();
        item.put("cmd", c);
        cmdList.add(item);
    }

    private void addCmd(List<HashMap<String, Cmd>> cmdList, int cmd, float param) {
        Cmd c = new Cmd(cmd, param);
        HashMap<String, Cmd> item = new HashMap<>();
        item.put("cmd", c);
        cmdList.add(item);
    }

    public synchronized int[] sendCmd(Cmd cmd) {
        if (cmd.param < 0) {
            if (!isFake) {
                BtleManager.getInstance().send(CMD_LIST[cmd.cmd - DEVICE_CMD_LIST_START] + "\r");
            } else {
                Log.d(TAG, "fake send -> " + CMD_LIST[cmd.cmd - DEVICE_CMD_LIST_START]);
            }
        } else {
            if (!isFake) {
                BtleManager.getInstance().send(CMD_LIST[cmd.cmd - DEVICE_CMD_LIST_START] + " " + cmd.param + "\r");
            } else {
                Log.d(TAG, "fake send -> " + CMD_LIST[cmd.cmd - DEVICE_CMD_LIST_START] + " " + cmd.param);
            }
        }

        if (!isFake) {
            try {
                //wait '>'
                //Log.v(TAG, "wait");
                this.wait();
                //Log.v(TAG, "wait done");

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "Wait notify~");
        }

        if (isFake) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void doWork(List<HashMap<String, Cmd>> cmdList) {
        mWork = null;
        mWork = new DeviceWork();
        mWork.setCmdList(cmdList);
        mWorkThread = new WorkTask();
        mWorkThread.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, mWork);
    }

    public void stopWork() {
        if (mWork != null) {
            mWork.setStop();
        }
        if (mWorkThread != null && (mWorkThread.getStatus() != AsyncTask.Status.FINISHED)) {
            mWorkThread.cancel(true);
        }
    }

    /*Bit switch if the work entry need to process
    * */
    public static final int WORK_ENTRY_FLAG_INITIALIZE = 1 << 0;
    public static final int WORK_ENTRY_FLAG_UPDATE_STATUS = 1 << 1;
    public static final int WORK_ENTRY_FLAG_SET_WAVELENGTH = 1 << 2;
    public static final int WORK_ENTRY_FLAG_REZERO = 1 << 3;
    public static final int WORK_ENTRY_FLAG_PHOTOMETRIC_MEASURE = 1 << 4;
    public static final int WORK_ENTRY_FLAG_TIME_SCAN = 1 << 5;
    public static final int WORK_ENTRY_FLAG_BASELINE = 1 << 6;
    public static final int WORK_ENTRY_FLAG_DOREZERO = 1 << 7;
    public static final int WORK_ENTRY_FLAG_WAVELENGTH_SCAN = 1 << 8;
    public static final int WORK_ENTRY_FLAG_MULTIPLE_WAVELENGTH_REZERO = 1 << 9;
    public static final int WORK_ENTRY_FLAG_MULTIPLE_WAVELENGTH_TEST = 1 << 10;
    public static final int WORK_ENTRY_FLAG_QUANTITATIVE_ANALYSIS = 1 << 11;
    public static final int WORK_ENTRY_FLAG_GET_STATUS = 1 << 12;
    public static final int WORK_ENTRY_FLAG_SET_LAMP_WAVELENGTH = 1 << 13;
    public static final int WORK_ENTRY_FLAG_DNA_REZERO = 1 << 14;
    public static final int WORK_ENTRY_FLAG_DNA_TEST = 1 << 15;
    public static final int WORK_ENTRY_FLAG_QUANTITATIVE_ANALYSIS_REZERO = 1 << 16;
    public static final int WORK_ENTRY_FLAG_QUANTITATIVE_ANALYSIS_SAMPLE = 1 << 17;
    public static final int WORK_ENTRY_FLAG_SINGLE_COMMAND = 1 << 31;

    private int mEntryFlag = 0x00000000;

    //do getstatus
    //do self check
    public synchronized void getStatus() {
        Log.d(TAG, "do get status");
        //clear entry flag
        mEntryFlag &= 0x00000000;
        //set init flag
        mEntryFlag |= WORK_ENTRY_FLAG_GET_STATUS;
        Log.d(TAG, "GET STATUS FLAG = " + mEntryFlag);
        List<HashMap<String, Cmd>> cmdList = new ArrayList<HashMap<String, Cmd>>();
        clearCmd(cmdList);
        addCmd(cmdList, DEVICE_CMD_LIST_GET_STATUS, -1);
        doWork(cmdList);
    }

    /*1. Connect
      2. get dark
      3. get wavelength
      4. get A
    * */
    public synchronized void initializeWork() {
        Log.d(TAG, "do initializeWork");
        //clear entry flag
        mEntryFlag &= 0x00000000;
        //set init flag
        mEntryFlag |= WORK_ENTRY_FLAG_INITIALIZE;
        Log.d(TAG, "INIT WORK FLAG = " + mEntryFlag);
        List<HashMap<String, Cmd>> cmdList = new ArrayList<HashMap<String, Cmd>>();
        clearCmd(cmdList);
//        addCmd(cmdList, DEVICE_CMD_LIST_SET_QUIT, -1);
        addCmd(cmdList, DEVICE_CMD_LIST_CONNECT, -1);
        addCmd(cmdList, DEVICE_CMD_LIST_GET_DARK, -1);
        addCmd(cmdList, DEVICE_CMD_LIST_GET_WAVELENGTH, -1);
        addCmd(cmdList, DEVICE_CMD_LIST_GET_A, -1);
        addCmd(cmdList, DEVICE_CMD_LIST_GET_LAMP_WAVELENGTH, -1);
        doWork(cmdList);
    }

    //send rezero \r
    public synchronized void skip() {
//        mEntryFlag = 0x00000000;
//        //set rezero flag
//        mEntryFlag |= WORK_ENTRY_FLAG_REZERO;
        Log.d(TAG, "SKIP!!");
        List<HashMap<String, Cmd>> cmdList = new ArrayList<HashMap<String, Cmd>>();
        clearCmd(cmdList);
        addCmd(cmdList, DEVICE_CMD_LIST_SET_QUIT, -1);
        doWork(cmdList);
    }

    //send rezero \r
    public synchronized void rezeroWork() {
        setLoopThreadPause();
        mEntryFlag = 0x00000000;
        //set rezero flag
        mEntryFlag |= WORK_ENTRY_FLAG_REZERO;
        Log.d(TAG, "REZERO WORK FLAG = " + mEntryFlag);
        List<HashMap<String, Cmd>> cmdList = new ArrayList<HashMap<String, Cmd>>();
        clearCmd(cmdList);
        addCmd(cmdList, DEVICE_CMD_LIST_REZERO, -1);
        doWork(cmdList);
    }

    public synchronized void rezeroWork(float wl) {
        setLoopThreadPause();
        mEntryFlag = 0x00000000;
        //set rezero flag
        mEntryFlag |= WORK_ENTRY_FLAG_REZERO;
        Log.d(TAG, "REZERO WORK FLAG = " + mEntryFlag);
        List<HashMap<String, Cmd>> cmdList = new ArrayList<HashMap<String, Cmd>>();
        clearCmd(cmdList);
        addCmd(cmdList, DEVICE_CMD_LIST_SET_WAVELENGTH, wl);
        addCmd(cmdList, DEVICE_CMD_LIST_REZERO, -1);
        doWork(cmdList);
    }

    public synchronized void setWavelengthWork(float wavelength) {
        setLoopThreadPause();
        mEntryFlag = 0x00000000;
        //set update status flag
        mEntryFlag |= WORK_ENTRY_FLAG_SET_WAVELENGTH;
        Log.d(TAG, "SETWL WORK FLAG = " + mEntryFlag);
        List<HashMap<String, Cmd>> cmdList = new ArrayList<HashMap<String, Cmd>>();
        clearCmd(cmdList);
        addCmd(cmdList, DEVICE_CMD_LIST_SET_WAVELENGTH, wavelength);
        doWork(cmdList);
    }

    public synchronized void setLampWavelengthWork(float wavelength) {
        setLoopThreadPause();
        mEntryFlag = 0x00000000;
        //set update status flag
        mEntryFlag |= WORK_ENTRY_FLAG_SET_LAMP_WAVELENGTH;
        Log.d(TAG, "SETWL WORK FLAG = " + mEntryFlag);
        List<HashMap<String, Cmd>> cmdList = new ArrayList<HashMap<String, Cmd>>();
        clearCmd(cmdList);
        addCmd(cmdList, DEVICE_CMD_LIST_SET_LAMP_WAVELENGTH, wavelength);
        doWork(cmdList);
    }

    public synchronized void photometricMeasureWork() {
        setLoopThreadPause();
        //clear entry flag
        mEntryFlag = 0x00000000;
        //set update status flag
        mEntryFlag |= WORK_ENTRY_FLAG_PHOTOMETRIC_MEASURE;
        Log.d(TAG, "PM WORK FLAG = " + mEntryFlag);
        List<HashMap<String, Cmd>> cmdList = new ArrayList<HashMap<String, Cmd>>();
        clearCmd(cmdList);
        addCmd(cmdList, DEVICE_CMD_LIST_GET_ENERGY, 6);
        doWork(cmdList);
    }

    public synchronized void timeScanWork() {
        setLoopThreadPause();
        //clear entry flag
        mEntryFlag = 0x00000000;
        //set update status flag
        mEntryFlag |= WORK_ENTRY_FLAG_TIME_SCAN;
        Log.d(TAG, "TS WORK FLAG = " + mEntryFlag);
        List<HashMap<String, Cmd>> cmdList = new ArrayList<HashMap<String, Cmd>>();
        clearCmd(cmdList);
        addCmd(cmdList, DEVICE_CMD_LIST_GET_ENERGY, 6);
        doWork(cmdList);
    }

    public synchronized void baselineWork(int wavelength, int a, int r) {
        Log.d(TAG, "baselineWork: wavelength = " + wavelength);

        if (wavelength < BASELINE_START && wavelength > 0) {
            Log.d(TAG, "BASELINE DONE!");
            mEntryFlag = 0x00000000;
            setLoopThreadRestart();
            return;
        }

        boolean needWavelength = (wavelength > 0) ? true : false;
        boolean needGain = (a > 0) ? true : false;
        boolean needGainRef = (r > 0) ? true : false;
        //stop main loop
        setLoopThreadPause();
        //clear entry flag
        mEntryFlag = 0x00000000;
        //set baseline flag
        mEntryFlag |= WORK_ENTRY_FLAG_BASELINE;
        List<HashMap<String, Cmd>> cmdList = new ArrayList<HashMap<String, Cmd>>();
        clearCmd(cmdList);
        if (needWavelength) {
            addCmd(cmdList, DEVICE_CMD_LIST_SET_WAVELENGTH, wavelength);
            //fill gain array
//            if(wavelength < BASELINE_END) {
//                setGain(wavelength + 1, a);
//            }
        }
        if (needGain) {
            addCmd(cmdList, DEVICE_CMD_LIST_SET_A, a);
        }
        if (needGainRef) {
            addCmd(cmdList, DEVICE_CMD_LIST_SET_R, r);
        }
        addCmd(cmdList, DEVICE_CMD_LIST_GET_ENERGY, 1);
        doWork(cmdList);
    }

    public synchronized void dorezeroWork(float start, float end, float interval) {
        setLoopThreadPause();
        mEntryFlag = 0x00000000;
        mEntryFlag |= WORK_ENTRY_FLAG_DOREZERO;
        List<HashMap<String, Cmd>> cmdList = new ArrayList<HashMap<String, Cmd>>();
        clearCmd(cmdList);

        for (float wl = end; wl >= start; wl -= interval) {
            addCmd(cmdList, DEVICE_CMD_LIST_SET_WAVELENGTH, wl);
            addCmd(cmdList, DEVICE_CMD_LIST_SET_A, getGainFromBaseline((int) wl));
            addCmd(cmdList, DEVICE_CMD_LIST_SET_R, getGainFromBaselineRef((int) wl));
            addCmd(cmdList, DEVICE_CMD_LIST_GET_ENERGY, 1);
        }
        doWork(cmdList);
    }

    public synchronized void doWavelengthScan(float start, float end, float interval) {
        setLoopThreadPause();
        mEntryFlag = 0x00000000;
        mEntryFlag |= WORK_ENTRY_FLAG_WAVELENGTH_SCAN;
        List<HashMap<String, Cmd>> cmdList = new ArrayList<HashMap<String, Cmd>>();
        clearCmd(cmdList);

        for (float wl = end; wl >= start; wl -= interval) {
            addCmd(cmdList, DEVICE_CMD_LIST_SET_WAVELENGTH, wl);
            addCmd(cmdList, DEVICE_CMD_LIST_SET_A, getGainFromBaseline((int) wl));
            addCmd(cmdList, DEVICE_CMD_LIST_SET_R, getGainFromBaselineRef((int) wl));
            addCmd(cmdList, DEVICE_CMD_LIST_GET_ENERGY, 1);
        }
        doWork(cmdList);
    }

    public synchronized void doMultipleWavelengthRezero(float[] wavelengths) {
        setLoopThreadPause();
        mEntryFlag = 0x00000000;
        mEntryFlag |= WORK_ENTRY_FLAG_MULTIPLE_WAVELENGTH_REZERO;
        List<HashMap<String, Cmd>> cmdList = new ArrayList<HashMap<String, Cmd>>();
        clearCmd(cmdList);

        for (int i = 0; i < wavelengths.length; i++) {
            addCmd(cmdList, DEVICE_CMD_LIST_SET_WAVELENGTH, (int) wavelengths[i]);
            addCmd(cmdList, DEVICE_CMD_LIST_REZERO, -1);
        }

        doWork(cmdList);
    }

    public synchronized void doMultipleWavelengthTest(float[] wavelengths) {
        setLoopThreadPause();
        mEntryFlag = 0x00000000;
        mEntryFlag |= WORK_ENTRY_FLAG_MULTIPLE_WAVELENGTH_TEST;
        List<HashMap<String, Cmd>> cmdList = new ArrayList<HashMap<String, Cmd>>();
        clearCmd(cmdList);

        for (int i = 0; i < wavelengths.length; i++) {
            addCmd(cmdList, DEVICE_CMD_LIST_SET_WAVELENGTH, (int) wavelengths[i]);
            addCmd(cmdList, DEVICE_CMD_LIST_SET_A, getGainFromBaseline((int) wavelengths[i]));
            addCmd(cmdList, DEVICE_CMD_LIST_GET_ENERGY, 20);
        }
        doWork(cmdList);
    }

    public synchronized void doDnaRezero(float wl1, float wl2, float wlRef) {
        setLoopThreadPause();
        mEntryFlag = 0x00000000;
        mEntryFlag |= WORK_ENTRY_FLAG_DNA_REZERO;
        List<HashMap<String, Cmd>> cmdList = new ArrayList<HashMap<String, Cmd>>();
        clearCmd(cmdList);

        addCmd(cmdList, DEVICE_CMD_LIST_SET_WAVELENGTH, wl1);
        addCmd(cmdList, DEVICE_CMD_LIST_REZERO, -1);
        addCmd(cmdList, DEVICE_CMD_LIST_SET_WAVELENGTH, wl2);
        addCmd(cmdList, DEVICE_CMD_LIST_REZERO, -1);
        addCmd(cmdList, DEVICE_CMD_LIST_SET_WAVELENGTH, wlRef);
        addCmd(cmdList, DEVICE_CMD_LIST_REZERO, -1);
        doWork(cmdList);
    }

    public synchronized void doDnaTest(float wl1, float wl2, float wlRef) {
        setLoopThreadPause();
        mEntryFlag = 0x00000000;
        mEntryFlag |= WORK_ENTRY_FLAG_DNA_TEST;
        List<HashMap<String, Cmd>> cmdList = new ArrayList<HashMap<String, Cmd>>();
        clearCmd(cmdList);

        addCmd(cmdList, DEVICE_CMD_LIST_SET_WAVELENGTH, wl1);
        addCmd(cmdList, DEVICE_CMD_LIST_SET_A, getGainFromBaseline((int)wl1));
        addCmd(cmdList, DEVICE_CMD_LIST_GET_ENERGY, 1);
        addCmd(cmdList, DEVICE_CMD_LIST_SET_WAVELENGTH, wl2);
        addCmd(cmdList, DEVICE_CMD_LIST_SET_A, getGainFromBaseline((int)wl2));
        addCmd(cmdList, DEVICE_CMD_LIST_GET_ENERGY, 1);
        addCmd(cmdList, DEVICE_CMD_LIST_SET_WAVELENGTH, wlRef);
        addCmd(cmdList, DEVICE_CMD_LIST_SET_A, getGainFromBaseline((int)wlRef));
        addCmd(cmdList, DEVICE_CMD_LIST_GET_ENERGY, 1);
        doWork(cmdList);
    }
    public synchronized void doQuantitativeAnalysis() {
        setLoopThreadPause();
        mEntryFlag = 0x00000000;
        mEntryFlag |= WORK_ENTRY_FLAG_QUANTITATIVE_ANALYSIS;
        List<HashMap<String, Cmd>> cmdList = new ArrayList<HashMap<String, Cmd>>();
        clearCmd(cmdList);

        addCmd(cmdList, DEVICE_CMD_LIST_GET_ENERGY, 5);
        doWork(cmdList);
    }

    public synchronized void doQualtitativeAnalysisSample(float wl1, float wl2, float wl3) {
        setLoopThreadPause();
        mEntryFlag = 0x00000000;
        mEntryFlag |= WORK_ENTRY_FLAG_QUANTITATIVE_ANALYSIS_SAMPLE;
        List<HashMap<String, Cmd>> cmdList = new ArrayList<HashMap<String, Cmd>>();
        clearCmd(cmdList);

        addCmd(cmdList, DEVICE_CMD_LIST_SET_WAVELENGTH, wl1);
        addCmd(cmdList, DEVICE_CMD_LIST_GET_ENERGY, 5);
        if(wl2 > 0) {
            addCmd(cmdList, DEVICE_CMD_LIST_SET_WAVELENGTH, wl2);
            addCmd(cmdList, DEVICE_CMD_LIST_GET_ENERGY, 5);
        }
        if(wl3 > 0) {
            addCmd(cmdList, DEVICE_CMD_LIST_SET_WAVELENGTH, wl3);
            addCmd(cmdList, DEVICE_CMD_LIST_GET_ENERGY, 5);
        }
        doWork(cmdList);

    }

    public synchronized void doQuantitativeAnalysisRezero(float wl1, float wl2, float wl3) {
        setLoopThreadPause();
        mEntryFlag = 0x00000000;
        mEntryFlag |= WORK_ENTRY_FLAG_QUANTITATIVE_ANALYSIS_REZERO;
        List<HashMap<String, Cmd>> cmdList = new ArrayList<HashMap<String, Cmd>>();
        clearCmd(cmdList);

        addCmd(cmdList, DEVICE_CMD_LIST_SET_WAVELENGTH, wl1);
        addCmd(cmdList, DEVICE_CMD_LIST_REZERO, -1);
        if(wl2 > 0) {
            addCmd(cmdList, DEVICE_CMD_LIST_SET_WAVELENGTH, wl2);
            addCmd(cmdList, DEVICE_CMD_LIST_REZERO, -1);
        }
        if(wl3 > 0) {
            addCmd(cmdList, DEVICE_CMD_LIST_SET_WAVELENGTH, wl3);
            addCmd(cmdList, DEVICE_CMD_LIST_REZERO, -1);
        }
        doWork(cmdList);
    }

    public synchronized void doSingleCommand(int cmdType) {
        setLoopThreadPause();
        mEntryFlag = 0x00000000;
        mEntryFlag |= WORK_ENTRY_FLAG_SINGLE_COMMAND;
        List<HashMap<String, Cmd>> cmdList = new ArrayList<HashMap<String, Cmd>>();
        clearCmd(cmdList);

        addCmd(cmdList, cmdType, -1);
        doWork(cmdList);
    }

    public synchronized void doSingleCommand(int cmdType, int val) {
        setLoopThreadPause();
        mEntryFlag = 0x00000000;
        mEntryFlag |= WORK_ENTRY_FLAG_SINGLE_COMMAND;
        List<HashMap<String, Cmd>> cmdList = new ArrayList<HashMap<String, Cmd>>();
        clearCmd(cmdList);

        addCmd(cmdList, cmdType, val);
        doWork(cmdList);
    }

    /*send ge and update bottom status bar
     */
    private synchronized void updateStatus() {
        //clear entry flag
        mEntryFlag &= 0x00000000;
        //set update status flag
        mEntryFlag |= WORK_ENTRY_FLAG_UPDATE_STATUS;
        Log.v(TAG, "UPDATE WORK FLAG = " + mEntryFlag);
        List<HashMap<String, Cmd>> cmdList = new ArrayList<HashMap<String, Cmd>>();
        clearCmd(cmdList);
        addCmd(cmdList, DEVICE_CMD_LIST_GET_ENERGY, 5);
        addCmd(cmdList, DEVICE_CMD_LIST_GET_WAVELENGTH, -1);
        doWork(cmdList);
    }

    public synchronized void clearFlag(int mask) {
        mEntryFlag &= ~mask;
    }

    public void setLoopThreadPause() {
        mUpdateThread.pause();
    }

    public void setLoopThreadRestart() {
        mEntryFlag = 0x00000000;
        mEntryFlag |= WORK_ENTRY_FLAG_UPDATE_STATUS;
        mUpdateThread.restart();
    }

    public void saveBaseline() {
        DeviceApplication.getInstance().getSpUtils().saveBaseline(mBaseline);
        DeviceApplication.getInstance().getSpUtils().saveBaselineRef(mBaselineRef);
        DeviceApplication.getInstance().getSpUtils().setKeyBaselineAvailable(true);
    }

    class UpdateThread extends Thread {
        private boolean exit = false;
        private boolean pause = false;

        public void exit() {
            exit = true;
        }

        public UpdateThread() {
            exit = false;
        }

        public void pause() {
            pause = true;
        }

        public void restart() {
            pause = false;
        }

        @Override
        public void run() {
            super.run();
            while (!exit) {
                try {
                    Thread.sleep(500);
                    if (mIsConnected && !pause) {
                        Log.v(TAG, "update!");
                        Thread.sleep(1000);
                        if (mIsConnected && !pause) {
                            updateStatus();
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Log.d(TAG, "Exit loop thread");
        }
    }
}
