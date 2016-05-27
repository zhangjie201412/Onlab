package org.zhangjie.onlab.device.work;

import android.util.Log;

/**
 * Created by H151136 on 5/27/2016.
 */
public class WorkTask extends Thread {
    private static final String TAG = "Onlab.WorkTask";
    private WorkTaskMethod mWorkMethod = null;

    public WorkTask(WorkTaskMethod method) {
        mWorkMethod = method;
    }

    @Override
    public void run() {
        super.run();
        if(mWorkMethod != null) {
            try {
                mWorkMethod.setup();
                mWorkMethod.process();
                mWorkMethod.cleanup();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "Job done!");
    }
}
