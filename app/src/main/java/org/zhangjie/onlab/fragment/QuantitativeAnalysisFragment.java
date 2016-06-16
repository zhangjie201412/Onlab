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
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import org.zhangjie.onlab.DeviceApplication;
import org.zhangjie.onlab.R;
import org.zhangjie.onlab.adapter.MultiSelectionAdapter;
import org.zhangjie.onlab.device.DeviceManager;
import org.zhangjie.onlab.dialog.QASampleDialog;
import org.zhangjie.onlab.otto.BusProvider;
import org.zhangjie.onlab.otto.SettingEvent;
import org.zhangjie.onlab.setting.QuantitativeAnalysisSettingActivity;
import org.zhangjie.onlab.setting.TimescanSettingActivity;
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
public class QuantitativeAnalysisFragment extends Fragment implements  View.OnClickListener {

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
    private List<PointValue> mPoints;
    //---
    private QASampleDialog mSampleDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quantitative_analysis, container, false);
        initView(view);
        mSampleDialog = new QASampleDialog();
        mSampleDialog.setCallback(new QASampleDialogSample());
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

    private void initView(View view) {
        mFittingTypeTextView = (TextView)view.findViewById(R.id.tv_qa_fitting_type);
        mFittingMethodTextView = (TextView)view.findViewById(R.id.tv_qa_fitting_method);
        mConcUnitTextView = (TextView)view.findViewById(R.id.tv_qa_conc_unit);
        mFormaluTextView = (TextView)view.findViewById(R.id.tv_formalu);

        mStartButton = (Button)view.findViewById(R.id.bt_qa_start_test);
        mRezeroButton = (Button)view.findViewById(R.id.bt_qa_rezero);
        mAddButton = (Button)view.findViewById(R.id.bt_qa_add);
        mDoFittingButton = (Button)view.findViewById(R.id.bt_qa_do_fitting);
        mSelectallButton = (Button)view.findViewById(R.id.bt_qa_sample_selectall);
        mDeleteButton = (Button)view.findViewById(R.id.bt_qa_sample_delete);

        mStartButton.setOnClickListener(this);
        mRezeroButton.setOnClickListener(this);
        mAddButton.setOnClickListener(this);
        mDoFittingButton.setOnClickListener(this);
        mSelectallButton.setOnClickListener(this);
        mDeleteButton.setOnClickListener(this);

        mListView = (ListView)view.findViewById(R.id.lv_qa_test);
        mSampleListView = (ListView)view.findViewById(R.id.lv_qa_sample);
        mData = new ArrayList<HashMap<String, String>>();
        mSampleData = new ArrayList<HashMap<String, String>>();
        mAdapter = new MultiSelectionAdapter(getActivity(), mData,
                R.layout.item_quantitative_analysis,
                new String[] {"id", "name", "abs", "conc"},
                new int[] {R.id.item_index, R.id.item_name,
                R.id.item_abs, R.id.item_conc});
        mSampleAdapter = new MultiSelectionAdapter(getActivity(), mData,
                R.layout.item_quantitative_analysis,
                new String[] {"id", "name", "abs", "conc"},
                new int[] {R.id.item_index, R.id.item_name,
                        R.id.item_abs, R.id.item_conc});
        mListView.setAdapter(mAdapter);
        mSampleListView.setAdapter(mSampleAdapter);

        mChartView = (LineChartView) view.findViewById(R.id.hello_qa);
        initChart();
        loadSetting();
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

        if(calc_type == QuantitativeAnalysisSettingActivity.CALC_TYPE_FORMALU) {
            mFormaluTextView.setVisibility(View.VISIBLE);
            //make the formalu
            String formalu = "CONC = ";
            if(k0 != 0) {
                formalu += "" + k0;
            }
            if(k1 != 0) {
                if(k0 != 0) {
                    formalu += " + " + k1 + " x A";
                } else {
                    formalu += "" + k1 + " x A";
                }
            }
            if(k0 == 0 && k1 == 0) {
                formalu = "CONC = 0";
            }
            mFormaluTextView.setText(formalu);
        } else if(calc_type == QuantitativeAnalysisSettingActivity.CALC_TYPE_SAMPLE) {
            mFormaluTextView.setVisibility(View.GONE);
        }
        String xTtitle = getString(R.string.abs_with_unit);
        String yTitle = getString(R.string.conc) + "(" + getResources().getStringArray(R.array.concs)[conc_unit] +")";
        updateXYTitle(xTtitle, yTitle, 0, 4.0f, 10.0f, 0);
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

    @Subscribe
    public void OnSettingEvent(SettingEvent event) {
        Context context = null;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context = getContext();
        } else {
            context = getActivity();
        }

        Intent intent = new Intent(context, QuantitativeAnalysisSettingActivity.class);
        startActivityForResult(intent, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == QuantitativeAnalysisSettingActivity.RESULT_OK) {
            Log.d(TAG, "OK");
            loadSetting();
        } else if(resultCode == QuantitativeAnalysisSettingActivity.RESULT_CANCEL) {
            Log.d(TAG, "CANCEL");
        }
    }

    class QASampleDialogSample implements QASampleDialog.QASampleDialogCallback {

        @Override
        public void onCompleteInput(int type, String name, String conc) {
            if(type == QASampleDialog.TYPE_OK) {

            } else if(type == QASampleDialog.TYPE_TEST) {

            } else if(type == QASampleDialog.TYPE_CANCEL) {

            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_qa_start_test:
                break;
            case R.id.bt_qa_rezero:
                DeviceManager.getInstance().rezeroWork();
                break;
            case R.id.bt_qa_add:
                mSampleDialog.show(getFragmentManager(), getString(R.string.sample_conc_setting));
                break;
            case R.id.bt_qa_do_fitting:
                break;
            case R.id.bt_qa_sample_selectall:
                break;
            case R.id.bt_qa_sample_delete:
                break;
            default:
                break;
        }
    }
}
