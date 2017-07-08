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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import org.zhangjie.onlab.DeviceApplication;
import org.zhangjie.onlab.MainActivity;
import org.zhangjie.onlab.R;
import org.zhangjie.onlab.adapter.MultiSelectionAdapter;
import org.zhangjie.onlab.device.DeviceManager;
import org.zhangjie.onlab.dialog.FileExportDialog;
import org.zhangjie.onlab.dialog.SaveNameDialog;
import org.zhangjie.onlab.dialog.SettingEditDialog;
import org.zhangjie.onlab.otto.BusProvider;
import org.zhangjie.onlab.otto.CancelEvent;
import org.zhangjie.onlab.otto.FileOperateEvent;
import org.zhangjie.onlab.otto.RezeroEvent;
import org.zhangjie.onlab.otto.SettingEvent;
import org.zhangjie.onlab.otto.WavelengthScanCallbackEvent;
import org.zhangjie.onlab.otto.WavelengthScanCancelEvent;
import org.zhangjie.onlab.record.PhotoMeasureRecord;
import org.zhangjie.onlab.record.TimeScanRecord;
import org.zhangjie.onlab.record.WavelengthScanRecord;
import org.zhangjie.onlab.setting.TimescanSettingActivity;
import org.zhangjie.onlab.setting.WavelengthSettingActivity;
import org.zhangjie.onlab.utils.SharedPreferenceUtils;
import org.zhangjie.onlab.utils.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
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
    private boolean isFake = false;
    private static final String TAG = "Onlab.WavelengthScan";
    private static final int LINE_MAX = 30;

    private ListView mListView;
    private MultiSelectionAdapter mAdapter;
    private List<HashMap<String, String>>[] mData;
    private List<HashMap<String, String>> mPeakData;
    private List<HashMap<String, String>> mOperateData;

    private float mInterval;
    private float mStart;
    private float mEnd;
    private int mTestMode;
    private int mSpeed;

    private Button mCurrentButton;
    private Button mStartButton;
    private Button mStopButton;
    private Button mClearButton;
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
    private Line mOperateLine;
    private Line mDerivativeLine;
    private List<PointValue>[] mPoints;
    //    private List<PointValue> mPoints;
