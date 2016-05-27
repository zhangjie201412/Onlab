package org.zhangjie.onlab.device;

import android.util.Log;

import org.zhangjie.onlab.device.work.BaseWork;

/**
 * Created by H151136 on 5/27/2016.
 */
public class PhotometricMeasureWork extends BaseWork {

    private static final String TAG = "Onlab.PhotometricMea";
    @Override
    public void setup() throws InterruptedException {
        super.setup();
        Log.d(TAG, "setup");
        Thread.sleep(1000);
    }

    @Override
    public void process() throws InterruptedException {
        super.process();
        Log.d(TAG, "process");
        Thread.sleep(1000);
    }

    @Override
    public void cleanup() throws InterruptedException {
        super.cleanup();
        Log.d(TAG, "cleanup");
        Thread.sleep(1000);
    }
}
