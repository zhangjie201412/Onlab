package org.zhangjie.onlab.device.work;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by H151136 on 5/27/2016.
 */
public class WorkTask extends AsyncTask<Integer, Integer, Integer> {
    private static final String TAG = "Onlab.WorkTask";
    private WorkTaskMethod mWorkMethod = null;

    public WorkTask(WorkTaskMethod method) {
        mWorkMethod = method;
    }

    @Override
    protected Integer doInBackground(Integer... params) {
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
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
    }
}
