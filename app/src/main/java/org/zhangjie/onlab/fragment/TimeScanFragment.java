package org.zhangjie.onlab.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import org.w3c.dom.Text;
import org.zhangjie.onlab.DeviceApplication;
import org.zhangjie.onlab.MainActivity;
import org.zhangjie.onlab.R;
import org.zhangjie.onlab.adapter.MultiSelectionAdapter;
import org.zhangjie.onlab.device.DeviceManager;
import org.zhangjie.onlab.dialog.CalcSpeedDialog;
import org.zhangjie.onlab.dialog.SaveNameDialog;
import org.zhangjie.onlab.otto.BusProvider;
import org.zhangjie.onlab.otto.FileOperateEvent;
import org.zhangjie.onlab.otto.LoadWavelengthDialogEvent;
import org.zhangjie.onlab.otto.SettingEvent;
import org.zhangjie.onlab.otto.UpdateFragmentEvent;
import org.zhangjie.onlab.otto.WaitProgressEvent;
import org.zhangjie.onlab.record.PhotoMeasureRecord;
import org.zhangjie.onlab.record.TimeScanRecord;
import org.zhangjie.onlab.setting.TimescanSettingActivity;
import org.zhangjie.onlab.setting.WavelengthSettingActivity;
import org.zhangjie.onlab.utils.Utils;

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
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * Created by H151136 on 5/24/2016.
 */
public class TimeScanFragment extends Fragment implements View.OnClickListener, CalcSpeedDialog.CalcSpeedListener {

    private static final String TAG = "Onlab.TimeScanFragment";
    private boolean isFake = false;
    private TextView mWavelengthTextView;
    private TextView mRatioTextView;
    private TextView mIntervalTextView;
    private TextView mTestModeTextView;
    private Button mStartButton;
    private Button mStopButton;
    private Button mRezeroButton;
    private Button mCurrentButton;
    private Button mProcessButton;
    private ListView mListView;
    private MultiSelectionAdapter mAdapter;
    private List<HashMap<String, String>>[] mData;
    private List<HashMap<String, String>> mPeakData;
    private List<HashMap<String, String>> mOperateData;

    private int mInterval = 1;
    private int mDuration = 60;
    private int mTestMode = 0;

    //+++chart
    private LineChartView mChartView;
    private LineChartData mChartData;
    private List<Line> mLines;
    private Line[] mLine;
    private List<PointValue>[] mPoints;
    private Line mPeakLine;
    private Line mOperateLine;
    private Line mDerivativeLine;
    private List<PointValue> mPeakPoints;
    private List<PointValue> mOperatePoints;
    private List<PointValue> mDerivativePoints;
    //---
    private TimescanThread mThread;
    private int mX = 0;
    private SaveNameDialog mSaveDialog;
    private CalcSpeedDialog mCalcSpeedDialog;

    private boolean loadFile = false;
    private int loadFileIndex = -1;

