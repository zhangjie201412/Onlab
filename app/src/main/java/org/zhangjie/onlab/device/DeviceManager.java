package org.zhangjie.onlab.device;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import org.zhangjie.onlab.ble.BtleListener;
import org.zhangjie.onlab.ble.BtleManager;
import org.zhangjie.onlab.device.work.WorkTask;

/**
 * Created by Administrator on 2016/5/26.
 */
public class DeviceManager implements BtleListener {

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

    private Context mContext;
    private Handler mUiHandler = null;
    private WorkTask mWorkThread;
    private PhotometricMeasureWork mPhotometricMeasureWork;

    @Override
    public void onDeviceConnected() {
        mUiHandler.obtainMessage(UI_MSG_DEVICE_CONNECTED).sendToTarget();
//        Toast.makeText(mContext, "Connected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeviceDisconnected() {
        mUiHandler.obtainMessage(UI_MSG_DEVICE_DISCONNECTED).sendToTarget();
//        Toast.makeText(mContext, "Disconnected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDataAvailable(byte[] data) {

    }

    public void init(Context context, Handler handler) {
        BtleManager.getInstance().init(context);
        BtleManager.getInstance().register(this);
        mContext = context;
        mUiHandler = handler;
        mPhotometricMeasureWork = new PhotometricMeasureWork();
    }

    public void release() {
        BtleManager.getInstance().unregister();
        BtleManager.getInstance().release();
    }

    public void scan() {
        BtleManager.getInstance().scan(true);
    }

    public void doPhotometricMeasureWork() {
        mWorkThread = new WorkTask(mPhotometricMeasureWork);
        mWorkThread.start();
    }
}
