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

import com.squareup.otto.Subscribe;

import org.zhangjie.onlab.R;
import org.zhangjie.onlab.otto.BusProvider;
import org.zhangjie.onlab.otto.RezeroEvent;
import org.zhangjie.onlab.otto.SettingEvent;
import org.zhangjie.onlab.setting.TimescanSettingActivity;
import org.zhangjie.onlab.setting.WavelengthSettingActivity;

/**
 * Created by H151136 on 5/24/2016.
 */
public class WavelengthScanFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "Onlab.WavelengthScan";
    private ListView mListView;

    private Button mStartButton;
    private Button mStopButton;
    private Button mRezeroButton;
    private Button mProcessButton;

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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_wavelength_scan_start:
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
