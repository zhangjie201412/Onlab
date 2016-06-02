package org.zhangjie.onlab.device;

import android.util.Log;

import org.zhangjie.onlab.device.work.BaseWork;

import java.util.HashMap;
import java.util.List;

/**
 * Created by H151136 on 5/27/2016.
 */
public class DeviceWork extends BaseWork {

    private static final String TAG = "Onlab.Device";
    private List<HashMap<String, Cmd>> mCmdList;

    public void setCmdList(List<HashMap<String, Cmd>> cmdList) {
        mCmdList = cmdList;
    }

    @Override
    public void setup() throws InterruptedException {
        super.setup();
        Log.d(TAG, "setup");
        DeviceManager.getInstance().setLoopThreadPause();
    }

    @Override
    public void process() throws InterruptedException {
        super.process();
        Log.d(TAG, "process");
        int count = mCmdList.size();
        for(int i = 0; i < count; i++) {
            DeviceManager.getInstance().sendCmd(mCmdList.get(i).get("cmd"));
        }
    }

    @Override
    public void cleanup() throws InterruptedException {
        super.cleanup();
        Log.d(TAG, "cleanup");
        DeviceManager.getInstance().setLoopThreadRestart();
    }
}
