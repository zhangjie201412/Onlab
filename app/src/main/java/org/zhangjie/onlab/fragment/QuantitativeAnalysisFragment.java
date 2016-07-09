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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import org.zhangjie.onlab.DeviceApplication;
import org.zhangjie.onlab.MainActivity;
import org.zhangjie.onlab.R;
import org.zhangjie.onlab.adapter.MultiSelectionAdapter;
import org.zhangjie.onlab.device.DeviceManager;
import org.zhangjie.onlab.dialog.QASampleDialog;
import org.zhangjie.onlab.dialog.SaveNameDialog;
import org.zhangjie.onlab.otto.BusProvider;
import org.zhangjie.onlab.otto.FileOperateEvent;
import org.zhangjie.onlab.otto.QaUpdateEvent;
import org.zhangjie.onlab.otto.SetOperateEvent;
import org.zhangjie.onlab.otto.SetOperateModeEvent;
import org.zhangjie.onlab.otto.SettingEvent;
import org.zhangjie.onlab.record.QuantitativeAnalysisRecord;
import org.zhangjie.onlab.setting.QuantitativeAnalysisSettingActivity;
import org.zhangjie.onlab.utils.SharedPreferenceUtils;
import org.zhangjie.onlab.utils.Utils;

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
public class QuantitativeAnalysisFragment extends Fragment implements View.OnClickListener {

    private final int ACTION_TYPE_SAMPLE = 1;
    private final int ACTION_TYPE_TEST = 2;

    private static final String TAG = "Onlab.Quantitative";
    private TextView mFittingTypeTextView;
    private TextView mFittingMethodTextView;
    private TextView mConcUnitTextView;
    private TextView mFormaluTextView;

    private Button mStartButton;
    private Button mRezeroButton;
    private Button mAddButton;
    private Button mDoFittingButton;
    private Button mSelectallButton;
    private Button mDeleteButton;

    private ListView mListView;
    private MultiSelectionAdapter mAdapter;
    private List<HashMap<String, String>> mData;

    private ListView mSampleListView;
    private MultiSelectionAdapter mSampleAdapter;
    private List<HashMap<String, String>> mSampleData;

    //+++chart
    private LineChartView mChartView;
    private LineChartData mChartData;
    private List<Line> mLines;
    private Line mLine;
    private Line mLine2;
    private List<PointValue> mPoints;
    private List<PointValue> mPoints2;
    //---
    private QASampleDialog mSampleDialog;
    private int mUpdateSampleIndex = 0;
    private boolean isFittinged = false;
    private float sampleA0;
    private float sampleA1;
    private float mActionType = 0;
    private SaveNameDialog mNameDialog;
    private SaveNameDialog mSaveDialog;

