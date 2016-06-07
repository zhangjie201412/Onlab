package org.zhangjie.onlab.setting;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.zhangjie.onlab.R;
import org.zhangjie.onlab.dialog.SettingDialog;

/**
 * Created by H151136 on 6/6/2016.
 */
public class TimescanSettingActivity extends AppCompatActivity implements View.OnClickListener, SettingDialog.SettingInputListern {

    private static final String TAG = "Onlab.TimescanSetting";
    private Toolbar mToolbar;
    private LinearLayout mResetLayout;
    private LinearLayout mWorkWavelength;
    private LinearLayout mStartTime;
    private LinearLayout mEndTime;
    private LinearLayout mIntervalTime;
    private EditText mWavelengthEditText;
    private EditText mStartEditText;
    private EditText mEndEditText;
    private EditText mIntervalEditText;

    private TextView mWavelengthValue;
    private TextView mStartValue;
    private TextView mEndValue;
    private TextView mIntervalValue;

    private SettingDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_fragment_timescan);
        initView();
        mDialog = new SettingDialog();
    }

    private void initView() {
        mToolbar = (Toolbar)findViewById(R.id.tb_timescan_setting);
        setSupportActionBar(mToolbar);

        mResetLayout = (LinearLayout)findViewById(R.id.layout_timescan_reset);
        mResetLayout.setOnClickListener(this);
        mWorkWavelength = (LinearLayout)findViewById(R.id.layout_work_wavelength);
        mStartTime = (LinearLayout)findViewById(R.id.layout_start_time);
        mEndTime = (LinearLayout)findViewById(R.id.layout_end_time);
        mIntervalTime = (LinearLayout)findViewById(R.id.layout_time_interval) ;
        mWorkWavelength.setOnClickListener(this);
        mStartTime.setOnClickListener(this);
        mEndTime.setOnClickListener(this);
        mIntervalTime.setOnClickListener(this);
        mWavelengthEditText = new EditText(this);
        mStartEditText = new EditText(this);
        mEndEditText = new EditText(this);
        mIntervalEditText = new EditText(this);

        mWavelengthValue = (TextView)findViewById(R.id.work_wavelength_value);
        mStartValue = (TextView)findViewById(R.id.start_time_value);
        mEndValue = (TextView)findViewById(R.id.end_time_value);
        mIntervalValue = (TextView)findViewById(R.id.time_interval_value);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.layout_timescan_reset) {
            Log.d(TAG, "reset");
        } else if(v.getId() == R.id.layout_work_wavelength) {
            mDialog.init(v.getId(), getString(R.string.title_timescan_setting_work_wavelength),
                    getString(R.string.title_timescan_work_wavelength), this);
            mDialog.show(getFragmentManager(), "work_wavelength");
        } else if(v.getId() == R.id.layout_start_time) {
            mDialog.init(v.getId(), getString(R.string.title_timescan_setting_x),
                    getString(R.string.title_timescan_start), this);
            mDialog.show(getFragmentManager(), "start");
        } else if(v.getId() == R.id.layout_end_time) {
            mDialog.init(v.getId(), getString(R.string.title_timescan_setting_x),
                    getString(R.string.title_timescan_end), this);
            mDialog.show(getFragmentManager(), "end");
        } else if(v.getId() == R.id.layout_time_interval) {
            mDialog.init(v.getId(), getString(R.string.title_timescan_setting_x),
                    getString(R.string.title_timescan_interval), this);
            mDialog.show(getFragmentManager(), "interval");
        }
    }

    @Override
    public void onSettingInputComplete(int index, String setting) {
        switch (index) {
            case R.id.layout_work_wavelength:
                Log.d(TAG, "wavelength = " + setting);
                mWavelengthValue.setText(setting + " " + getString(R.string.nm));
                break;
            case R.id.layout_start_time:
                Log.d(TAG, "start time = " + setting);
                mStartValue.setText(setting + " " + getString(R.string.s));
                break;
            case R.id.layout_end_time:
                Log.d(TAG, "end time = " + setting);
                mEndValue.setText(setting + " " + getString(R.string.s));
                break;
            case R.id.layout_time_interval:
                Log.d(TAG, "interval time = " + setting);
                mIntervalValue.setText(setting + " " + getString(R.string.s));
                break;
            default:
                break;
        }
    }
}
