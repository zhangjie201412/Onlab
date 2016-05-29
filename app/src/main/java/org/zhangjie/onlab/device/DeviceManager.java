package org.zhangjie.onlab.device;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

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

    private List<HashMap<Integer, Cmd>> mCmdList;

    private Context mContext;
    private Handler mUiHandler = null;
    private WorkTask mWorkThread;
    private DeviceWorkd mWork;

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

        mCmdList = new ArrayList<HashMap<Integer, Cmd>>();
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
    public void onDataAvailable(byte[] data) {
        for(int i = 0; i < data.length; i++) {
            Log.d(TAG, String.format("[%d] = %02x\n", i, data[i]));
        }
    }

    public void init(Context context, Handler handler) {
        BtleManager.getInstance().init(context);
        BtleManager.getInstance().register(this);
        mContext = context;
        mUiHandler = handler;
        mWork = new DeviceWorkd();
        initCmdList();
    }

    public void release() {
        BtleManager.getInstance().unregister();
        BtleManager.getInstance().release();
    }

    public void scan() {
        BtleManager.getInstance().scan(true);
    }

    private int[] sendCmd(int cmd) {
        BtleManager.getInstance().send(CMD_LIST[cmd - DEVICE_CMD_LIST_START] + "\r");

        return null;
    }

    private void clearCmd() {
        mCmdList.clear();
    }

    private void addCmd(int cmd, int param) {
        Cmd c = new Cmd(cmd, param);
        HashMap<Integer, Cmd> item = new HashMap<>();
        item.put(mCmdList.size(), c);
        mCmdList.add(item);
    }

    private int[] sendCmd(int cmd, int param) {
        BtleManager.getInstance().send(CMD_LIST[cmd - DEVICE_CMD_LIST_START] + " " + param + "\r");

        return null;
    }

    public void doWork() {
        mWorkThread = new WorkTask(mWork);
    }
}
