package org.zhangjie.onlab.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import org.zhangjie.onlab.DeviceApplication;
import org.zhangjie.onlab.R;
import org.zhangjie.onlab.adapter.MultiSelectionAdapter;
import org.zhangjie.onlab.device.DeviceManager;
import org.zhangjie.onlab.dialog.SaveNameDialog;
import org.zhangjie.onlab.dialog.SettingEditDialog;
import org.zhangjie.onlab.otto.BusProvider;
import org.zhangjie.onlab.otto.FileOperateEvent;
import org.zhangjie.onlab.otto.RezeroEvent;
import org.zhangjie.onlab.otto.SettingEvent;
import org.zhangjie.onlab.otto.WavelengthScanCallbackEvent;
import org.zhangjie.onlab.record.PhotoMeasureRecord;
import org.zhangjie.onlab.record.TimeScanRecord;
import org.zhangjie.onlab.record.WavelengthScanRecord;
import org.zhangjie.onlab.setting.TimescanSettingActivity;
import org.zhangjie.onlab.setting.WavelengthSettingActivity;
import org.zhangjie.onlab.utils.SharedPreferenceUtils;
import org.zhangjie.onlab.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * Created by H151136 on 5/24/2016.
 */
public class WavelengthScanFragment extends Fragment implements View.OnClickListener {

    private boolean isFake = true;
    private static final String TAG = "Onlab.WavelengthScan";
    private static final int[] COLORS = {ChartUtils.DEFAULT_COLOR, ChartUtils.COLOR_ORANGE,
            ChartUtils.COLOR_BLUE, ChartUtils.COLOR_VIOLET};

    private ListView mListView;
    private MultiSelectionAdapter mAdapter;
    private List<HashMap<String, String>>[] mData;
    private List<HashMap<String, String>> mPeakData;

    private float mInterval;
    private float mStart;
    private float mEnd;
    private int mTestMode;
    private int mSpeed;

    private Button mCurrentButton;
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
    private Line[] mLine;
    private Line mPeakLine;
    private List<PointValue>[] mPoints;
    //    private List<PointValue> mPoints;
//    private List<PointValue> mPoints;
//    private List<PointValue> mPoints;
    private List<PointValue> mPeakPoints;
    //----
    private SaveNameDialog mSaveDialog;

    private int loadFileIndex = -1;
    private boolean loadFile = false;

    private SettingEditDialog mPeakDialog;
    private int mCurDataIndex = 0;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wavelength_scan, container, false);
        Utils.needToSave = false;
        mCurDataIndex = 0;
        initUi(view);
        mPeakDialog = new SettingEditDialog();
        return view;
    }

    private void initUi(View view) {
        mCurrentButton = (Button) view.findViewById(R.id.bt_wavelength_scan_current);
        mStartButton = (Button) view.findViewById(R.id.bt_wavelength_scan_start);
        mStopButton = (Button) view.findViewById(R.id.bt_wavelength_scan_stop);
        mRezeroButton = (Button) view.findViewById(R.id.bt_wavelength_scan_rezero);
        mProcessButton = (Button) view.findViewById(R.id.bt_wavelength_scan_process);
        mStartButton.setOnClickListener(this);
        mStopButton.setOnClickListener(this);
        mRezeroButton.setOnClickListener(this);
        mProcessButton.setOnClickListener(this);

        mListView = (ListView) view.findViewById(R.id.lv_wavelength_scan);

        mData = new List[4];

        mData[0] = new ArrayList<HashMap<String, String>>();
        mData[1] = new ArrayList<HashMap<String, String>>();
        mData[2] = new ArrayList<HashMap<String, String>>();
        mData[3] = new ArrayList<HashMap<String, String>>();
        mPeakData = new ArrayList<HashMap<String, String>>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mAdapter = new MultiSelectionAdapter(getContext(), mData[mCurDataIndex],
                    R.layout.item_wavelength_scan,
                    new String[]{"id", "wavelength", "abs", "trans", "energy"},
                    new int[]{R.id.item_index, R.id.item_wavelength, R.id.item_abs, R.id.item_trans, R.id.item_energy});
        } else {
            mAdapter = new MultiSelectionAdapter(getActivity(), mData[mCurDataIndex],
                    R.layout.item_wavelength_scan,
                    new String[]{"id", "wavelength", "abs", "trans", "energy"},
                    new int[]{R.id.item_index, R.id.item_wavelength, R.id.item_abs, R.id.item_trans, R.id.item_energy});
        }
        mListView.setAdapter(mAdapter);
