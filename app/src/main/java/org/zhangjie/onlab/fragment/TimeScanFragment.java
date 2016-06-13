package org.zhangjie.onlab.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import org.w3c.dom.Text;
import org.zhangjie.onlab.DeviceApplication;
import org.zhangjie.onlab.R;
import org.zhangjie.onlab.adapter.MultiSelectionAdapter;
import org.zhangjie.onlab.device.DeviceManager;
import org.zhangjie.onlab.otto.BusProvider;
import org.zhangjie.onlab.otto.SettingEvent;
import org.zhangjie.onlab.otto.UpdateFragmentEvent;
import org.zhangjie.onlab.otto.WaitProgressEvent;
import org.zhangjie.onlab.record.PhotoMeasureRecord;
import org.zhangjie.onlab.record.TimeScanRecord;
import org.zhangjie.onlab.setting.TimescanSettingActivity;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * Created by H151136 on 5/24/2016.
 */
public class TimeScanFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "Onlab.TimeScanFragment";
    private TextView mWavelengthTextView;
    private TextView mRatioTextView;
    private TextView mIntervalTextView;
    private TextView mTestModeTextView;
    private Button mStartButton;
    private Button mStopButton;
    private Button mRezeroButton;
    private ListView mListView;
    private MultiSelectionAdapter mAdapter;
    private List<HashMap<String, String>> mData;
    private int mInterval = 1;
    private int mDuration = 60;
    private int mTestMode = 0;

    //+++chart
    private LineChartView mChartView;
    private LineChartData mChartData;
    private List<Line> mLines;
    private Line mLine;
    private List<PointValue> mPoints;
    //---
    private TimescanThread mThread;
    private int mX = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_time_scan, container, false);
        initUi(view);
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    private void initUi(View view) {
        mWavelengthTextView = (TextView) view.findViewById(R.id.tv_wavelength);
        mRatioTextView = (TextView) view.findViewById(R.id.tv_ratio);
        mIntervalTextView = (TextView) view.findViewById(R.id.tv_time_scan_interval);
        mTestModeTextView = (TextView) view.findViewById(R.id.tv_time_scan_test_mode);
        mStartButton = (Button) view.findViewById(R.id.bt_time_scan_start);
        mStopButton = (Button) view.findViewById(R.id.bt_time_scan_stop);
        mRezeroButton = (Button) view.findViewById(R.id.bt_time_scan_rezero);
        mChartView = (LineChartView) view.findViewById(R.id.hello_time_scan);
        mStartButton.setOnClickListener(this);
        mStopButton.setOnClickListener(this);
        mStopButton.setEnabled(false);
        mRezeroButton.setOnClickListener(this);

        mListView = (ListView) view.findViewById(R.id.lv_time_scan);
        mData = new ArrayList<HashMap<String, String>>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mAdapter = new MultiSelectionAdapter(getContext(), mData,
                    R.layout.item_time_scan,
                    new String[]{"id", "abs", "trans", "energy"},
                    new int[]{R.id.item_index, R.id.item_abs, R.id.item_trans, R.id.item_energy});
        } else {
            mAdapter = new MultiSelectionAdapter(getActivity(), mData,
                    R.layout.item_time_scan,
                    new String[]{"id",  "abs", "trans", "energy"},
                    new int[]{R.id.item_index, R.id.item_abs, R.id.item_trans, R.id.item_energy});
        }
        mListView.setAdapter(mAdapter);
        initChart();
        loadFromSetting();
    }

    private void initChart() {
        mPoints = new ArrayList<PointValue>();
        mLines = new ArrayList<Line>();
        mLine = new Line(mPoints).setColor(Color.WHITE).setCubic(true);
        mLine.setPointRadius(1);
        mLine.setStrokeWidth(1);
        mLines.add(mLine);

        mChartData = new LineChartData();
        mChartData.setBaseValue(Float.NEGATIVE_INFINITY);
        mChartView.setLineChartData(mChartData);
    }

    private void loadFromSetting() {
        int mode = DeviceApplication.getInstance().getSpUtils().getTimescanTestMode();
        float work_wavelength = DeviceApplication.getInstance().getSpUtils().getTimescanWorkWavelength();
        int start_time = DeviceApplication.getInstance().getSpUtils().getTimescanStartTime();
        int end_time = DeviceApplication.getInstance().getSpUtils().getTimescanEndTime();
        int time_interval = DeviceApplication.getInstance().getSpUtils().getTimescanTimeInterval();
        float limit_up = DeviceApplication.getInstance().getSpUtils().getTimescanLimitUp();
        float limit_down = DeviceApplication.getInstance().getSpUtils().getTimescanLimitDown();

        //set workwavelength
        mWavelengthTextView.setText(getString(R.string.wavelength) + ": " + work_wavelength);

        if(mode == TimescanSettingActivity.TEST_MODE_ABS) {
            updateXYTitle(getString(R.string.time_with_unit), getString(R.string.abs_with_unit),
                    start_time, end_time, limit_up, limit_down);
            mTestModeTextView.setText(getString(R.string.mode) + ": " + getString(R.string.abs));
        } else {
            updateXYTitle(getString(R.string.time_with_unit), getString(R.string.trans_with_unit),
                    start_time, end_time, limit_up, limit_down);
            mTestModeTextView.setText(getString(R.string.mode) + ": " + getString(R.string.trans));
        }

        mInterval = time_interval;
        mIntervalTextView.setText(getString(R.string.interval) + ": " + mInterval + " " + getString(R.string.s));
        mTestMode = mode;
    }

    void updateXYTitle(String xTitle, String yTitle, float left, float right, float top, float bottom) {
        final Viewport viewport = new Viewport(mChartView.getCurrentViewport());
        viewport.left = left;
        viewport.top = top;
        viewport.right = right;
        viewport.bottom = bottom;

        mChartView.setMaximumViewport(viewport);
        mChartView.setCurrentViewport(viewport);
        mChartView.setViewportCalculationEnabled(false);

        Axis axisX = new Axis();
        Axis axisY = new Axis();
        axisX.setName(xTitle);
        axisX.setHasSeparationLine(true);
//        axisX.setHasLines(true);
        axisY.setName(yTitle);
        axisY.setHasSeparationLine(true);
//        axisY.setHasLines(true);
        mChartData.setAxisXBottom(axisX);
        mChartData.setAxisYLeft(axisY);
    }

    private void addItem(TimeScanRecord record) {
        HashMap<String, String> item = new HashMap<String, String>();
        int no = mData.size() + 1;
        record.setIndex(no);

        item.put("id", "" + no);
        item.put("abs", String.format("%.3f", record.getAbs()));
        item.put("trans", String.format("%.3f", record.getTrans()));
        item.put("energy", "" + record.getEnergy());
        item.put("date", "" + record.getDate());
        mData.add(item);
        mAdapter.add();
        mAdapter.notifyDataSetChanged();
        if (mData.size() > 0) {
            mListView.setSelection(mData.size() - 1);
        }
    }

    @Subscribe
    public void onUpdateFragmentEvent(UpdateFragmentEvent event) {
        Log.d(TAG, "ts onUpdate type = " + event.getType());
        if(event.getType() == UpdateFragmentEvent.UPDATE_FRAGMENT_EVENT_TYPE_TIME_SCAN) {
            int energy = event.getEnergy();
            float abs = event.getAbs();
            float trans = event.getTrans();
            TimeScanRecord record = new TimeScanRecord(-1,
                    abs, trans, energy,
                    System.currentTimeMillis());
            addItem(record);
            //update chart
            if(mTestMode == TimescanSettingActivity.TEST_MODE_ABS) {
                updateChart(mX, abs);
            } else if(mTestMode == TimescanSettingActivity.TEST_MODE_TRANS) {
                updateChart(mX, trans);
            }
        }
    }

    private void updateChart(int x, float y) {
        Log.d(TAG, "update chart x = " + x + ", y = " + y);
        mPoints.add(new PointValue(x, y));
        mChartData.setLines(mLines);
        mChartView.setLineChartData(mChartData);
    }

    private void clearData() {
        mData.clear();
        mAdapter.notifyDataSetChanged();
        mPoints.clear();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_time_scan_start:
                clearData();
                mThread = new TimescanThread(mInterval, mDuration);
                mThread.start();

                break;
            case R.id.bt_time_scan_stop:
                if(mThread.isAlive()) {
                    mThread.pause();
                }
                break;
            case R.id.bt_time_scan_rezero:
                DeviceManager.getInstance().rezeroWork();
                break;
            default:
                break;
        }
    }

    private final int TIME_SCAN_START = 0x00;
    private final int TIME_SCAN_END = 0x01;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == TIME_SCAN_START) {
                mStopButton.setEnabled(true);
                mStartButton.setEnabled(false);
            } else if(msg.what == TIME_SCAN_END) {
                mStopButton.setEnabled(false);
                mStartButton.setEnabled(true);
            }
        }
    };

    @Subscribe public void OnSettingEvent(SettingEvent event) {
        Context context = null;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context = getContext();
        } else {
            context = getActivity();
        }

        Intent intent = new Intent(context, TimescanSettingActivity.class);
        startActivityForResult(intent, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == TimescanSettingActivity.RESULT_OK) {
            Log.d(TAG, "OK");
            loadFromSetting();
        } else if(resultCode == TimescanSettingActivity.RESULT_CANCEL) {
            Log.d(TAG, "CANCEL");
        }
    }

    class TimescanThread extends Thread {

        private boolean start = false;
        private int interval;
        private int duration;

        public TimescanThread(int interval, int duration) {
            this.interval = interval;
            this.duration = duration;
        }

        @Override
        public synchronized void start() {
            super.start();
            start = true;
            mHandler.sendEmptyMessage(TIME_SCAN_START);
        }

        public void pause() {
            try {
                start = false;
                join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            super.run();

            for(int i = 0; i < duration; i += interval) {
                mX = i;
                if(!start) {
                    break;
                } else {
                    try {
                        //work
                        DeviceManager.getInstance().timeScanWork();
                        Thread.sleep(interval * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            mHandler.sendEmptyMessage(TIME_SCAN_END);
            DeviceManager.getInstance().setLoopThreadRestart();
        }
    }
}