    private int mCurDataIndex = 0;
    private int mLstDataIndex = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_time_scan, container, false);
        Utils.needToSave = false;
        mCurDataIndex = 0;
        initUi(view);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.needToSave = false;
        Log.d(TAG, "onDestroy");
        loadFile = false;
    }

    private void initUi(View view) {
        mWavelengthTextView = (TextView) view.findViewById(R.id.tv_wavelength);
        mRatioTextView = (TextView) view.findViewById(R.id.tv_ratio);
        mIntervalTextView = (TextView) view.findViewById(R.id.tv_time_scan_interval);
        mTestModeTextView = (TextView) view.findViewById(R.id.tv_time_scan_test_mode);
        mStartButton = (Button) view.findViewById(R.id.bt_time_scan_start);
        mStopButton = (Button) view.findViewById(R.id.bt_time_scan_stop);
        mRezeroButton = (Button) view.findViewById(R.id.bt_time_scan_rezero);
        mCurrentButton = (Button) view.findViewById(R.id.bt_time_scan_current);
        mProcessButton = (Button) view.findViewById(R.id.bt_time_scan_process);
        mChartView = (LineChartView) view.findViewById(R.id.hello_time_scan);
        mStartButton.setOnClickListener(this);
        mStopButton.setOnClickListener(this);
        mStopButton.setEnabled(false);
        mRezeroButton.setOnClickListener(this);
        mCurrentButton.setOnClickListener(this);
        mProcessButton.setOnClickListener(this);
        mRatioTextView.setVisibility(View.INVISIBLE);

        mListView = (ListView) view.findViewById(R.id.lv_time_scan);
        mData = new List[4];

        mData[0] = new ArrayList<HashMap<String, String>>();
        mData[1] = new ArrayList<HashMap<String, String>>();
        mData[2] = new ArrayList<HashMap<String, String>>();
        mData[3] = new ArrayList<HashMap<String, String>>();

        mPeakData = new ArrayList<HashMap<String, String>>();
        mOperateData = new ArrayList<HashMap<String, String>>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mAdapter = new MultiSelectionAdapter(getContext(), mData[mCurDataIndex],
                    R.layout.item_time_scan,
                    new String[]{"id", "abs", "trans", "energy"},
                    new int[]{R.id.item_index, R.id.item_abs, R.id.item_trans, R.id.item_energy});
        } else {
            mAdapter = new MultiSelectionAdapter(getActivity(), mData[mCurDataIndex],
                    R.layout.item_time_scan,
                    new String[]{"id", "abs", "trans", "energy"},
                    new int[]{R.id.item_index, R.id.item_abs, R.id.item_trans, R.id.item_energy});
        }
        mListView.setAdapter(mAdapter);
        mAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (mData[mCurDataIndex].size() > 0) {
                    Utils.needToSave = true;
                } else {
                    Utils.needToSave = false;
                }
            }
        });
        initChart();
        loadFromSetting();
        mSaveDialog = new SaveNameDialog();
        mSaveDialog.init(-1, getString(R.string.action_save), getString(R.string.name), new SaveNameDialog.SettingInputListern() {
            @Override
            public void onSettingInputComplete(int id, String name) {

                if (name.length() < 1 || (!Utils.isValidName(name))) {
                    Toast.makeText(getActivity(), getString(R.string.notice_name_invalid), Toast.LENGTH_SHORT).show();
                    return;
                }
                List<String> saveFileList = DeviceApplication.getInstance().getTimeScanDb().getTables();
                Log.d(TAG, "Alread saved -> " + saveFileList.size() + " files.");

                for (int i = 0; i < saveFileList.size(); i++) {
                    Log.d(TAG, String.format("[%d] -> %s\n", i, saveFileList.get(i)));
                }
                String fileName = name;
                for (int i = 0; i < mData[mCurDataIndex].size(); i++) {
                    int index = 0;
                    int second = 0;
                    float abs = 0.0f;
                    float trans = 0.0f;
                    int energy = 0;
                    long date = 0;

                    HashMap<String, String> map = mData[mCurDataIndex].get(i);
                    index = Integer.parseInt(map.get("id"));
                    second = Integer.parseInt(map.get("second"));
                    abs = Float.parseFloat(map.get("abs"));
                    trans = Float.parseFloat(map.get("trans"));
                    energy = Integer.parseInt(map.get("energy"));
                    date = Long.parseLong(map.get("date"));

                    TimeScanRecord record = new TimeScanRecord(index, second, abs, trans, energy, date);
                    DeviceApplication.getInstance().getTimeScanDb().saveRecord(fileName, record);
                }
                Log.d(TAG, "save to -> " + fileName);
                Utils.needToSave = false;
            }

            @Override
            public void abort() {
                getFragmentManager().popBackStack();
            }
        });
        if (loadFile) {
            loadFileById(loadFileIndex);
        }
        setCurrentButton();
        mCalcSpeedDialog = new CalcSpeedDialog();
        mCalcSpeedDialog.init(this);
    }

    public void prepareLoadFile(int id) {
        loadFile = true;
        loadFileIndex = id;
    }

    private void initChart() {
        mPoints = new List[4];
        mPoints[0] = new ArrayList<PointValue>();
        mPoints[1] = new ArrayList<PointValue>();
        mPoints[2] = new ArrayList<PointValue>();
        mPoints[3] = new ArrayList<PointValue>();
        mPeakPoints = new ArrayList<PointValue>();
        mOperatePoints = new ArrayList<PointValue>();
        mDerivativePoints = new ArrayList<PointValue>();
        mLines = new ArrayList<Line>();
        mLine = new Line[4];
        mLine[0] = new Line(mPoints[0]).setColor(Utils.COLORS[0]).setCubic(true);
        mLine[1] = new Line(mPoints[1]).setColor(Utils.COLORS[1]).setCubic(true);
        mLine[2] = new Line(mPoints[2]).setColor(Utils.COLORS[2]).setCubic(true);
        mLine[3] = new Line(mPoints[3]).setColor(Utils.COLORS[3]).setCubic(true);

        for (int i = 0; i < mLine.length; i++) {
            mLine[i].setPointRadius(1);
            mLine[i].setStrokeWidth(1);
            mLine[i].setCubic(false);
        }

        mPeakLine = new Line(mPeakPoints).setColor(ChartUtils.COLOR_GREEN).setCubic(false);
        mOperateLine = new Line(mOperatePoints).setColor(ChartUtils.COLOR_RED).setCubic(false);
        mDerivativeLine = new Line(mDerivativePoints).setColor(Color.YELLOW).setCubic(false);
        mOperateLine.setPointRadius(2);
        mOperateLine.setStrokeWidth(2);
        mOperateLine.setCubic(false);

        mPeakLine.setPointRadius(3);
        mPeakLine.setStrokeWidth(1);

        mDerivativeLine.setPointRadius(1);
        mDerivativeLine.setStrokeWidth(1);
        mDerivativeLine.setCubic(false);
        for (int i = 0; i < mLine.length; i++) {
            mLines.add(mLine[i]);
        }
        mLines.add(mPeakLine);
        mLines.add(mOperateLine);
        mLines.add(mDerivativeLine);

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

        if (mode == TimescanSettingActivity.TEST_MODE_ABS) {
            updateXYTitle(getString(R.string.time_with_unit), getString(R.string.abs_with_unit),
                    start_time, end_time, limit_up, limit_down);
            mTestModeTextView.setText(getString(R.string.mode) + ": " + getString(R.string.abs));
        } else {
            updateXYTitle(getString(R.string.time_with_unit), getString(R.string.trans_with_unit),
                    start_time, end_time, limit_up, limit_down);
            mTestModeTextView.setText(getString(R.string.mode) + ": " + getString(R.string.trans));
        }

        mDuration = end_time;
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

    void autoUpdateChart() {
//        mChartView.setViewportCalculationEnabled(true);
//        mChartData.setLines(mLines);
//        mChartView.setLineChartData(mChartData);
    }

    private void addItem(int index, TimeScanRecord record) {
        HashMap<String, String> item = new HashMap<String, String>();
        int no = mData[index].size() + 1;
        record.setIndex(no);

        item.put("id", "" + no);
        item.put("second", "" + record.getSecond());


        item.put("abs", Utils.formatAbs(record.getAbs()));
        item.put("trans", Utils.formatTrans(record.getTrans()));
        item.put("energy", "" + record.getEnergy());
        item.put("date", "" + record.getDate());
        mData[index].add(item);
        mAdapter.add();
        mAdapter.notifyDataSetChanged();
        if (mData[index].size() > 0) {
            mListView.setSelection(mData[index].size() - 1);
        }
    }

    @Subscribe
    public void onUpdateFragmentEvent(UpdateFragmentEvent event) {
        Log.d(TAG, "ts onUpdate type = " + event.getType());
        if (event.getType() == UpdateFragmentEvent.UPDATE_FRAGMENT_EVENT_TYPE_TIME_SCAN) {
            int energy = event.getEnergy();
            float abs = event.getAbs();
            float trans = event.getTrans();

            TimeScanRecord record = new TimeScanRecord(-1, mX,
                    abs, trans, energy,
                    System.currentTimeMillis());
            addItem(mCurDataIndex, record);
            //update chart
            if (mTestMode == TimescanSettingActivity.TEST_MODE_ABS) {
                updateChart(mCurDataIndex, mX, abs);
            } else if (mTestMode == TimescanSettingActivity.TEST_MODE_TRANS) {
                updateChart(mCurDataIndex, mX, trans);
            }
        }
    }

    private void updateChart(int index, int x, float y) {
        Log.d(TAG, "update chart x = " + x + ", y = " + y);
        mPoints[index].add(new PointValue(x, y));
        mChartData.setLines(mLines);
        mChartView.setLineChartData(mChartData);
    }

    private void updateChart(int index, TimeScanRecord record) {
        int x;
        float y = 0.0f;
        int mode = DeviceApplication.getInstance().getSpUtils().getWavelengthscanTestMode();
        if (mode == WavelengthSettingActivity.TEST_MODE_ABS) {
            y = record.getAbs();
        } else if (mode == WavelengthSettingActivity.TEST_MODE_TRANS) {
            y = record.getTrans();
        }
        x = record.getSecond();
        updateChart(index, x, y);
    }

    private void updatePeakChart(TimeScanRecord record) {
        int mode = DeviceApplication.getInstance().getSpUtils().getTimescanTestMode();
        if (mode == WavelengthSettingActivity.TEST_MODE_ABS) {
            mPeakPoints.add(new PointValue(record.getSecond(), record.getAbs()));
        } else if (mode == WavelengthSettingActivity.TEST_MODE_TRANS) {
            mPeakPoints.add(new PointValue(record.getSecond(), record.getTrans()));
        }

        mPeakLine.setHasPoints(true);
        mPeakLine.setHasLines(false);
        if (!mLines.contains(mPeakLine)) {
            mLines.add(mPeakLine);
        }
        mChartData.setLines(mLines);
        mChartView.setLineChartData(mChartData);
    }

    private void updateOperateChart(TimeScanRecord record) {
        int mode = DeviceApplication.getInstance().getSpUtils().getTimescanTestMode();
        if (mode == WavelengthSettingActivity.TEST_MODE_ABS) {
            mOperatePoints.add(new PointValue(record.getSecond(), record.getAbs()));
        } else if (mode == WavelengthSettingActivity.TEST_MODE_TRANS) {
            mOperatePoints.add(new PointValue(record.getSecond(), record.getTrans()));
        }
        mOperateLine.setHasPoints(true);
        mOperateLine.setHasLines(true);
        if (!mLines.contains(mOperateLine)) {
            mLines.add(mOperateLine);
        }
        mChartData.setLines(mLines);
        mChartView.setLineChartData(mChartData);
    }

    private void updateDerivativeChart(int x, float y) {
        mDerivativePoints.add(new PointValue(x, y));
        mDerivativeLine.setHasPoints(true);
        mDerivativeLine.setHasLines(true);
        if (!mLines.contains(mDerivativeLine)) {
            mLines.add(mDerivativeLine);
        }
        mChartData.setLines(mLines);
        mChartView.setLineChartData(mChartData);
    }

    private void clearDerivativeChart() {
        mDerivativePoints.clear();
        mChartData.setLines(mLines);
        mChartView.setLineChartData(mChartData);
    }

    private void clearData(int index) {
        mData[index].clear();
        mAdapter.notifyDataSetChanged();
        mPoints[index].clear();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_time_scan_current:
                showCurrentLines();
                break;
            case R.id.bt_time_scan_process:
                showProcessDialog();
                break;
            case R.id.bt_time_scan_start:
                if (!isFake) {
                    for (int i = 3; i >= 0; i--) {
                        if (mData[i].size() == 0) {
                            mCurDataIndex = i;
                            mLstDataIndex = mCurDataIndex;
                        }
                    }
                    setCurrentButton();
                    //set adapter data
                    mAdapter.setData(mData[mCurDataIndex]);
                    clearData(mCurDataIndex);
                    mThread = new TimescanThread(mInterval, mDuration);
                    mThread.start();
                } else {
                    for (int i = 3; i >= 0; i--) {
                        if (mData[i].size() == 0) {
                            mCurDataIndex = i;
                        }
                    }
                    setCurrentButton();
                    //set adapter data
                    mAdapter.setData(mData[mCurDataIndex]);
                    clearData(mCurDataIndex);
                    for (int i = 0; i <= mDuration; i += mInterval) {
                        int energy = (int) (Math.random() * 1000.0f);
                        float abs = (float) (Math.random() * 2);
                        float trans = (float) (Math.random() * 100);
                        TimeScanRecord record = new TimeScanRecord(-1, i,
                                abs, trans, energy,
                                System.currentTimeMillis());
                        addItem(mCurDataIndex, record);
                        //update chart
                        if (mTestMode == TimescanSettingActivity.TEST_MODE_ABS) {
                            updateChart(mCurDataIndex, i, abs);
                        } else if (mTestMode == TimescanSettingActivity.TEST_MODE_TRANS) {
                            updateChart(mCurDataIndex, i, trans);
                        }
                    }
                }
                break;
            case R.id.bt_time_scan_stop:
                if (mThread.isAlive()) {
                    mThread.pause();
                    mCurDataIndex = mLstDataIndex;
                }
                break;
            case R.id.bt_time_scan_rezero:
                DeviceManager.getInstance().rezeroWork();
                break;
            default:
                break;
        }
    }

    private void showCurrentLines() {
        int avaliables = 0;
        List<String> lineItems = new ArrayList<String>();
        final int[] indicates = new int[4];
        int index = 0;
        for (int i = 0; i < 4; i++) {
            if (mData[i].size() > 0) {
                avaliables++;
                lineItems.add(getString(R.string.line) + (i + 1));
                indicates[index++] = i;
            }
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.select_lines));
        builder.setIcon(R.mipmap.ic_launcher);
        String[] avaItems = new String[avaliables];
        for (int i = 0; i < avaliables; i++) {
            avaItems[i] = (String) (lineItems.toArray()[i]);
        }
        builder.setSingleChoiceItems(avaItems, mCurDataIndex, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "select line " + (indicates[which] + 1));
                mCurDataIndex = indicates[which];
                setCurrentButton();
                mAdapter.setData(mData[mCurDataIndex]);
                mAdapter.notifyDataSetChanged();
            }
        });
        builder.setNeutralButton(getString(R.string.action_delete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getActivity(), "delete " + mCurDataIndex, Toast.LENGTH_SHORT).show();
            }
        });
        builder.create().show();
    }

    private void setCurrentButton() {
        mCurrentButton.setText(getString(R.string.current) +
                ": " + getString(R.string.line) + "" + (mCurDataIndex + 1));
        mCurrentButton.setTextColor(Utils.COLORS[mCurDataIndex]);
    }

    private final int PROCESS_ITEM_CUBIC = 0;
    private final int PROCESS_ITEM_PEAK = 1;
    private final int PROCESS_ITEM_DERIVATIVE = 2;
    private final int PROCESS_ITEM_OPERATION = 3;
    private final int PROCESS_ITEM_SPEED = 4;

    private int mOperateType = 0;
    private boolean mOperateSelect = false;

    private void showProcessDialog() {
        String[] items = getResources().getStringArray(R.array.processings_ts);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        int count = items.length;
        boolean[] select = new boolean[count];
        if (mLine[mCurDataIndex].isCubic()) {
            select[PROCESS_ITEM_CUBIC] = true;
        }
        if (mPeakPoints.size() > 0) {
            select[PROCESS_ITEM_PEAK] = true;
        }
        if(mDerivativePoints.size() > 0) {
            select[PROCESS_ITEM_DERIVATIVE] = true;
        }
        select[PROCESS_ITEM_OPERATION] = mOperateSelect;

        builder.setTitle(R.string.process);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setMultiChoiceItems(items, select, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                switch (which) {
                    case PROCESS_ITEM_CUBIC:
                        mLine[mCurDataIndex].setCubic(isChecked);
                        mChartData.setLines(mLines);
                        mChartView.setLineChartData(mChartData);
                        break;
                    case PROCESS_ITEM_PEAK:
                        if (isChecked) {
                            makePeak(mCurDataIndex, DeviceApplication.getInstance().getSpUtils().getPeakDistance());
                        } else {
                            makeNormal(mCurDataIndex);
                        }
                        break;
                    case PROCESS_ITEM_DERIVATIVE:
                        if(isChecked) {
                            makeDerivative(mCurDataIndex);
                        } else {
                            clearDerivativeChart();
                        }
                        break;
                    case PROCESS_ITEM_OPERATION:
                        if (isChecked) {
                            dialog.dismiss();
                            //do operation
                            //select lines
                            int avaliables = 0;
                            List<String> lineItems = new ArrayList<String>();
                            for (int i = 0; i < 4; i++) {
                                if (mData[i].size() > 0) {
                                    avaliables++;
                                    lineItems.add(getString(R.string.line) + (i + 1));
                                }
                            }
                            if (avaliables >= 2) {
                                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle(getString(R.string.select_lines));
                                builder.setIcon(R.mipmap.ic_launcher);
                                final boolean[] select = new boolean[avaliables];
                                String[] avaItems = new String[avaliables];
                                for (int i = 0; i < avaliables; i++) {
                                    avaItems[i] = (String) (lineItems.toArray()[i]);
                                }
                                builder.setMultiChoiceItems(avaItems, select, new DialogInterface.OnMultiChoiceClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                        select[which] = isChecked;
                                    }
                                });
                                builder.setPositiveButton(R.string.ok_string, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        int selectCount = 0;
                                        for (int i = 0; i < select.length; i++) {
                                            if (select[i]) {
                                                selectCount++;
                                            }
                                        }

                                        Log.d(TAG, "selectCount = " + selectCount);
                                        final int[] ids = new int[2];
                                        int index = 0;
                                        if (selectCount != 2) {
                                            Toast.makeText(getActivity(), getString(R.string.notice_select_2), Toast.LENGTH_SHORT).show();
                                            return;
                                        } else {
                                            for (int i = 0; i < select.length; i++) {
                                                if (select[i]) {
                                                    ids[index++] = i;
                                                }
                                            }

                                            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                            builder.setTitle(getString(R.string.select_operation));
                                            builder.setIcon(R.mipmap.ic_launcher);
                                            builder.setSingleChoiceItems(getResources().getStringArray(R.array.operations), -1, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    mOperateType = which;
                                                }
                                            });
                                            builder.setPositiveButton(R.string.ok_string, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                    makeOperate(ids[0], ids[1], mOperateType);
                                                    mOperateSelect = true;
                                                }
                                            });
                                            builder.create().show();
                                        }
                                        //do operation
                                    }
                                });

                                builder.create().show();
                            } else {
                                Toast.makeText(getActivity(), getString(R.string.lines_less_than_2), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            //cancel operation
                            makeNormal(mCurDataIndex);
                            mOperateSelect = false;
                        }
                        break;

                    case PROCESS_ITEM_SPEED:
                        dialog.dismiss();
                        mCalcSpeedDialog.show(getFragmentManager(), "calc speed");

                        break;
                    default:
                        break;
                }
            }
        });

        builder.create().show();
    }

    private void makeOperate(int id1, int id2, int operateType) {
        Log.d(TAG, "id1 = " + id1 + ", id2 = " + id2 + ", operateType = " + operateType);
        int count = 0;
        mOperateData.clear();
        mOperatePoints.clear();
        //get the less count item
        if (mData[id1].size() > mData[id2].size()) {
            count = mData[id2].size();
        } else {
            count = mData[id1].size();
        }

        for (int i = 0; i < count; i++) {
            HashMap<String, String> item = new HashMap<String, String>();
            HashMap<String, String> map1 = mData[id1].get(i);
            HashMap<String, String> map2 = mData[id2].get(i);

            int no = mOperateData.size() + 1;

            int energy = 0;
            float abs = 0.0f;
            float trans = 0.0f;
            int second = 0;

            if (operateType == Utils.OPERATE_TYPE_ADD) {
                energy = Integer.parseInt(map1.get("energy")) + Integer.parseInt(map1.get("energy"));
                abs = Float.parseFloat(map1.get("abs")) + Float.parseFloat(map2.get("abs"));
                trans = Float.parseFloat(map1.get("trans")) + Float.parseFloat(map2.get("trans"));
            } else if (operateType == Utils.OPERATE_TYPE_SUB) {
                energy = Integer.parseInt(map1.get("energy")) - Integer.parseInt(map1.get("energy"));
                abs = Float.parseFloat(map1.get("abs")) - Float.parseFloat(map2.get("abs"));
                trans = Float.parseFloat(map1.get("trans")) - Float.parseFloat(map2.get("trans"));
            } else if (operateType == Utils.OPERATE_TYPE_MUL) {
                energy = Integer.parseInt(map1.get("energy")) * Integer.parseInt(map1.get("energy"));
                abs = Float.parseFloat(map1.get("abs")) * Float.parseFloat(map2.get("abs"));
                trans = Float.parseFloat(map1.get("trans")) * Float.parseFloat(map2.get("trans"));
            } else if (operateType == Utils.OPERATE_TYPE_DIV) {
                energy = Integer.parseInt(map1.get("energy")) / Integer.parseInt(map1.get("energy"));
                abs = Float.parseFloat(map1.get("abs")) / Float.parseFloat(map2.get("abs"));
                trans = Float.parseFloat(map1.get("trans")) / Float.parseFloat(map2.get("trans"));
            }

            second = Integer.parseInt(map1.get("second"));

            item.put("id", "" + no);
            item.put("second", "" + second);
            item.put("abs", Utils.formatAbs(abs));
            item.put("trans", Utils.formatTrans(trans));
            item.put("energy", "" + energy);
            mOperateData.add(item);
        }
        mAdapter.setData(mOperateData);
        mAdapter.notifyDataSetChanged();
        for (int i = 0; i < mOperateData.size(); i++) {
            int index = 0;
            int energy = 0;
            float abs = 0.0f;
            float trans = 0.0f;
            int second = 0;

            HashMap<String, String> map = mOperateData.get(i);
            index = Integer.parseInt(map.get("id"));
            energy = Integer.parseInt(map.get("energy"));
            abs = Float.parseFloat(map.get("abs"));
            trans = Float.parseFloat(map.get("trans"));
            second = Integer.parseInt(map.get("second"));
            updateOperateChart(new TimeScanRecord(index, second, abs, trans,
                    energy, System.currentTimeMillis()));
        }
    }

    private void makeDerivative(int id) {
        int total = mData[id].size();

        if(total == 0) {
            return;
        }

        float[] yy = new float[total];
        int [] x = new int[total];

        for (int i = 0; i < total; i++) {
            float abs = 0.0f;
            float trans = 0.0f;
            int second = 0;

            HashMap<String, String> map = mData[id].get(i);
            second = Integer.parseInt(map.get("second"));
            abs = Float.parseFloat(map.get("abs"));
            trans = Float.parseFloat(map.get("trans"));
            int mode = DeviceApplication.getInstance().getSpUtils().getTimescanTestMode();
            if (mode == TimescanSettingActivity.TEST_MODE_ABS) {
                yy[i] = abs;
            } else if (mode == TimescanSettingActivity.TEST_MODE_TRANS) {
                yy[i] = trans;
            }
            x[i] = second;
        }
        mDerivativePoints.clear();

        for(int i = 0; i < total; i++) {
            float y = 0.0f;
            //first one
            if(i == 0) {
                y = (yy[1] - yy[0]) / (x[1] - x[0]);
            }
            //last one
            else if(i == total - 1) {
                //skip
            }
            //others
            else {
                y = (yy[i + 1] - yy[i - 1]) / (x[i + 1] - x[i - 1]);
            }
            updateDerivativeChart(x[i], y);
        }
        autoUpdateChart();
    }

    private void makePeak(int id, float distance) {
        int totalSize = mData[id].size();
        if (totalSize == 0) {
            return;
        }

        float[] data = new float[totalSize];
        int[] ind2 = new int[totalSize];
        int[] ind = new int[totalSize];
        int ind_count2 = 0;
        int ind_count = 0;

        for (int i = 0; i < mData[id].size(); i++) {
            int index = 0;
            int energy = 0;
            float abs = 0.0f;
            float trans = 0.0f;

            HashMap<String, String> map = mData[id].get(i);
            index = Integer.parseInt(map.get("id"));
            energy = Integer.parseInt(map.get("energy"));
            abs = Float.parseFloat(map.get("abs"));
            trans = Float.parseFloat(map.get("trans"));
            int mode = DeviceApplication.getInstance().getSpUtils().getTimescanTestMode();
            if (mode == TimescanSettingActivity.TEST_MODE_ABS) {
                data[i] = abs;
            } else if (mode == TimescanSettingActivity.TEST_MODE_TRANS) {
                data[i] = trans;
            }
        }

        int direction = data[0] > 0 ? -1 : 1;
        for (int i = 0; i < totalSize - 1; i++) {
            if ((data[i + 1] - data[i]) * direction > 0) {
                direction = -direction;
                if (direction == 1) {
                    ind2[ind_count2] = i;
                    ind_count2++;
                } else {
                    ind2[ind_count2] = i;
                    ind_count2++;
                }
            }
        }

        for (int i = 0; i < ind_count2; i++) {
            float d = Math.abs(data[ind2[i]] - data[ind2[i + 1]]);

            if (d >= distance) {
                ind[ind_count] = ind2[i];
                ind[ind_count + 1] = ind2[i + 1];
                ind_count = ind_count + 2;
            }
        }

        mPeakData.clear();
        mPeakPoints.clear();
        mChartData.setLines(mLines);
        mChartView.setLineChartData(mChartData);
        for (int i = 0; i < ind_count; i++) {
            mPeakData.add(mData[id].get(ind[i]));
        }
        mAdapter.setData(mPeakData);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        for (int i = 0; i < mPeakData.size(); i++) {
            int index = 0;
            int energy = 0;
            float abs = 0.0f;
            float trans = 0.0f;
            int second = 0;

            HashMap<String, String> map = mPeakData.get(i);
            index = Integer.parseInt(map.get("id"));
            energy = Integer.parseInt(map.get("energy"));
            abs = Float.parseFloat(map.get("abs"));
            trans = Float.parseFloat(map.get("trans"));
            second = Integer.parseInt(map.get("second"));

            updatePeakChart(new TimeScanRecord(index, second, abs, trans,
                    energy, System.currentTimeMillis()));
        }
    }

    private void makeNormal(int id) {
        mPeakPoints.clear();
        mOperatePoints.clear();
        mPoints[id].clear();
        mChartData.setLines(mLines);
        mChartView.setLineChartData(mChartData);
        mAdapter.setData(mData[id]);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        for (int i = 0; i < mData[id].size(); i++) {
            int index = 0;
            int energy = 0;
            float abs = 0.0f;
            float trans = 0.0f;
            int second = 0;

            HashMap<String, String> map = mData[id].get(i);
            index = Integer.parseInt(map.get("id"));
            energy = Integer.parseInt(map.get("energy"));
            abs = Float.parseFloat(map.get("abs"));
            trans = Float.parseFloat(map.get("trans"));
            second = Integer.parseInt(map.get("second"));
            updateChart(id, new TimeScanRecord(index, second, abs, trans,
                    energy, System.currentTimeMillis()));
        }
    }


    private final int TIME_SCAN_START = 0x00;
    private final int TIME_SCAN_END = 0x01;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == TIME_SCAN_START) {
                mStopButton.setEnabled(true);
                mStartButton.setEnabled(false);
            } else if (msg.what == TIME_SCAN_END) {
                mStopButton.setEnabled(false);
                mStartButton.setEnabled(true);
            }
        }
    };

    @Subscribe
    public void OnSettingEvent(SettingEvent event) {
        Context context = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context = getContext();
        } else {
            context = getActivity();
        }

        Intent intent = new Intent(context, TimescanSettingActivity.class);
        startActivityForResult(intent, 0);
    }

    @Subscribe
    public void onFileOperateEvent(FileOperateEvent event) {
        if (event.op_type == FileOperateEvent.OP_EVENT_OPEN) {
            List<String> saveFileList = DeviceApplication.getInstance().getTimeScanDb().getTables();

            Utils.showItemSelectDialog(getActivity(), getString(R.string.action_open)
                    , saveFileList.toArray(new String[saveFileList.size()]), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            loadFileById(which);
                        }
                    });

        } else if (event.op_type == FileOperateEvent.OP_EVENT_SAVE) {
            if (mData[mCurDataIndex].size() < 1) {
                Toast.makeText(getActivity(), getString(R.string.notice_save_null), Toast.LENGTH_SHORT).show();
                return;
            }
            mSaveDialog.show(getFragmentManager(), "save");
        } else if (event.op_type == FileOperateEvent.OP_EVENT_PRINT) {

        }
    }

    private void loadFileById(int id) {
        List<String> fileList = DeviceApplication.getInstance().getTimeScanDb().getTables();
        String fileName = fileList.get(id);
        List<TimeScanRecord> lists = DeviceApplication.getInstance().getTimeScanDb().getRecords(fileName);
        clearData(mCurDataIndex);
        for (int i = 0; i < lists.size(); i++) {
            addItem(mCurDataIndex, lists.get(i));
            if (mTestMode == TimescanSettingActivity.TEST_MODE_ABS) {
                updateChart(mCurDataIndex, lists.get(i).getSecond(), lists.get(i).getAbs());
            } else if (mTestMode == TimescanSettingActivity.TEST_MODE_TRANS) {
                updateChart(mCurDataIndex, lists.get(i).getSecond(), lists.get(i).getTrans());
            }
        }
        Utils.needToSave = false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == TimescanSettingActivity.RESULT_OK) {
            Log.d(TAG, "OK");
            loadFromSetting();
            //set wavelength to target
            float work_wavelength = DeviceApplication.getInstance().getSpUtils().getTimescanWorkWavelength();
            if (!isFake) {
                ((MainActivity) getActivity()).loadWavelengthDialog(work_wavelength);
            }

        } else if (resultCode == TimescanSettingActivity.RESULT_CANCEL) {
            Log.d(TAG, "CANCEL");
        }
    }

    @Override
    public void onCalcSpeedInput(String start, String end, String calcRatio) {
        int start_time;
        int end_time;
        float ratio;
        float result = 0.0f;

        if(start.length() < 1 &&
                (end.length() < 1) &&
                (calcRatio.length() < 1)) {
            Toast.makeText(getActivity(), R.string.notice_edit_null, Toast.LENGTH_SHORT).show();
        } else {
            //get start, end, ratio
            start_time = Integer.parseInt(start);
            end_time = Integer.parseInt(end);
            ratio = Float.parseFloat(calcRatio);
            if (mTestMode == TimescanSettingActivity.TEST_MODE_ABS) {
                float start_val = Float.parseFloat(mData[mCurDataIndex].get(start_time).get("abs"));
                float end_val = Float.parseFloat(mData[mCurDataIndex].get(end_time).get("abs"));
                //per minute
                result = (end_val - start_val) / (end_time - start_time) * ratio / 60.0f;
            } else if (mTestMode == TimescanSettingActivity.TEST_MODE_TRANS) {
                float start_val = Float.parseFloat(mData[mCurDataIndex].get(start_time).get("trans"));
                float end_val = Float.parseFloat(mData[mCurDataIndex].get(end_time).get("trans"));
                //per minute
                result = (end_val - start_val) / (end_time - start_time) * ratio / 60.0f;
            }
            //show the result dialog
            Utils.showMessageDialog(getActivity(), getString(R.string.calc_speed_title), getString(R.string.calc_speed_result) + result);
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

            for (int i = 0; i <= duration; i += interval) {
                mX = i;
                if (!start) {
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