//    private List<PointValue> mPoints;
//    private List<PointValue> mPoints;
    private List<PointValue> mPeakPoints;
    private List<PointValue> mOperatePoints;
    private List<PointValue> mDerivativePoints;

    //----
    private SaveNameDialog mSaveDialog;

    private int loadFileIndex = -1;
    private boolean loadFile = false;

    private int mCurDataIndex = 0;
    private int mFileType = FileExportDialog.FILE_TYPE_TXT;
    private FileExportDialog mFileExportDialog;
    private int mSaveCount;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wavelength_scan, container, false);
        Utils.needToSave = false;
        mCurDataIndex = 0;
        initUi(view);
        return view;
    }

    private Handler mNextSaveDialog = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mSaveDialog.setTitile(getString(R.string.action_save ) + " " + getString(R.string.line) + (mSaveCount + 1));
            mSaveDialog.show(getFragmentManager(), "save");
        }
    };

    private void initUi(View view) {
        mCurrentButton = (Button) view.findViewById(R.id.bt_wavelength_scan_current);
        mStartButton = (Button) view.findViewById(R.id.bt_wavelength_scan_start);
        mStopButton = (Button) view.findViewById(R.id.bt_wavelength_scan_stop);
        mClearButton = (Button) view.findViewById(R.id.bt_wavelength_scan_clear);
        mRezeroButton = (Button) view.findViewById(R.id.bt_wavelength_scan_rezero);
        mProcessButton = (Button) view.findViewById(R.id.bt_wavelength_scan_process);
        mCurrentButton.setOnClickListener(this);
        mStartButton.setOnClickListener(this);
        mStopButton.setOnClickListener(this);
        mRezeroButton.setOnClickListener(this);
        mProcessButton.setOnClickListener(this);
        mClearButton.setOnClickListener(this);

        mListView = (ListView) view.findViewById(R.id.lv_wavelength_scan);

        mData = new List[LINE_MAX];
        for (int i = 0; i < LINE_MAX; i++) {
            mData[i] = new ArrayList<HashMap<String, String>>();
        }
        mPeakData = new ArrayList<HashMap<String, String>>();
        mOperateData = new ArrayList<HashMap<String, String>>();
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

        mTestModeTextView = (TextView) view.findViewById(R.id.tv_wavelength_scan_test_mode);

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
                Log.d(TAG, "save to -> " + fileName);

                DeviceApplication.getInstance().getWavelengthScanDb().saveRecord(fileName, mData[mSaveCount++]);
                if(mData[mSaveCount].size() > 0) {
                    //show next save dialog later
                    mNextSaveDialog.sendEmptyMessageDelayed(0, 300);
                }
                Utils.needToSave = false;
            }

            @Override
            public void abort() {
                getFragmentManager().popBackStack();
            }
        });
        mFileExportDialog = new FileExportDialog();
        mFileExportDialog.init(getString(R.string.action_file_export), getString(R.string.name), new FileExportDialog.SettingInputListern() {
            @Override
            public void onSettingInputComplete(String name) {
                if(name.length() < 1 || (!Utils.isValidName(name))) {
                    Toast.makeText(getActivity(), getString(R.string.notice_name_invalid), Toast.LENGTH_SHORT).show();
                    return;
                }
                String typeString = "unknow";
                String titleFormatString = "";
                String contentFormatString = "";
                if(mFileType == FileExportDialog.FILE_TYPE_TXT) {
                    typeString = "txt";
                    titleFormatString = "%s\t%s\t%s\t%s\n";
                    contentFormatString = "%d\t%f\t%f\t%d\n";
                } else if(mFileType ==FileExportDialog.FILE_TYPE_CVS) {
                    typeString = "cvs";
                    titleFormatString = "%s,%s,%s,%s\n";
                    contentFormatString = "%d,%f,%f,%d\n";
                }
                for(int index = 0; index < LINE_MAX; index++) {
                    if(mData[index].size() == 0)
                        continue;
                    File file = Utils.getWavelengthScanFile(name + "__" + (index + 1) + "." + typeString );
                    try {
                        FileWriter out = new FileWriter(file, false);
                        BufferedWriter writer = new BufferedWriter(out);
                        String line = String.format(titleFormatString,
                                getString(R.string.index),
                                getString(R.string.abs),
                                getString(R.string.trans),
                                getString(R.string.energy));
                        writer.write(line);
                        for (int i = 0; i < mData[index].size(); i++) {
                            int id = 0;
                            float abs = 0.0f;
                            float trans = 0.0f;
                            int energy = 0;

                            HashMap<String, String> map = mData[index].get(i);
                            id = Integer.parseInt(map.get("id"));
                            abs = Float.parseFloat(map.get("abs"));
                            trans = Float.parseFloat(map.get("trans"));
                            energy = Integer.parseInt(map.get("energy"));
                            line = String.format(contentFormatString,
                                    id, abs, trans, energy);
                            writer.write(line);
                        }
                        writer.flush();
                        writer.close();
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onFileTypeSelect(int type) {
                mFileType = type;
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
        mPoints = new List[LINE_MAX];
        for (int i = 0; i < LINE_MAX; i++) {
            mPoints[i] = new ArrayList<PointValue>();
        }
        mPeakPoints = new ArrayList<PointValue>();
        mOperatePoints = new ArrayList<PointValue>();
        mDerivativePoints = new ArrayList<PointValue>();

        mLines = new ArrayList<Line>();

        mLine = new Line[LINE_MAX];

        for (int i = 0; i < LINE_MAX; i++) {
            mLine[i] = new Line(mPoints[i]).setColor(Utils.COLORS[i % 4]).setCubic(true);
        }
        mPeakLine = new Line(mPeakPoints).setColor(ChartUtils.COLOR_GREEN).setCubic(true);
        mOperateLine = new Line(mOperatePoints).setColor(ChartUtils.COLOR_RED).setCubic(true);
        mDerivativeLine = new Line(mDerivativePoints).setColor(Color.YELLOW).setCubic(false);

        for (int i = 0; i < mLine.length; i++) {
            mLine[i].setPointRadius(0);
            mLine[i].setStrokeWidth(1);
            mLine[i].setCubic(false);
            mLine[i].setHasPoints(false);
        }

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

        mChartView.setPadding(0, 50, 50, 0);
        mChartView.setMaximumViewport(viewport);
        mChartView.setCurrentViewport(viewport);
        mChartView.setViewportCalculationEnabled(false);

        int s1, s2, s3, s4, s5;

        List<AxisValue> axisXValues = new ArrayList<>();
        if(right - left > 1) {
            s1 = (int) left;
            s2 = (int) ((left + right) / 4);
            s3 = (int) ((left + right) / 2);
            s4 = (int) ((left + right) * 3 / 4);
            s5 = (int) right;
            axisXValues.add(new AxisValue(s1));
            axisXValues.add(new AxisValue(s2));
            axisXValues.add(new AxisValue(s3));
            axisXValues.add(new AxisValue(s4));
            axisXValues.add(new AxisValue(s5));
        }
        List<AxisValue> axisYValues = new ArrayList<>();
        if(top - bottom > 1) {
            s1 = (int) bottom;
            s2 = (int) ((top + bottom) / 4);
            s3 = (int) ((top + bottom) / 2);
            s4 = (int) ((top + bottom) * 3 / 4);
            s5 = (int) top;
            axisYValues.add(new AxisValue(s1));
            axisYValues.add(new AxisValue(s2));
            axisYValues.add(new AxisValue(s3));
            axisYValues.add(new AxisValue(s4));
            axisYValues.add(new AxisValue(s5));
        }
        Axis axisX;
        if(axisXValues.size() > 0) {
            axisX = new Axis(axisXValues);
        } else {
            axisX = new Axis();
        }
        Axis axisY;
        if(axisYValues.size() > 0) {
            axisY =new Axis(axisYValues);
        } else {
            axisY = new Axis();
        }
        axisX.setName(xTitle);
        axisX.setHasSeparationLine(true);
//        axisX.setHasLines(true);
        axisY.setName(yTitle);
        axisY.setHasSeparationLine(true);
//        axisY.setHasLines(true);
        mChartData.setAxisXBottom(axisX);
        mChartData.setAxisYLeft(axisY);
        List<AxisValue> nullValues = new ArrayList<>();
        Axis nullAxis = new Axis(nullValues);
        nullAxis.setName("");
        nullAxis.setInside(false);
        mChartData.setAxisXTop(nullAxis);
        mChartData.setAxisYRight(nullAxis);
    }

    private void addItem(int index, WavelengthScanRecord record) {
        HashMap<String, String> item = new HashMap<String, String>();
        int no = mData[index].size() + 1;
        record.setIndex(no);

        item.put("id", "" + no);
        item.put("wavelength", String.format("%.1f", record.getWavelength()));
        item.put("abs", "" + record.getAbs());
        item.put("trans", "" + record.getTrans());
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

    private void updatePeakChart(WavelengthScanRecord record) {
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

    private void updateOperateChart(WavelengthScanRecord record) {
        int mode = DeviceApplication.getInstance().getSpUtils().getWavelengthscanTestMode();
        if (mode == WavelengthSettingActivity.TEST_MODE_ABS) {
            mOperatePoints.add(new PointValue(record.getWavelength(), record.getAbs()));
        } else if (mode == WavelengthSettingActivity.TEST_MODE_TRANS) {
            mOperatePoints.add(new PointValue(record.getWavelength(), record.getTrans()));
        } else if (mode == WavelengthSettingActivity.TEST_MODE_ENERGY) {
            mOperatePoints.add(new PointValue(record.getWavelength(), record.getEnergy()));
        }
        mOperateLine.setHasPoints(true);
        mOperateLine.setHasLines(true);
        if (!mLines.contains(mOperateLine)) {
            mLines.add(mOperateLine);
        }
        mChartData.setLines(mLines);
        mChartView.setLineChartData(mChartData);
    }

    private void updateDerivativeChart(float x, float y) {
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

    private void clearTable(int index) {
        mData[index].clear();
        mAdapter.notifyDataSetChanged();
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
        if (event.op_type == FileOperateEvent.OP_EVENT_OPEN) {
            List<String> saveFileList = DeviceApplication.getInstance().getWavelengthScanDb().getTables();

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
            mSaveDialog.setTitile(getString(R.string.action_save ) + " " + getString(R.string.line) + "1");
            mSaveCount = 0;
            mSaveDialog.show(getFragmentManager(), "save");
        } else if (event.op_type == FileOperateEvent.OP_EVENT_PRINT) {

        } else if(event.op_type == FileOperateEvent.OP_EVENT_FILE_EXPORT) {

        } else if(event.op_type == FileOperateEvent.OP_EVENT_REZERO) {
            //check baseline is existed
            if(!DeviceApplication.getInstance().getSpUtils().getBaselineAvailable()) {
                Toast.makeText(getActivity(), R.string.notice_baseline_null, Toast.LENGTH_SHORT).show();
                return;
            }
            int [] baseline = DeviceApplication.getInstance().getSpUtils()
                    .getBaseline((int) (DeviceManager.BASELINE_END - DeviceManager.BASELINE_START + 1));
            for(int i = 0; i < (int) (DeviceManager.BASELINE_END - DeviceManager.BASELINE_START + 1); i++) {
                if(baseline[i] <= 0) {
                    Toast.makeText(getActivity(), R.string.notice_baseline_null, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            SharedPreferenceUtils sp = DeviceApplication.getInstance().getSpUtils();

            float start = sp.getWavelengthscanStart();
            float end = sp.getWavelengthscanEnd();
            int speed = sp.getWavelengthscanSpeed();
            float interval = sp.getWavelengthscanInterval();
            BusProvider.getInstance().post(new RezeroEvent(start, end, speed, interval));

        } else if(event.op_type == FileOperateEvent.OP_EVENT_START_TEST) {
            if (isFake) {
                for (int i = LINE_MAX - 1; i >= 0; i--) {
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
                int availables = 0;
                for (int i = LINE_MAX - 1; i >= 0; i--) {
                    if (mData[i].size() == 0) {
                        mCurDataIndex = i;
                    } else {
                        availables++;
                    }
                }
                if (availables == LINE_MAX) {
                    clearData(mCurDataIndex);
                }
                setCurrentButton();
                //set adapter data
                mAdapter.setData(mData[mCurDataIndex]);
                SharedPreferenceUtils sp = DeviceApplication.getInstance().getSpUtils();

                float start = sp.getWavelengthscanStart();
                float end = sp.getWavelengthscanEnd();
                int speed = sp.getWavelengthscanSpeed();
                float interval = sp.getWavelengthscanInterval();

                ((MainActivity) (getActivity())).loadWavelengthDialog(end);
                DeviceManager.getInstance().doWavelengthScan(start, end, interval);
                mStartButton.setEnabled(false);
                mStopButton.setEnabled(true);
            }

        }
    }

    private void loadFileById(int id) {
        List<String> fileList = DeviceApplication.getInstance().getWavelengthScanDb().getTables();
        String fileName = fileList.get(id);
        List<WavelengthScanRecord> lists = DeviceApplication.getInstance().getWavelengthScanDb().getRecords(fileName);
        if(mData[mCurDataIndex].size() > 0) {
            mCurDataIndex ++;
        }
        setCurrentButton();
        for (int i = 0; i < lists.size(); i++) {
            addItem(mCurDataIndex, lists.get(i));
            updateChart(mCurDataIndex, lists.get(i));
        }
        Utils.needToSave = false;
    }

    @Subscribe
    public void OnCancelEvent(CancelEvent event) {
        BusProvider.getInstance().post(new WavelengthScanCancelEvent());
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
                Log.d(TAG, "show current lines");
                showCurrentLines();
                break;
            case R.id.bt_wavelength_scan_start:
                if (isFake) {
                    for (int i = LINE_MAX - 1; i >= 0; i--) {
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
                    int availables = 0;
                    for (int i = LINE_MAX - 1; i >= 0; i--) {
                        if (mData[i].size() == 0) {
                            mCurDataIndex = i;
                        } else {
                            availables++;
                        }
                    }
                    if (availables == LINE_MAX) {
                        clearData(mCurDataIndex);
                    }
                    setCurrentButton();
                    //set adapter data
                    mAdapter.setData(mData[mCurDataIndex]);
                    SharedPreferenceUtils sp = DeviceApplication.getInstance().getSpUtils();

                    float start = sp.getWavelengthscanStart();
                    float end = sp.getWavelengthscanEnd();
                    int speed = sp.getWavelengthscanSpeed();
                    float interval = sp.getWavelengthscanInterval();

                    ((MainActivity) (getActivity())).loadWavelengthDialog(end);

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
            case R.id.bt_wavelength_scan_clear:
                int availables = 0;
                for (int i = 0; i < LINE_MAX; i++) {
                    if (mData[i].size() > 0) {
                        availables++;
                    }
                }
                if(availables > 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.notice);
                    builder.setMessage(R.string.sure_to_delete);
                    builder.setPositiveButton(R.string.ok_string, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            selectIndex = 0;
                            for (int i = 0; i < mData.length; i++) {
                                mData[i].clear();
                            }
                            mCurDataIndex = 0;
                            for (int i = 0; i < LINE_MAX; i++) {
                                makeNormal(i);
                            }
                        }
                    });
                    builder.setNegativeButton(R.string.cancel_string, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder.setCancelable(false);
                    builder.create().show();
                }
                break;
            case R.id.bt_wavelength_scan_rezero:
                //check baseline is existed
                if(!DeviceApplication.getInstance().getSpUtils().getBaselineAvailable()) {
                    Toast.makeText(getActivity(), R.string.notice_baseline_null, Toast.LENGTH_SHORT).show();
                    return;
                }
                int [] baseline = DeviceApplication.getInstance().getSpUtils()
                        .getBaseline((int) (DeviceManager.BASELINE_END - DeviceManager.BASELINE_START + 1));
                for(int i = 0; i < (int) (DeviceManager.BASELINE_END - DeviceManager.BASELINE_START + 1); i++) {
                    if(baseline[i] <= 0) {
                        Toast.makeText(getActivity(), R.string.notice_baseline_null, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
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

    private int selectIndex;
    private void showCurrentLines() {
        int avaliables = 0;
        List<String> lineItems = new ArrayList<String>();
        final int[] indicates = new int[LINE_MAX];
        int index = 0;
        for (int i = 0; i < LINE_MAX; i++) {
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
                selectIndex = which;
            }
        });
        builder.setNeutralButton(getString(R.string.action_delete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getActivity(), "delete " + mCurDataIndex, Toast.LENGTH_SHORT).show();
                mData[selectIndex].clear();
                if(mCurDataIndex == selectIndex)
                    mCurDataIndex = 0;
                showCurrentLines();
                setCurrentButton();
                for(int i = 0; i < LINE_MAX; i++) {
                    makeNormal(i);
                }
            }
        });
        builder.create().show();
    }

    private void setCurrentButton() {
        mCurrentButton.setText(getString(R.string.current) +
                ": " + getString(R.string.line) + "" + (mCurDataIndex + 1));
        mCurrentButton.setTextColor(Utils.COLORS[mCurDataIndex % 4]);
    }

    private final int PROCESS_ITEM_CUBIC = 0;
    private final int PROCESS_ITEM_PEAK = 1;
    private final int PROCESS_ITEM_DERIVATIVE = 2;
    private final int PROCESS_ITEM_OPERATION = 3;

    private int mOperateType = 0;
    private boolean mOperateSelect = false;

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
        if (mDerivativePoints.size() > 0) {
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
                        if (isChecked) {
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
                            for (int i = 0; i < LINE_MAX; i++) {
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

                    default:
                        break;
                }
            }
        });

        builder.create().show();
    }


    private void makeDerivative(int id) {
        int total = mData[id].size();

        if (total == 0) {
            return;
        }

        float[] yy = new float[total];
        float[] x = new float[total];

        for (int i = 0; i < total; i++) {
            int energy = 0;
            float abs = 0.0f;
            float trans = 0.0f;
            float wavelength = 0.0f;

            HashMap<String, String> map = mData[id].get(i);
            energy = Integer.parseInt(map.get("energy"));
            abs = Float.parseFloat(map.get("abs"));
            trans = Float.parseFloat(map.get("trans"));
            wavelength = Float.parseFloat(map.get("wavelength"));
            int mode = DeviceApplication.getInstance().getSpUtils().getWavelengthscanTestMode();
            if (mode == WavelengthSettingActivity.TEST_MODE_ABS) {
                yy[i] = abs;
            } else if (mode == WavelengthSettingActivity.TEST_MODE_TRANS) {
                yy[i] = trans;
            } else if (mode == WavelengthSettingActivity.TEST_MODE_ENERGY) {
                yy[i] = energy;
            }

            x[i] = wavelength;
        }
        mDerivativePoints.clear();

        for (int i = 0; i < total; i++) {
            float y = 0.0f;
            //first one
            if (i == 0) {
                y = (yy[1] - yy[0]) / (x[1] - x[0]);
            }
            //last one
            else if (i == total - 1) {
                //skip
            }
            //others
            else {
                y = (yy[i + 1] - yy[i - 1]) / (x[i + 1] - x[i - 1]);
            }
            updateDerivativeChart(x[i], y);
        }
        //autoUpdateChart();

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
            float wavelength = 0.0f;

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

            wavelength = Float.parseFloat(map1.get("wavelength"));

            item.put("id", "" + no);
            item.put("wavelength", String.format("%.1f", wavelength));
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
            float wavelength = 0.0f;

            HashMap<String, String> map = mOperateData.get(i);
            index = Integer.parseInt(map.get("id"));
            energy = Integer.parseInt(map.get("energy"));
            abs = Float.parseFloat(map.get("abs"));
            trans = Float.parseFloat(map.get("trans"));
            wavelength = Float.parseFloat(map.get("wavelength"));
            updateOperateChart(new WavelengthScanRecord(index, wavelength, abs, trans,
                    energy, System.currentTimeMillis()));
        }
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

/*
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
*/
        for(int i = 1; i < totalSize - 1; i++) {
            if((data[i + 1] - data[i]) * (data[i] - data[i - 1]) < 0) {
                ind2[ind_count2] = i;
                ind_count2 ++;
            }
        }
        for (int i = 0; i < ind_count2 - 1; i++) {
//            float d = Math.abs(data[ind2[i]] - data[ind2[i + 1]]);
            float d = data[ind2[i + 1]] - data[ind2[i]];

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
            updatePeakChart(new WavelengthScanRecord(index, wavelength, abs, trans,
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