//        mAdapter.registerDataSetObserver(new DataSetObserver() {
//            @Override
//            public void onChanged() {
//                super.onChanged();
//                if (mData.size() > 0) {
//                    Utils.needToSave = true;
//                } else {
//                    Utils.needToSave = false;
//                }
//            }
//        });

        mTestModeTextView = (TextView) view.findViewById(R.id.tv_wavelength_scan_test_mode);
//        mSmoothCheckBox = (CheckBox) view.findViewById(R.id.check_smooth);
//        mSmoothCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                mLine.setCubic(isChecked);
//                mChartData.setLines(mLines);
//                mChartView.setLineChartData(mChartData);
//            }
//        });

        mChartView = (LineChartView) view.findViewById(R.id.hello_wavelength_scan);
        initChart();
        loadFromSetting();
        if (isFake) {
            mStartButton.setEnabled(true);
            mStopButton.setEnabled(true);
        } else {
            mStartButton.setEnabled(false);
            mStopButton.setEnabled(false);
        }
        mSaveDialog = new SaveNameDialog();
        mSaveDialog.init(-1, getString(R.string.action_save), getString(R.string.name), new SaveNameDialog.SettingInputListern() {
            @Override
            public void onSettingInputComplete(int id, String name) {

                if (name.length() < 1 || (!Utils.isValidName(name))) {
                    Toast.makeText(getActivity(), getString(R.string.notice_name_invalid), Toast.LENGTH_SHORT).show();
                    return;
                }
                List<String> saveFileList = DeviceApplication.getInstance().getWavelengthScanDb().getTables();
                Log.d(TAG, "Alread saved -> " + saveFileList.size() + " files.");

                for (int i = 0; i < saveFileList.size(); i++) {
                    Log.d(TAG, String.format("[%d] -> %s\n", i, saveFileList.get(i)));
                }
                String fileName = name;
                for (int i = 0; i < mData[mCurDataIndex].size(); i++) {
                    int index = 0;
                    float wavelength = 0;
                    float abs = 0.0f;
                    float trans = 0.0f;
                    int energy = 0;
                    long date = 0;

                    HashMap<String, String> map = mData[mCurDataIndex].get(i);
                    index = Integer.parseInt(map.get("id"));
                    wavelength = Float.parseFloat(map.get("wavelength"));
                    abs = Float.parseFloat(map.get("abs"));
                    trans = Float.parseFloat(map.get("trans"));
                    energy = Integer.parseInt(map.get("energy"));
                    date = Long.parseLong(map.get("date"));

                    WavelengthScanRecord record = new WavelengthScanRecord(index, wavelength, abs, trans, energy, date);
                    DeviceApplication.getInstance().getWavelengthScanDb().saveRecord(fileName, record);
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
        mLines = new ArrayList<Line>();

        mLine = new Line[4];

        mLine[0] = new Line(mPoints[0]).setColor(COLORS[0]).setCubic(true);
        mLine[1] = new Line(mPoints[1]).setColor(COLORS[1]).setCubic(true);
        mLine[2] = new Line(mPoints[2]).setColor(COLORS[2]).setCubic(true);
        mLine[3] = new Line(mPoints[3]).setColor(COLORS[3]).setCubic(true);

        mPeakLine = new Line(mPeakPoints).setColor(ChartUtils.COLOR_GREEN).setCubic(true);

        for (int i = 0; i < mLine.length; i++) {
            mLine[i].setPointRadius(1);
            mLine[i].setStrokeWidth(1);
            mLine[i].setCubic(false);
        }

        mPeakLine.setPointRadius(3);
        mPeakLine.setStrokeWidth(1);
        for (int i = 0; i < mLine.length; i++) {
            mLines.add(mLine[i]);
        }
        mLines.add(mPeakLine);

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

        if (mode == WavelengthSettingActivity.TEST_MODE_ABS) {
            updateXYTitle(getString(R.string.wavelength_with_unit), getString(R.string.abs_with_unit),
                    start, end, limit_up, limit_down);
            mTestModeTextView.setText(getString(R.string.mode) + ": " + getString(R.string.abs));
        } else if (mode == WavelengthSettingActivity.TEST_MODE_TRANS) {
            updateXYTitle(getString(R.string.wavelength_with_unit), getString(R.string.trans_with_unit),
                    start, end, limit_up, limit_down);
            mTestModeTextView.setText(getString(R.string.mode) + ": " + getString(R.string.trans));
        } else if (mode == WavelengthSettingActivity.TEST_MODE_ENERGY) {
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

    private void addItem(int index, WavelengthScanRecord record) {
        HashMap<String, String> item = new HashMap<String, String>();
        int no = mData[index].size() + 1;
        record.setIndex(no);

        item.put("id", "" + no);
        item.put("wavelength", String.format("%.1f", record.getWavelength()));
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

    private void updateChart(int index, WavelengthScanRecord record) {
        int mode = DeviceApplication.getInstance().getSpUtils().getWavelengthscanTestMode();
        if (mode == WavelengthSettingActivity.TEST_MODE_ABS) {
            mPoints[index].add(new PointValue(record.getWavelength(), record.getAbs()));
        } else if (mode == WavelengthSettingActivity.TEST_MODE_TRANS) {
            mPoints[index].add(new PointValue(record.getWavelength(), record.getTrans()));
        } else if (mode == WavelengthSettingActivity.TEST_MODE_ENERGY) {
            mPoints[index].add(new PointValue(record.getWavelength(), record.getEnergy()));
        }
        mChartData.setLines(mLines);
        mChartView.setLineChartData(mChartData);
    }

    private void updateChart2(WavelengthScanRecord record) {
        int mode = DeviceApplication.getInstance().getSpUtils().getWavelengthscanTestMode();
        if (mode == WavelengthSettingActivity.TEST_MODE_ABS) {
            mPeakPoints.add(new PointValue(record.getWavelength(), record.getAbs()));
        } else if (mode == WavelengthSettingActivity.TEST_MODE_TRANS) {
            mPeakPoints.add(new PointValue(record.getWavelength(), record.getTrans()));
        } else if (mode == WavelengthSettingActivity.TEST_MODE_ENERGY) {
            mPeakPoints.add(new PointValue(record.getWavelength(), record.getEnergy()));
        }
        mPeakLine.setHasPoints(true);
        mPeakLine.setHasLines(false);
        if (!mLines.contains(mPeakLine)) {
            mLines.add(mPeakLine);
        }
        mChartData.setLines(mLines);
        mChartView.setLineChartData(mChartData);
    }

    private void clearData(int index) {
        mData[index].clear();
        mAdapter.notifyDataSetChanged();
        mPoints[index].clear();
        mChartData.setLines(mLines);
        mChartView.setLineChartData(mChartData);
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
        Utils.needToSave = false;
        loadFile = false;
    }

    @Subscribe
    public void OnSettingEvent(SettingEvent event) {
        Context context = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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

        if (resultCode == WavelengthSettingActivity.RESULT_OK) {
            Log.d(TAG, "OK");
            loadFromSetting();
        } else if (resultCode == WavelengthSettingActivity.RESULT_CANCEL) {
            Log.d(TAG, "CANCEL");
        }
    }


    @Subscribe
    public void onFileOperateEvent(FileOperateEvent event) {
//        if (event.op_type == FileOperateEvent.OP_EVENT_OPEN) {
//            List<String> saveFileList = DeviceApplication.getInstance().getWavelengthScanDb().getTables();
//
//            Utils.showItemSelectDialog(getActivity(), getString(R.string.action_open)
//                    , saveFileList.toArray(new String[saveFileList.size()]), new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            loadFileById(which);
//                        }
//                    });
//
//        } else if (event.op_type == FileOperateEvent.OP_EVENT_SAVE) {
//            if (mData[mCurDataIndex].size() < 1) {
//                Toast.makeText(getActivity(), getString(R.string.notice_save_null), Toast.LENGTH_SHORT).show();
//                return;
//            }
//            mSaveDialog.show(getFragmentManager(), "save");
//        } else if (event.op_type == FileOperateEvent.OP_EVENT_PRINT) {
//
//        }
    }

    private void loadFileById(int id) {
//        List<String> fileList = DeviceApplication.getInstance().getWavelengthScanDb().getTables();
//        String fileName = fileList.get(id);
//        List<WavelengthScanRecord> lists = DeviceApplication.getInstance().getWavelengthScanDb().getRecords(fileName);
//        clearData();
//        for (int i = 0; i < lists.size(); i++) {
//            addItem(lists.get(i));
//            updateChart(lists.get(i));
//        }
//        Utils.needToSave = false;

    }

    @Subscribe
    public void OnEventCallback(WavelengthScanCallbackEvent event) {
        if (event.event_type == WavelengthScanCallbackEvent.EVENT_TYPE_REZERO_DONE) {
            mStartButton.setEnabled(true);
        } else if (event.event_type == WavelengthScanCallbackEvent.EVENT_TYPE_WORKING) {

            WavelengthScanRecord record = new WavelengthScanRecord(-1,
                    event.wavelength, event.abs, event.trans, event.energy,
                    System.currentTimeMillis());
            addItem(mCurDataIndex, record);
            updateChart(mCurDataIndex, record);
        } else if (event.event_type == WavelengthScanCallbackEvent.EVENT_TYPE_WORK_DONE) {
            mStartButton.setEnabled(true);
            mStopButton.setEnabled(false);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_wavelength_scan_current:
                break;
            case R.id.bt_wavelength_scan_start:
                if (isFake) {

                    for (int i = 3; i >= 0; i--) {
                        if (mData[i].size() == 0) {
                            mCurDataIndex = i;
                        }
                    }
                    setCurrentButton();
                    //set adapter data
                    mAdapter.setData(mData[mCurDataIndex]);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            SharedPreferenceUtils sp = DeviceApplication.getInstance().getSpUtils();

                            float start = sp.getWavelengthscanStart();
                            float end = sp.getWavelengthscanEnd();
                            int speed = sp.getWavelengthscanSpeed();
                            float interval = sp.getWavelengthscanInterval();
                            int count = (int) ((end - start) / interval);
                            while (count > mData[mCurDataIndex].size()) {
                                int energy = (int) (Math.random() * 1000.0f);
                                float wavelength = end - mData[mCurDataIndex].size() * interval;
                                float abs = (float) (Math.random() * 2);
                                float trans = (float) (Math.random() * 100);

                                WavelengthScanRecord record = new WavelengthScanRecord(-1,
                                        wavelength, abs, trans, energy,
                                        System.currentTimeMillis());
                                addItem(mCurDataIndex, record);
                                updateChart(mCurDataIndex, record);
                            }
                        }

                    });

                } else {
                    clearData(mCurDataIndex);
                    SharedPreferenceUtils sp = DeviceApplication.getInstance().getSpUtils();

                    float start = sp.getWavelengthscanStart();
                    float end = sp.getWavelengthscanEnd();
                    int speed = sp.getWavelengthscanSpeed();
                    float interval = sp.getWavelengthscanInterval();

                    DeviceManager.getInstance().doWavelengthScan(start, end, interval);
                    mStartButton.setEnabled(false);
                    mStopButton.setEnabled(true);
                }
                break;
            case R.id.bt_wavelength_scan_stop:
                mStartButton.setEnabled(true);
                mStopButton.setEnabled(false);
                DeviceManager.getInstance().stopWork();
                break;
            case R.id.bt_wavelength_scan_rezero:
                SharedPreferenceUtils sp = DeviceApplication.getInstance().getSpUtils();

                float start = sp.getWavelengthscanStart();
                float end = sp.getWavelengthscanEnd();
                int speed = sp.getWavelengthscanSpeed();
                float interval = sp.getWavelengthscanInterval();
                BusProvider.getInstance().post(new RezeroEvent(start, end, speed, interval));
                break;
            case R.id.bt_wavelength_scan_process:
                showProcessDialog();
                break;
            default:
                break;
        }
    }

    private void setCurrentButton() {
        mCurrentButton.setText(getString(R.string.current) +
                ": " + getString(R.string.line) + "" + (mCurDataIndex + 1));
        mCurrentButton.setTextColor(COLORS[mCurDataIndex]);
    }

    private final int PROCESS_ITEM_CUBIC = 0;
    private final int PROCESS_ITEM_PEAK = 1;
    private final int PROCESS_ITEM_DERIVATIVE = 2;
    private final int PROCESS_ITEM_OPERATION = 3;

    private void showProcessDialog() {
        String[] items = getResources().getStringArray(R.array.processings);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        int count = items.length;
        boolean[] select = new boolean[count];
        if (mLine[mCurDataIndex].isCubic()) {
            select[PROCESS_ITEM_CUBIC] = true;
        }
        if (mPeakPoints.size() > 0) {
            select[PROCESS_ITEM_PEAK] = true;
        }

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
                            makePeak(mCurDataIndex, 1);
                        } else {
                            removePeak();
                            makeNormal(mCurDataIndex);
                        }
                        break;
                    case PROCESS_ITEM_DERIVATIVE:
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
                                        if (selectCount != 2) {
                                            Toast.makeText(getActivity(), getString(R.string.notice_select_2), Toast.LENGTH_SHORT).show();
                                            return;
                                        } else {
                                            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                            builder.setTitle(getString(R.string.select_operation));
                                            builder.setIcon(R.mipmap.ic_launcher);
                                            builder.setSingleChoiceItems(getResources().getStringArray(R.array.operations), 0, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Toast.makeText(getActivity(), getResources().getStringArray(R.array.operations)[which],
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                            builder.setPositiveButton(R.string.ok_string, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
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
                        }
                        break;

                    default:
                        break;
                }
            }
        });

        builder.create().show();
    }

    private void removePeak() {
        mPeakPoints.clear();
        mChartData.setLines(mLines);
        mChartView.setLineChartData(mChartData);
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
            float wavelength = 0.0f;

            HashMap<String, String> map = mData[id].get(i);
            index = Integer.parseInt(map.get("id"));
            energy = Integer.parseInt(map.get("energy"));
            abs = Float.parseFloat(map.get("abs"));
            trans = Float.parseFloat(map.get("trans"));
            wavelength = Float.parseFloat(map.get("wavelength"));
            int mode = DeviceApplication.getInstance().getSpUtils().getWavelengthscanTestMode();
            if (mode == WavelengthSettingActivity.TEST_MODE_ABS) {
                data[i] = abs;
            } else if (mode == WavelengthSettingActivity.TEST_MODE_TRANS) {
                data[i] = trans;
            } else if (mode == WavelengthSettingActivity.TEST_MODE_ENERGY) {
                data[i] = energy;
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
            float wavelength = 0.0f;

            HashMap<String, String> map = mPeakData.get(i);
            index = Integer.parseInt(map.get("id"));
            energy = Integer.parseInt(map.get("energy"));
            abs = Float.parseFloat(map.get("abs"));
            trans = Float.parseFloat(map.get("trans"));
            wavelength = Float.parseFloat(map.get("wavelength"));
            updateChart2(new WavelengthScanRecord(index, wavelength, abs, trans,
                    energy, System.currentTimeMillis()));
        }
    }

    private void makeNormal(int id) {
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
            float wavelength = 0.0f;

            HashMap<String, String> map = mData[id].get(i);
            index = Integer.parseInt(map.get("id"));
            energy = Integer.parseInt(map.get("energy"));
            abs = Float.parseFloat(map.get("abs"));
            trans = Float.parseFloat(map.get("trans"));
            wavelength = Float.parseFloat(map.get("wavelength"));
            updateChart(id, new WavelengthScanRecord(index, wavelength, abs, trans,
                    energy, System.currentTimeMillis()));
        }
    }
}
