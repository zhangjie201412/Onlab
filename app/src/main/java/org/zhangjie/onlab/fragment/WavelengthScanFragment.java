package org.zhangjie.onlab.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import org.zhangjie.onlab.DeviceApplication;
import org.zhangjie.onlab.R;
import org.zhangjie.onlab.adapter.MultiSelectionAdapter;
import org.zhangjie.onlab.otto.BusProvider;
import org.zhangjie.onlab.otto.RezeroEvent;
import org.zhangjie.onlab.otto.SettingEvent;
import org.zhangjie.onlab.record.PhotoMeasureRecord;
import org.zhangjie.onlab.record.TimeScanRecord;
import org.zhangjie.onlab.record.WavelengthScanRecord;
import org.zhangjie.onlab.setting.TimescanSettingActivity;
import org.zhangjie.onlab.setting.WavelengthSettingActivity;
import org.zhangjie.onlab.utils.SharedPreferenceUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * Created by H151136 on 5/24/2016.
 */
public class WavelengthScanFragment extends Fragment implements View.OnClickListener {

    private boolean isFake = true;
    private static final String TAG = "Onlab.WavelengthScan";
    private ListView mListView;
    private MultiSelectionAdapter mAdapter;
    private List<HashMap<String, String>> mData;

    private float mInterval;
    private float mStart;
    private float mEnd;
    private int mTestMode;
    private int mSpeed;

    private Button mStartButton;
    private Button mStopButton;
    private Button mRezeroButton;
    private Button mProcessButton;

    private TextView mTestModeTextView;
    private CheckBox mSmoothCheckBox;

    //++++chart
    private LineChartView mChartView;
    private LineChartData mChartData;
    private List<Line> mLines;
    private Line mLine;
    private List<PointValue> mPoints;
    //----

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wavelength_scan, container, false);
        initUi(view);
        return view;
    }

    private void initUi(View view) {
        mStartButton = (Button) view.findViewById(R.id.bt_wavelength_scan_start);
        mStopButton = (Button) view.findViewById(R.id.bt_wavelength_scan_stop);
        mRezeroButton = (Button) view.findViewById(R.id.bt_wavelength_scan_rezero);
        mProcessButton = (Button) view.findViewById(R.id.bt_wavelength_scan_process);
        mStartButton.setOnClickListener(this);
        mStopButton.setOnClickListener(this);
        mRezeroButton.setOnClickListener(this);
        mProcessButton.setOnClickListener(this);

        mListView = (ListView)view.findViewById(R.id.lv_wavelength_scan);
        mData = new ArrayList<HashMap<String, String>>();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mAdapter = new MultiSelectionAdapter(getContext(), mData,
                    R.layout.item_wavelength_scan,
                    new String[]{"id", "wavelength", "abs", "trans", "energy"},
                    new int[]{R.id.item_index, R.id.item_wavelength, R.id.item_abs, R.id.item_trans, R.id.item_energy});
        } else {
            mAdapter = new MultiSelectionAdapter(getActivity(), mData,
                    R.layout.item_wavelength_scan,
                    new String[]{"id", "wavelength", "abs", "trans", "energy"},
                    new int[]{R.id.item_index, R.id.item_wavelength, R.id.item_abs, R.id.item_trans, R.id.item_energy});
        }
        mListView.setAdapter(mAdapter);

        mTestModeTextView = (TextView)view.findViewById(R.id.tv_wavelength_scan_test_mode);
        mSmoothCheckBox = (CheckBox)view.findViewById(R.id.check_smooth);
        mSmoothCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mLine.setCubic(isChecked);
                mChartData.setLines(mLines);
                mChartView.setLineChartData(mChartData);
            }
        });

        mChartView = (LineChartView) view.findViewById(R.id.hello_wavelength_scan);
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
        SharedPreferenceUtils sp = DeviceApplication.getInstance().getSpUtils();

        int mode = sp.getWavelengthscanTestMode();
        float limit_up = sp.getWavelengthscanLimitUp();
        float limit_down = sp.getWavelengthscanLimitDown();
        float start = sp.getWavelengthscanStart();
        float end = sp.getWavelengthscanEnd();
        int speed = sp.getWavelengthscanSpeed();
        float interval = sp.getWavelengthscanInterval();

        if(mode == WavelengthSettingActivity.TEST_MODE_ABS) {
            updateXYTitle(getString(R.string.wavelength_with_unit), getString(R.string.abs_with_unit),
                    start, end, limit_up, limit_down);
            mTestModeTextView.setText(getString(R.string.mode) + ": " + getString(R.string.abs));
        } else if(mode == WavelengthSettingActivity.TEST_MODE_TRANS) {
            updateXYTitle(getString(R.string.wavelength_with_unit), getString(R.string.trans_with_unit),
                    start, end, limit_up, limit_down);
            mTestModeTextView.setText(getString(R.string.mode) + ": " + getString(R.string.trans));
        }else if(mode == WavelengthSettingActivity.TEST_MODE_ENERGY) {
            updateXYTitle(getString(R.string.wavelength_with_unit), getString(R.string.energy),
                    start, end, limit_up, limit_down);
            mTestModeTextView.setText(getString(R.string.mode) + ": " + getString(R.string.energy));
        }

        mTestMode = mode;
        mStart = start;
        mEnd = end;
        mInterval = interval;
        mSpeed = speed;
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

    private void addItem(WavelengthScanRecord record) {
        HashMap<String, String> item = new HashMap<String, String>();
        int no = mData.size() + 1;
        record.setIndex(no);

        item.put("id", "" + no);
        item.put("wavelength", String.format("%.1f", record.getWavelength()));
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

    @Subscribe
    public void OnSettingEvent(SettingEvent event) {
        Context context = null;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context = getContext();
        } else {
            context = getActivity();
        }

        Intent intent = new Intent(context, WavelengthSettingActivity.class);
        startActivityForResult(intent, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == WavelengthSettingActivity.RESULT_OK) {
            Log.d(TAG, "OK");
            loadFromSetting();
        } else if(resultCode == WavelengthSettingActivity.RESULT_CANCEL) {
            Log.d(TAG, "CANCEL");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_wavelength_scan_start:
                if(isFake) {
                    int energy = (int) (Math.random() * 1000.0f);
                    float wavelength = (float) (Math.random() * 1000.0f);
                    float abs = (float) (Math.random() * 10);
                    float trans = (float) (Math.random() * 100);

                    WavelengthScanRecord record = new WavelengthScanRecord(-1,
                            wavelength, abs, trans, energy,
                            System.currentTimeMillis());
                    addItem(record);
                    updateChart((int) mStart + mData.size() * (int) mInterval, abs);
                }
                break;
            case R.id.bt_wavelength_scan_stop:
                break;
            case R.id.bt_wavelength_scan_rezero:
                BusProvider.getInstance().post(new RezeroEvent());
                break;
            case R.id.bt_wavelength_scan_process:
                break;
            default:
                break;
        }
    }
}
