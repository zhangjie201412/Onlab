package org.zhangjie.onlab.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;
import org.zhangjie.onlab.R;

import lecho.lib.hellocharts.view.LineChartView;

/**
 * Created by H151136 on 5/24/2016.
 */
public class TimeScanFragment extends Fragment implements  View.OnClickListener {

    private static final String TAG = "Onlab.TimeScanFragment";
    private TextView mWavelengthTextView;
    private TextView mRatioTextView;
    private TextView mIntervalTextView;
    private TextView mTestModeTextView;
    private Button mStartButton;
    private Button mStopButton;
    private Button mRezeroButton;
    private LineChartView mChartView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_time_scan, container, false);
        initUi(view);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    private void initUi(View view) {
        mWavelengthTextView = (TextView)view.findViewById(R.id.tv_wavelength);
        mRatioTextView = (TextView)view.findViewById(R.id.tv_ratio);
        mIntervalTextView = (TextView)view.findViewById(R.id.tv_time_scan_interval);
        mTestModeTextView = (TextView)view.findViewById(R.id.tv_time_scan_test_mode);
        mStartButton = (Button)view.findViewById(R.id.bt_time_scan_start);
        mStopButton = (Button)view.findViewById(R.id.bt_time_scan_stop);
        mRezeroButton = (Button)view.findViewById(R.id.bt_time_scan_rezero);
        mChartView = (LineChartView)view.findViewById(R.id.hello_time_scan);
        mStartButton.setOnClickListener(this);
        mStopButton.setOnClickListener(this);
        mRezeroButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            default:
                break;
        }
    }
}
