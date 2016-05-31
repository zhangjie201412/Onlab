package org.zhangjie.onlab.device;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import org.zhangjie.onlab.ble.BtleListener;
import org.zhangjie.onlab.ble.BtleManager;
import org.zhangjie.onlab.device.work.WorkTask;

import java.io.ByteArrayInputStream;
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
    public static final int DEVICE_CMD_LIST_END = 0x100C;
    public static String[] CMD_LIST;
    //----cmd list

    private Context mContext;
    private Handler mUiHandler = null;
    private WorkTask mWorkThread;
    private DeviceWorkd mWork;
    private final int BUF_SIZE = 128;
    private byte[] buffer;
    private int position;
    private int last_flag_pos;
    private int flag_pos;

    private void initCmdList() {
        CMD_LIST = new String[DEVICE_CMD_LIST_END - DEVICE_CMD_LIST_START];
        CMD_LIST[DEVICE_CMD_LIST_CONNECT - DEVICE_CMD_LIST_START] = "connect";
        CMD_LIST[DEVICE_CMD_LIST_GET_STATUS - DEVICE_CMD_LIST_START] = "getstatus";
        CMD_LIST[DEVICE_CMD_LIST_GET_ENERGY - DEVICE_CMD_LIST_START] = "ge";
        CMD_LIST[DEVICE_CMD_LIST_GET_WAVELENGTH - DEVICE_CMD_LIST_START] = "getwl";
        CMD_LIST[DEVICE_CMD_LIST_GET_DARK - DEVICE_CMD_LIST_START] = "getdark";
        CMD_LIST[DEVICE_CMD_LIST_GET_A - DEVICE_CMD_LIST_START] = "ga";
        CMD_LIST[DEVICE_CMD_LIST_REZERO - DEVICE_CMD_LIST_START] = "rezero";
        CMD_LIST[DEVICE_CMD_LIST_SET_DARK - DEVICE_CMD_LIST_START] = "resetdark";
        CMD_LIST[DEVICE_CMD_LIST_SET_WAVELENGTH - DEVICE_CMD_LIST_START] = "swl";
        CMD_LIST[DEVICE_CMD_LIST_SET_A - DEVICE_CMD_LIST_START] = "sa";
        CMD_LIST[DEVICE_CMD_LIST_SET_QUIT - DEVICE_CMD_LIST_START] = "quit";
    }

    @Override
    public void onDeviceConnected() {
        mUiHandler.obtainMessage(UI_MSG_DEVICE_CONNECTED).sendToTarget();
//        Toast.makeText(mContext, "Connected", Toast.LENGTH_SHORT).show();
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
//        Toast.makeText(mContext, "Disconnected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public synchronized void onDataAvailable(byte[] data) {
//        for (int i = 0; i < data.length; i++) {
//            Log.d(TAG, String.format("[%d] = %02x, %c\n", i, data[i], data[i]));
//        }
        String[] recvMsg;
        if (handlerBuffer(data)) {
            this.notify();
            recvMsg = process();
            for (int i = 0; i < recvMsg.length; i++) {
                Log.d(TAG, String.format("[%d] = %s\n", i, recvMsg[i]));
            }
        } else {
            return;
        }


        if ((mEntryFlag & WORK_ENTRY_FLAG_INITIALIZE) != 0) {
            //initialzation ertry
            Log.d(TAG, "INITIALZE ENTRY");
        }
        if ((mEntryFlag & WORK_ENTRY_FLAG_UPDATE_STATUS) != 0) {
            //update status entry
            Log.d(TAG, "UPDATE STATUS ENTRY");
        }
    }

    private boolean handlerBuffer(byte[] data) {
        boolean retVal = false;
        for (int i = 0; i < data.length; i++) {
            buffer[position++] = data[i];
            if (position == BUF_SIZE) {
                position = 0;
            }
            //get '>'
            if (data[i] == 0x3e) {
                flag_pos = position - 1;
                //process data
                retVal = true;
            }
        }

        return retVal;
    }

    private synchronized String[] process() {
        int validBufLength;

        Log.d(TAG, "process: last_flag_pos = " + last_flag_pos + ", flag_pos = " + flag_pos);

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
        last_flag_pos = flag_pos;
        String validString = new String(validBuf);
        Log.d(TAG, "VALID BUF = " + validString);

        return validString.split("\n");
    }

    public void init(Context context, Handler handler) {
        BtleManager.getInstance().init(context);
        BtleManager.getInstance().register(this);
        mContext = context;
        mUiHandler = handler;
        mWork = new DeviceWorkd();
        initCmdList();
        buffer = new byte[BUF_SIZE];
        position = 0;
        last_flag_pos = 0;
        flag_pos = 0;
    }

    public void release() {
        BtleManager.getInstance().unregister();
        BtleManager.getInstance().release();
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

        if(!isFake) {
            try {
                //wait '>'
                this.wait();
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
        mWork.setCmdList(cmdList);
        mWorkThread = new WorkTask();
        mWorkThread.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, mWork);
    }

    /*Bit switch if the work entry need to process
    * */
    private final int WORK_ENTRY_FLAG_INITIALIZE = 1 << 0;
    private final int WORK_ENTRY_FLAG_UPDATE_STATUS = 1 << 1;

    private int mEntryFlag = 0x00000000;

    /*1. Connect
      2. get dark
      3. get wavelength
      4. get A
    * */
    public void initializeWork() {
        //clear entry flag
        mEntryFlag &= 0x00000000;
        //set init flag
        mEntryFlag |= WORK_ENTRY_FLAG_INITIALIZE;
        List<HashMap<String, Cmd>> cmdList = new ArrayList<HashMap<String, Cmd>>();
        clearCmd(cmdList);
        addCmd(cmdList, DEVICE_CMD_LIST_CONNECT, -1);
        addCmd(cmdList, DEVICE_CMD_LIST_GET_DARK, -1);
        addCmd(cmdList, DEVICE_CMD_LIST_GET_WAVELENGTH, -1);
        addCmd(cmdList, DEVICE_CMD_LIST_GET_A, -1);
        doWork(cmdList);
    }

    /*send ge and update bottom status bar
     */
    public void updateStatus() {
        //clear entry flag
        mEntryFlag &= 0x00000000;
        //set update status flag
        mEntryFlag |= WORK_ENTRY_FLAG_UPDATE_STATUS;
        List<HashMap<String, Cmd>> cmdList = new ArrayList<HashMap<String, Cmd>>();
        clearCmd(cmdList);
        addCmd(cmdList, DEVICE_CMD_LIST_GET_ENERGY, 10);
        doWork(cmdList);
    }
}