    private boolean loadFile = false;
    private int loadFileIndex = -1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quantitative_analysis, container, false);
        Utils.needToSave = false;
        initView(view);
        mSampleDialog = new QASampleDialog();
        mSampleDialog.setCallback(new QASampleDialogSample());

        mNameDialog = new SaveNameDialog();
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
        Utils.needToSave = false;
        loadFile = false;
        Log.d(TAG, "onDestroy");
    }

    private void initView(View view) {
        mFittingTypeTextView = (TextView) view.findViewById(R.id.tv_qa_fitting_type);
        mFittingMethodTextView = (TextView) view.findViewById(R.id.tv_qa_fitting_method);
        mConcUnitTextView = (TextView) view.findViewById(R.id.tv_qa_conc_unit);
        mFormaluTextView = (TextView) view.findViewById(R.id.tv_formalu);

        mStartButton = (Button) view.findViewById(R.id.bt_qa_start_test);
        mRezeroButton = (Button) view.findViewById(R.id.bt_qa_rezero);
        mAddButton = (Button) view.findViewById(R.id.bt_qa_add);
        mDoFittingButton = (Button) view.findViewById(R.id.bt_qa_do_fitting);
        mSelectallButton = (Button) view.findViewById(R.id.bt_qa_sample_selectall);
        mDeleteButton = (Button) view.findViewById(R.id.bt_qa_sample_delete);

        mStartButton.setOnClickListener(this);
        mRezeroButton.setOnClickListener(this);
        mAddButton.setOnClickListener(this);
        mDoFittingButton.setOnClickListener(this);
        mSelectallButton.setOnClickListener(this);
        mDeleteButton.setOnClickListener(this);

        mListView = (ListView) view.findViewById(R.id.lv_qa_test);
        mSampleListView = (ListView) view.findViewById(R.id.lv_qa_sample);
        mData = new ArrayList<HashMap<String, String>>();
        mSampleData = new ArrayList<HashMap<String, String>>();
        mAdapter = new MultiSelectionAdapter(getActivity(), mData,
                R.layout.item_quantitative_analysis,
                new String[]{"id", "name", "abs", "conc"},
                new int[]{R.id.item_index, R.id.item_name,
                        R.id.item_abs, R.id.item_conc});
        mSampleAdapter = new MultiSelectionAdapter(getActivity(), mSampleData,
                R.layout.item_quantitative_analysis,
                new String[]{"id", "name", "abs", "conc"},
                new int[]{R.id.item_index, R.id.item_name,
                        R.id.item_abs, R.id.item_conc});
        mListView.setAdapter(mAdapter);
        mAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (mData.size() > 0) {
                    Utils.needToSave = true;
                } else {
                    Utils.needToSave = false;
                }
            }
        });
        //set test listview on click listener
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                boolean mode = ((MainActivity) getActivity()).getOperateMode();
                if (mode) {
                    MultiSelectionAdapter.ViewHolder holder = (MultiSelectionAdapter.ViewHolder) view.getTag();
                    holder.cb.toggle();
                    mAdapter.getIsSelected().put(position, holder.cb.isChecked());
                    mAdapter.notifyDataSetChanged();
                } else {
                    //set test name
                    mNameDialog.init(position, getString(R.string.name_setting), getString(R.string.name)
                            , new SaveNameDialog.SettingInputListern() {
                                @Override
                                public void onSettingInputComplete(int index, String name) {
                                    if (name.length() < 1) {
                                        Toast.makeText(getActivity(), getString(R.string.notice_edit_null), Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    mData.get(position).put("name", name);
                                    mAdapter.notifyDataSetChanged();
                                }

                                @Override
                                public void abort() {

                                }
                            });
                    mNameDialog.setAbort(false);
                    mNameDialog.show(getFragmentManager(), "name");
                }
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<Integer, Boolean> sel = mAdapter.getIsSelected();
                for (int i = 0; i < sel.size(); i++) {
                    sel.put(i, false);
                }
                mAdapter.setSelectMode(true);
                mAdapter.notifyDataSetChanged();
                BusProvider.getInstance().post(new SetOperateModeEvent(true));
                return true;
            }
        });
        mSampleListView.setAdapter(mSampleAdapter);
        mAdapter.setSelectMode(false);

        mSampleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> item;
                item = mSampleData.get(position);
                String name = item.get("name");
                String conc = item.get("conc");
                if (name.length() < 1) {
                    name = getString(R.string.sample);
                }
                Bundle bundle = new Bundle();
                bundle.putString("conc", conc);
                bundle.putString("name", name);
                mSampleDialog.setData(name, conc);
                mSampleDialog.setIsNew(false);
                mSampleDialog.setIndex(position);
                mSampleDialog.show(getFragmentManager(), getString(R.string.sample_conc_setting));
            }
        });

        mChartView = (LineChartView) view.findViewById(R.id.hello_qa);
        initChart();
        loadSetting();
        mSaveDialog = new SaveNameDialog();
        mSaveDialog.init(-1, getString(R.string.action_save), getString(R.string.name), new SaveNameDialog.SettingInputListern() {
            @Override
            public void onSettingInputComplete(int id, String name) {

                if (name.length() < 1 || (!Utils.isValidName(name))) {
                    Toast.makeText(getActivity(), getString(R.string.notice_name_invalid), Toast.LENGTH_SHORT).show();
                    return;
                }
                List<String> saveFileList = DeviceApplication.getInstance().getQuantitativeAnalysisDb().getTables();
                Log.d(TAG, "Alread saved -> " + saveFileList.size() + " files.");

                for (int i = 0; i < saveFileList.size(); i++) {
                    Log.d(TAG, String.format("[%d] -> %s\n", i, saveFileList.get(i)));
                }
                String fileName = name;
                for (int i = 0; i < mData.size(); i++) {
                    int index = 0;
                    float abs = 0.0f;
                    float conc = 0.0f;
                    long date = 0;
                    String name2 = "";

                    HashMap<String, String> map = mData.get(i);
                    index = Integer.parseInt(map.get("id"));
                    abs = Float.parseFloat(map.get("abs"));
                    conc = Float.parseFloat(map.get("conc"));
                    name2 = map.get("name");
                    date = Long.parseLong(map.get("date"));

                    Log.d(TAG, "####save name = " + name2);

                    QuantitativeAnalysisRecord record = new QuantitativeAnalysisRecord(index, name2, abs, conc, date);
                    DeviceApplication.getInstance().getQuantitativeAnalysisDb().saveRecord(fileName, record);
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
    }

    public void prepareLoadFile(int id) {
        loadFile = true;
        loadFileIndex = id;
    }

    private void initChart() {
        mPoints = new ArrayList<PointValue>();
        mPoints2 = new ArrayList<PointValue>();
        mLines = new ArrayList<Line>();
        mLine = new Line(mPoints).setColor(Color.WHITE).setCubic(true);
        mLine2 = new Line(mPoints2).setColor(Color.GREEN).setCubic(true);
        mLine.setPointRadius(1);
        mLine.setStrokeWidth(1);
        mLine2.setPointRadius(1);
        mLine2.setStrokeWidth(1);
        mLines.add(mLine);
        mLines.add(mLine2);

        mChartData = new LineChartData();
        mChartData.setBaseValue(Float.NEGATIVE_INFINITY);
        mChartView.setLineChartData(mChartData);
    }

    private void loadSetting() {
        SharedPreferenceUtils sp = DeviceApplication.getInstance().getSpUtils();
        int fitting_method = sp.getQAFittingMethod();
        int conc_unit = sp.getQAConcUnit();
        int calc_type = sp.getQACalcType();
        float k0 = sp.getQAK0();
        float k1 = sp.getQAK1();
        int wavelength_setting = sp.getQAWavelengthSetting();
        float wavelength1 = sp.getQAWavelength1();
        float wavelength2 = sp.getQAWavelength2();
        float wavelength3 = sp.getQAWavelength3();
        float ratio1 = sp.getQARatio1();
        float ratio2 = sp.getQARatio2();
        float ratio3 = sp.getQARatio3();

        if (calc_type == QuantitativeAnalysisSettingActivity.CALC_TYPE_FORMALU) {
            mFormaluTextView.setVisibility(View.VISIBLE);
            //make the formalu
            String formalu = "CONC = ";
            if (k0 != 0) {
                formalu += "" + k0;
            }
            if (k1 != 0) {
                if (k0 != 0) {
                    formalu += " + " + k1 + " x A";
                } else {
                    formalu += "" + k1 + " x A";
                }
            }
            if (k0 == 0 && k1 == 0) {
                formalu = "CONC = 0";
            }
            mFormaluTextView.setText(formalu);
            mAddButton.setEnabled(false);
            mDoFittingButton.setEnabled(false);
            mSelectallButton.setEnabled(false);
            mDeleteButton.setEnabled(false);
            //update hello chart with formalu
            updateChartWithFormalu(k0, k1);
        } else if (calc_type == QuantitativeAnalysisSettingActivity.CALC_TYPE_SAMPLE) {
            mFormaluTextView.setVisibility(View.GONE);
            mAddButton.setEnabled(true);
            mDoFittingButton.setEnabled(true);
            mSelectallButton.setEnabled(true);
            mDeleteButton.setEnabled(true);
        }
        String xTtitle = getString(R.string.abs_with_unit);
        String yTitle = getString(R.string.conc) + "(" + getResources().getStringArray(R.array.concs)[conc_unit] + ")";
        updateXYTitle(xTtitle, yTitle, 0, 4.0f, 10.0f, 0);
    }

    void updateXYTitle(String xTitle, String yTitle, float left, float right, float top, float bottom) {
        final Viewport viewport = new Viewport(mChartView.getCurrentViewport());
        viewport.left = left;
        viewport.top = top;
        viewport.right = right;
        viewport.bottom = bottom;

        mChartView.setMaximumViewport(new Viewport(left - right, top * 2, right, bottom * 2));
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

    @Subscribe
    public void OnSettingEvent(SettingEvent event) {
        Context context = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context = getContext();
        } else {
            context = getActivity();
        }

        Intent intent = new Intent(context, QuantitativeAnalysisSettingActivity.class);
        startActivityForResult(intent, 0);
    }

    @Subscribe
    public void onSetOperateModeEvent(SetOperateModeEvent event) {
        if (!event.isOperateMode) {
            //back to normal mode
            mAdapter.setSelectMode(false);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Subscribe
    public void onSetOperateEvent(SetOperateEvent event) {

        if (event.mode == SetOperateEvent.OP_MODE_SELECTALL) {
            HashMap<Integer, Boolean> sel = mAdapter.getIsSelected();
            for (int i = 0; i < sel.size(); i++) {
                sel.put(i, true);
            }
            mAdapter.notifyDataSetInvalidated();
        } else if (event.mode == SetOperateEvent.OP_MODE_DELETE) {
            int selectCount = 0;
            HashMap<Integer, Boolean> sel = mAdapter.getIsSelected();
            for (int i = 0; i < sel.size(); i++) {
                if (sel.get(i)) {
                    selectCount++;
                }
            }

            if (selectCount > 0) {
                showDeleteAlertDialog();
            }
        }
    }

    @Subscribe
    public void onFileOperateEvent(FileOperateEvent event) {
        if (event.op_type == FileOperateEvent.OP_EVENT_OPEN) {
            List<String> saveFileList = DeviceApplication.getInstance().getQuantitativeAnalysisDb().getTables();

            Utils.showItemSelectDialog(getActivity(), getString(R.string.action_open)
                    , saveFileList.toArray(new String[saveFileList.size()]), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            loadFileById(which);
                        }
                    });

        } else if (event.op_type == FileOperateEvent.OP_EVENT_SAVE) {
            if (mData.size() < 1) {
                Toast.makeText(getActivity(), getString(R.string.notice_save_null), Toast.LENGTH_SHORT).show();
                return;
            }
            mSaveDialog.show(getFragmentManager(), "save");
        } else if (event.op_type == FileOperateEvent.OP_EVENT_PRINT) {

        }
    }

    private void loadFileById(int id) {
        List<String> fileList = DeviceApplication.getInstance().getQuantitativeAnalysisDb().getTables();
        String fileName = fileList.get(id);
        List<QuantitativeAnalysisRecord> lists = DeviceApplication.getInstance().getQuantitativeAnalysisDb().getRecords(fileName);
        mData.clear();
        for (int i = 0; i < lists.size(); i++) {
            addTestItem(lists.get(i));
        }
        Utils.needToSave = false;
    }


    void showDeleteAlertDialog() {
        new android.app.AlertDialog.Builder(getActivity())
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(getString(R.string.notice_string))
                .setMessage(getString(R.string.sure_to_delete))
                .setPositiveButton(R.string.ok_string,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // TODO Auto-generated method stub
                                HashMap<Integer, Boolean> sel = mAdapter
                                        .getIsSelected();
                                int delCount = 0;
                                HashMap<Integer, Integer> delHashMap = new HashMap<Integer, Integer>();
                                for (int i = 0; i < sel.size(); i++) {
                                    if (sel.get(i)) {
                                        delHashMap.put(delCount, i);
                                        Log.d(TAG, "count = "
                                                + delCount + ", id = " + i);
                                        delCount = delCount + 1;
                                    }
                                }
                                Log.d(TAG, "count = " + delCount);
                                for (int i = 0; i < delCount; i++) {
                                    removeTestItem(delHashMap.get(i));
                                    sel = mAdapter.getIsSelected();
                                    for (int j = delHashMap.get(i); j < sel
                                            .size() - 1; j++) {
                                        sel.put(j, sel.get(j + 1));
                                    }
                                    sel.remove(sel.size() - 1);
                                    for (int j = 0; j < delHashMap.size(); j++) {
                                        delHashMap.put(j, delHashMap.get(j) - 1);
                                    }
                                    mAdapter.setIsSelected(sel);
                                }

                            }
                        })
                .setNegativeButton(getString(R.string.cancel_string),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {// 响应事件
                                // TODO Auto-generated method stub
                            }
                        }).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == QuantitativeAnalysisSettingActivity.RESULT_OK) {
            Log.d(TAG, "OK");
            loadSetting();
            //set wavelength to target
            float work_wavelength = DeviceApplication.getInstance().getSpUtils().getQAWavelength1();
//            ((MainActivity)getActivity()).loadWavelengthDialog(work_wavelength);
        } else if (resultCode == QuantitativeAnalysisSettingActivity.RESULT_CANCEL) {
            Log.d(TAG, "CANCEL");
        }
    }

    class QASampleDialogSample implements QASampleDialog.QASampleDialogCallback {

        @Override
        public void onCompleteInput(int type, String name, String conc) {
            if (type == QASampleDialog.TYPE_OK) {
                Log.d("###", "OK");
                //check input
                if (name.length() < 1 || conc.length() < 1) {
                    //show invalid input
                    Toast.makeText(getActivity(), getString(R.string.notice_edit_null), Toast.LENGTH_SHORT).show();
                    return;
                }
                //abs == -100 means null
                if (mSampleDialog.isNew()) {
                    addSampleItem(new QuantitativeAnalysisRecord(-1, name, -100.0f,
                            Float.parseFloat(conc), System.currentTimeMillis()));
                } else {
                    //get index
                    int index = mSampleDialog.getIndex();
                    HashMap<String, String> item = new HashMap<String, String>();
                    item.put("id", "" + (index + 1));
                    item.put("name", name);
                    item.put("conc", conc);
                    item.put("abs", mSampleData.get(index).get("abs"));

                    mSampleData.set(mSampleDialog.getIndex(), item);
                    mSampleAdapter.notifyDataSetChanged();
                }
            } else if (type == QASampleDialog.TYPE_TEST) {
                Log.d("###", "TEST");
                mActionType = ACTION_TYPE_SAMPLE;
                //check input
                if (name.length() < 1 || conc.length() < 1) {
                    //show invalid input
                    Toast.makeText(getActivity(), getString(R.string.notice_edit_null), Toast.LENGTH_SHORT).show();
                    return;
                }
                //abs == -100 means null
                if (mSampleDialog.isNew()) {
                    addSampleItem(new QuantitativeAnalysisRecord(-1, name, -100.0f,
                            Float.parseFloat(conc), System.currentTimeMillis()));
                    mUpdateSampleIndex = mSampleData.size() - 1;
                } else {
                    //get index
                    int index = mSampleDialog.getIndex();
                    mUpdateSampleIndex = index;
                    HashMap<String, String> item = new HashMap<String, String>();
                    item.put("id", "" + (index + 1));
                    item.put("name", name);
                    item.put("conc", conc);
                    item.put("abs", mSampleData.get(index).get("abs"));

                    mSampleData.set(mSampleDialog.getIndex(), item);
                    mSampleAdapter.notifyDataSetChanged();
                }
                //send get abs cmd
                DeviceManager.getInstance().doQuantitativeAnalysis();
            } else if (type == QASampleDialog.TYPE_CANCEL) {

            }
        }
    }

    private void addTestItem(QuantitativeAnalysisRecord record) {
        HashMap<String, String> item = new HashMap<String, String>();
        int no = mData.size() + 1;
        record.setIndex(no);

        item.put("id", "" + no);
        item.put("name", (record.getName() == null) ? "" : record.getName());
        item.put("abs", Utils.formatAbs(record.getAbs()));
        item.put("conc", "" + Utils.formatConc(record.getConc()));
        item.put("date", "" + record.getDate());
        mData.add(item);
        mAdapter.add();
        mAdapter.notifyDataSetChanged();
        if (mData.size() > 0) {
            mListView.setSelection(mSampleData.size() - 1);
        }
    }

    private void removeTestItem(int position) {
        mData.remove(position);
        for (int i = 0; i < mData.size(); i++) {
            HashMap<String, String> item = mData.get(i);
            item.put("id", "" + (i + 1));
        }
        mAdapter.notifyDataSetChanged();
    }

    private void addSampleItem(QuantitativeAnalysisRecord record) {
        HashMap<String, String> item = new HashMap<String, String>();
        int no = mSampleData.size() + 1;
        record.setIndex(no);

        item.put("id", "" + no);
        item.put("name", record.getName());
        if (record.getAbs() == -100.0f) {
            item.put("abs", "");
        } else {
            item.put("abs", Utils.formatAbs(record.getAbs()));
        }
        item.put("conc", "" + Utils.formatConc(record.getConc()));
        mSampleData.add(item);
        mSampleAdapter.add();
        mSampleAdapter.setSelectMode(true);
        mSampleAdapter.notifyDataSetChanged();
        if (mSampleData.size() > 0) {
            mSampleListView.setSelection(mSampleData.size() - 1);
        }
    }

    private void removeSampleItem(int pos) {
        mSampleData.remove(pos);
        for (int i = 0; i < mSampleData.size(); i++) {
            HashMap<String, String> item = mSampleData.get(i);
            item.put("id", "" + (i + 1));
        }
        mSampleAdapter.notifyDataSetChanged();
    }

    private void updateChartWithFormalu(float k0, float k1) {
        mPoints.clear();
        mPoints.add(new PointValue(0, k0));
        mPoints.add(new PointValue(4, k0 + 4 * k1));
        if (k1 != 0) {
            mPoints.add(new PointValue((10 - k0) / k1, 10));
            mPoints.add(new PointValue(-k0 / k1, 0));
        } else {
            mPoints.add(new PointValue(4.0f, k0));
        }
        mLine.setHasPoints(false);
        mChartData.setLines(mLines);
        mChartView.setLineChartData(mChartData);
    }

    @Subscribe
    public void onUpdateEvent(QaUpdateEvent event) {
        float abs = event.abs;
        if (mActionType == ACTION_TYPE_SAMPLE) {
            mSampleData.get(mUpdateSampleIndex).put("abs", Utils.formatAbs(abs));
            mSampleAdapter.notifyDataSetChanged();
            //update point2 on chart
            mPoints2.clear();
            for (int i = 0; i < mSampleData.size(); i++) {
                String absString = mSampleData.get(i).get("abs");
                String concString = mSampleData.get(i).get("conc");
                if (absString.length() > 0 && (concString.length() > 0)) {
                    mPoints2.add(new PointValue(Float.parseFloat(absString)
                            , Float.parseFloat(concString)));
                }
            }
            mLine2.setHasPoints(true);
            mLine2.setHasLines(false);
            mChartData.setLines(mLines);
            mChartView.setLineChartData(mChartData);
        } else if (mActionType == ACTION_TYPE_TEST) {
            float conc = 0.0f;
            if (DeviceApplication.getInstance().getSpUtils().getQACalcType()
                    == QuantitativeAnalysisSettingActivity.CALC_TYPE_SAMPLE) {
                if (!isFittinged) {
                    Toast.makeText(getActivity(), getString(R.string.notice_fitting_null), Toast.LENGTH_SHORT).show();
                    return;
                }
                conc = sampleA0 + sampleA1 * abs;
            } else if (DeviceApplication.getInstance().getSpUtils().getQACalcType()
                    == QuantitativeAnalysisSettingActivity.CALC_TYPE_FORMALU) {
                float k0 = DeviceApplication.getInstance().getSpUtils().getQAK0();
                float k1 = DeviceApplication.getInstance().getSpUtils().getQAK1();
                conc = k0 + k1 * abs;
            }
            addTestItem(new QuantitativeAnalysisRecord(-1, "", abs, conc, System.currentTimeMillis()));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_qa_start_test:
                mActionType = ACTION_TYPE_TEST;
                DeviceManager.getInstance().doQuantitativeAnalysis();
//                addTestItem(new QuantitativeAnalysisRecord(-1, "Test", 1.0f, 2.4f, System.currentTimeMillis()));
                break;
            case R.id.bt_qa_rezero:
                DeviceManager.getInstance().rezeroWork();
                break;
            case R.id.bt_qa_add:
                mSampleDialog.setIsNew(true);
                mSampleDialog.setIndex(mSampleData.size());
                mSampleDialog.show(getFragmentManager(), getString(R.string.sample_conc_setting));
                break;
            case R.id.bt_qa_do_fitting:
                doFitting();
                break;
            case R.id.bt_qa_sample_selectall:
                HashMap<Integer, Boolean> sel = mSampleAdapter.getIsSelected();
                sel = mSampleAdapter.getIsSelected();
                for (int i = 0; i < sel.size(); i++) {
                    sel.put(i, true);
                }
                mSampleAdapter.notifyDataSetInvalidated();
                break;
            case R.id.bt_qa_sample_delete:
                showSampleDeleteDialog();
                break;
            default:
                break;
        }
    }

    private void doFitting() {
        HashMap<Integer, Boolean> sel = mSampleAdapter.getIsSelected();
        int fittingCount = 0;
        for (int i = 0; i < sel.size(); i++) {
            if (sel.get(i)) {
                String absVal = mSampleData.get(i).get("abs");
                String concVal = mSampleData.get(i).get("conc");
                if (absVal.length() > 0 && (concVal.length() > 0)) {
                    fittingCount = fittingCount + 1;
                } else {
                    Toast.makeText(getActivity(), getString(R.string.notice_select_null), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }
        Log.d(TAG, "fittingCount = " + fittingCount);
        double[] x = new double[fittingCount];
        double[] y = new double[fittingCount];
        int index = 0;
        for (int i = 0; i < sel.size(); i++) {
            if (sel.get(i)) {
                String absVal = mSampleData.get(i).get("abs");
                String concVal = mSampleData.get(i).get("conc");
                if (absVal.length() > 0 && (concVal.length() > 0)) {
                    x[index] = Double.parseDouble(absVal);
                    y[index] = Double.parseDouble(concVal);
                    index = index + 1;
                }
            }
        }

        float xi2 = 0;
        float yi = 0;
        float xi = 0;
        float xiyi = 0;

        float a0;
        float a1;
        for (int i = 0; i < x.length; i++) {
            xi2 = (float) (xi2 + x[i] * x[i]);
            yi = (float) (yi + y[i]);
            xi = (float) (xi + x[i]);
            xiyi = (float) (xiyi + x[i] * y[i]);
        }

        a0 = (xi2 * yi - xi * xiyi) / (x.length * xi2 - xi * xi);
        a1 = (x.length * xiyi - xi * yi) / (x.length * xi2 - xi * xi);
        sampleA0 = a0;
        sampleA1 = a1;
        Log.d(TAG, "a0 = " + a0 + ", a1 = " + a1);
        isFittinged = true;
        //update hello chart
        updateChartWithFormalu(a0, a1);
    }

    private void showSampleDeleteDialog() {
        int c = 0;
        HashMap<Integer, Boolean> sel = mSampleAdapter.getIsSelected();
        sel = mSampleAdapter.getIsSelected();
        for (int i = 0; i < sel.size(); i++) {
            if (sel.get(i) == true) {
                c++;
            }
        }
        if (c == 0) {
            Toast.makeText(getActivity(), getString(R.string.noting_selected), Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.notice_delete))
                .setIcon(R.mipmap.ic_launcher)
                .setMessage(getString(R.string.sure_to_delete))
                .setPositiveButton(R.string.ok_string,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                HashMap<Integer, Boolean> sel = mSampleAdapter
                                        .getIsSelected();
                                int delCount = 0;
                                HashMap<Integer, Integer> delHashMap = new HashMap<Integer, Integer>();
                                for (int i = 0; i < sel.size(); i++) {
                                    if (sel.get(i)) {
                                        delHashMap.put(delCount, i);
                                        delCount = delCount + 1;
                                    }
                                }
                                for (int i = 0; i < delCount; i++) {
//                                    if (delHashMap.get(i) == (mSampleData
//                                            .size() - 1)) {
//                                        continue;
//                                    }
                                    removeSampleItem(delHashMap.get(i));
                                    sel = mSampleAdapter.getIsSelected();
                                    for (int j = delHashMap.get(i); j < sel
                                            .size() - 1; j++) {
                                        sel.put(j, sel.get(j + 1));
                                    }
                                    sel.remove(sel.size() - 1);
                                    for (int j = 0; j < delHashMap.size(); j++) {
                                        delHashMap.put(j, delHashMap.get(j) - 1);
                                    }
                                    mSampleAdapter.setIsSelected(sel);
                                }
                            }
                        })
                .setNegativeButton(getString(R.string.cancel_string),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // TODO Auto-generated method stub
                            }
                        }).show();
    }
}
