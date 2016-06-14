package org.zhangjie.onlab.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
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

import org.zhangjie.onlab.R;
import org.zhangjie.onlab.adapter.MultiSelectionAdapter;
import org.zhangjie.onlab.device.DeviceManager;
import org.zhangjie.onlab.otto.BusProvider;
import org.zhangjie.onlab.otto.SettingEvent;
import org.zhangjie.onlab.setting.QuantitativeAnalysisSettingActivity;
import org.zhangjie.onlab.setting.TimescanSettingActivity;

import java.util.HashMap;
import java.util.List;

import lecho.lib.hellocharts.view.LineChartView;

/**
 * Created by H151136 on 5/24/2016.
 */
public class QuantitativeAnalysisFragment extends Fragment implements  View.OnClickListener {

    private static final String TAG = "Onlab.Quantitative";
    private TextView mFittingTypeTextView;
    private TextView mFittingMethodTextView;
    private TextView mConcUnitTextView;

    private Button mStartButton;
    private Button mRezeroButton;
    private Button mDoFittingButton;
    private Button mSelectallButton;
    private Button mDeleteButton;

    private ListView mListView;
    private MultiSelectionAdapter mAdapter;
    private List<HashMap<String, String>> mData;

    private ListView mSampleListView;
    private MultiSelectionAdapter mSampleAdapter;
    private List<HashMap<String, String>> mSampleData;

    private LineChartView mChartView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quantitative_analysis, container, false);
        initView(view);
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

    private void initView(View view) {
        mFittingTypeTextView = (TextView)view.findViewById(R.id.tv_qa_fitting_type);
        mFittingMethodTextView = (TextView)view.findViewById(R.id.tv_qa_fitting_method);
        mConcUnitTextView = (TextView)view.findViewById(R.id.tv_qa_conc_unit);

        mStartButton = (Button)view.findViewById(R.id.bt_qa_start_test);
        mRezeroButton = (Button)view.findViewById(R.id.bt_qa_rezero);
        mDoFittingButton = (Button)view.findViewById(R.id.bt_qa_do_fitting);
        mSelectallButton = (Button)view.findViewById(R.id.bt_qa_sample_selectall);
        mDeleteButton = (Button)view.findViewById(R.id.bt_qa_sample_delete);

        mStartButton.setOnClickListener(this);
        mRezeroButton.setOnClickListener(this);
        mDoFittingButton.setOnClickListener(this);
        mSelectallButton.setOnClickListener(this);
        mDeleteButton.setOnClickListener(this);
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

        Intent intent = new Intent(context, QuantitativeAnalysisSettingActivity.class);
        startActivityForResult(intent, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == QuantitativeAnalysisSettingActivity.RESULT_OK) {
            Log.d(TAG, "OK");
//            loadFromSetting();
        } else if(resultCode == QuantitativeAnalysisSettingActivity.RESULT_CANCEL) {
            Log.d(TAG, "CANCEL");
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
