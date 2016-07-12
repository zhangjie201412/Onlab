package org.zhangjie.onlab.setting;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.zhangjie.onlab.DeviceApplication;
import org.zhangjie.onlab.R;
import org.zhangjie.onlab.dialog.SettingEditDialog;
import org.zhangjie.onlab.utils.Utils;

/**
 * Created by H151136 on 6/6/2016.
 */
public class TimescanSettingActivity extends AppCompatActivity implements View.OnClickListener, SettingEditDialog.SettingInputListern {

    public static final int RESULT_OK = 0;
    public static final int RESULT_CANCEL = 1;

    public static final int TEST_MODE_ABS = 0;
    public static final int TEST_MODE_TRANS = 1;

    private static final String TAG = "Onlab.TimescanSetting";
    private Toolbar mToolbar;
    private LinearLayout mResetLayout;
    private LinearLayout mWorkWavelength;
    private LinearLayout mStartTime;
    private LinearLayout mEndTime;
    private LinearLayout mIntervalTime;
    private LinearLayout mTestMode;
    private LinearLayout mLimitUp;
    private LinearLayout mLimitDown;

    private EditText mWavelengthEditText;
    private EditText mStartEditText;
    private EditText mEndEditText;
    private EditText mIntervalEditText;

    private TextView mWavelengthValue;
    private TextView mStartValue;
    private TextView mEndValue;
    private TextView mIntervalValue;
    private TextView mTestModeValue;
    private TextView mLimitUpValue;
    private TextView mLimitDownValue;

    private SettingEditDialog mDialog;

    private int mMode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.setting_fragment_timescan);
        initView();
        mDialog = new SettingEditDialog();
    }

    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.tb_timescan_setting);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(R.mipmap.ic_arrow);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimescanSettingActivity.this.setResult(RESULT_OK);
                finish();
            }
        });

        mResetLayout = (LinearLayout) findViewById(R.id.layout_timescan_reset);
        mResetLayout.setOnClickListener(this);
        mWorkWavelength = (LinearLayout) findViewById(R.id.layout_work_wavelength);
        mStartTime = (LinearLayout) findViewById(R.id.layout_start_time);
        mEndTime = (LinearLayout) findViewById(R.id.layout_end_time);
        mIntervalTime = (LinearLayout) findViewById(R.id.layout_time_interval);
        mTestMode = (LinearLayout) findViewById(R.id.layout_test_mode);
        mLimitUp = (LinearLayout) findViewById(R.id.layout_limit_up);
        mLimitDown = (LinearLayout) findViewById(R.id.layout_limit_down);

        mWorkWavelength.setOnClickListener(this);
        mStartTime.setOnClickListener(this);
        mEndTime.setOnClickListener(this);
        mIntervalTime.setOnClickListener(this);
        mTestMode.setOnClickListener(this);
        mLimitUp.setOnClickListener(this);
        mLimitDown.setOnClickListener(this);

        mWavelengthEditText = new EditText(this);
        mStartEditText = new EditText(this);
        mEndEditText = new EditText(this);
        mIntervalEditText = new EditText(this);

        mWavelengthValue = (TextView) findViewById(R.id.work_wavelength_value);
        mStartValue = (TextView) findViewById(R.id.start_time_value);
        mEndValue = (TextView) findViewById(R.id.end_time_value);
        mIntervalValue = (TextView) findViewById(R.id.time_interval_value);
        mTestModeValue = (TextView) findViewById(R.id.test_mode_value);
        mLimitUpValue = (TextView) findViewById(R.id.limit_up_value);
        mLimitDownValue = (TextView) findViewById(R.id.limit_down_value);

        loadPreference();

    }

    private void loadPreference() {
        float wavelength = DeviceApplication.getInstance().getSpUtils().getTimescanWorkWavelength();
        int startTime = DeviceApplication.getInstance().getSpUtils().getTimescanStartTime();
        int endTime = DeviceApplication.getInstance().getSpUtils().getTimescanEndTime();
        int timeInterval = DeviceApplication.getInstance().getSpUtils().getTimescanTimeInterval();
        float limitUp = DeviceApplication.getInstance().getSpUtils().getTimescanLimitUp();
        float limitDown = DeviceApplication.getInstance().getSpUtils().getTimescanLimitDown();

        mWavelengthValue.setText("" + wavelength + " " + getString(R.string.nm));
        mStartValue.setText("" + startTime + " " + getString(R.string.s));
        mEndValue.setText("" + endTime + " " + getString(R.string.s));
        mIntervalValue.setText("" + timeInterval + " " + getString(R.string.s));
        if (DeviceApplication.getInstance().getSpUtils().getTimescanTestMode() == TEST_MODE_ABS) {
            mTestModeValue.setText(getString(R.string.abs_with_unit));
        } else {
            mTestModeValue.setText(getString(R.string.trans_with_unit));
        }
        if (DeviceApplication.getInstance().getSpUtils().getTimescanTestMode() == TEST_MODE_ABS) {
            mLimitUpValue.setText("" + limitUp + " " + getString(R.string.abs_unit));
        } else {
            mLimitUpValue.setText("" + limitUp + " " + getString(R.string.trans_unit));
        }
        if (DeviceApplication.getInstance().getSpUtils().getTimescanTestMode() == TEST_MODE_ABS) {
            mLimitDownValue.setText("" + limitDown + " " + getString(R.string.abs_unit));
        } else {
            mLimitDownValue.setText("" + limitDown + " " + getString(R.string.trans_unit));
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.layout_timescan_reset) {
            Log.d(TAG, "reset");
        } else if (v.getId() == R.id.layout_work_wavelength) {
            mDialog.init(v.getId(), getString(R.string.title_timescan_setting_work_wavelength),
                    getString(R.string.title_timescan_work_wavelength), this);
            mDialog.show(getFragmentManager(), "work_wavelength");
        } else if (v.getId() == R.id.layout_start_time) {
            mDialog.init(v.getId(), getString(R.string.title_timescan_setting_x),
                    getString(R.string.title_timescan_start), this);
            mDialog.show(getFragmentManager(), "start");
        } else if (v.getId() == R.id.layout_end_time) {
            mDialog.init(v.getId(), getString(R.string.title_timescan_setting_x),
                    getString(R.string.title_timescan_end), this);
            mDialog.show(getFragmentManager(), "end");
        } else if (v.getId() == R.id.layout_time_interval) {
            mDialog.init(v.getId(), getString(R.string.title_timescan_setting_x),
                    getString(R.string.title_timescan_interval), this);
            mDialog.show(getFragmentManager(), "interval");
        } else if (v.getId() == R.id.layout_test_mode) {
            final String[] items = new String[2];
            items[0] = getString(R.string.abs_with_unit);
            items[1] = getString(R.string.trans_with_unit);
            showSelectDialog(getString(R.string.title_test_mode), items,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            mTestModeValue.setText(items[which]);
                            DeviceApplication.getInstance().getSpUtils().setKeyTimescanTestMode(which);
                            //update limit up and down
                            if(which == TEST_MODE_ABS) {
                                DeviceApplication.getInstance().getSpUtils().setKeyTimescanLimitUp(Utils.DEFAULT_ABS_VALUE);
                                DeviceApplication.getInstance().getSpUtils().setKeyTimescanLimitDown(0.0f);
                            } else if(which == TEST_MODE_TRANS) {
                                DeviceApplication.getInstance().getSpUtils().setKeyTimescanLimitUp(Utils.DEFAULT_TRANS_VALUE);
                                DeviceApplication.getInstance().getSpUtils().setKeyTimescanLimitDown(0.0f);
                            }
                            loadPreference();

                        }
                    });
        } else if (v.getId() == R.id.layout_limit_up) {
            mDialog.init(v.getId(), getString(R.string.title_limit_up),
                    getString(R.string.title_limit_up), this);
            mDialog.show(getFragmentManager(), "limit_up");
        } else if (v.getId() == R.id.layout_limit_down) {
            mDialog.init(v.getId(), getString(R.string.title_limit_down),
                    getString(R.string.title_limit_down), this);
            mDialog.show(getFragmentManager(), "limit_down");
        }
    }

    private void showSelectDialog(String title, final String[] items, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setItems(items, listener);
        builder.create().show();
    }

    @Override
    public void onSettingInputComplete(int index, String setting) {

        if (setting.length() < 1) {
            Toast.makeText(this, getString(R.string.notice_edit_null), Toast.LENGTH_SHORT).show();
            return;
        }

        switch (index) {
            case R.id.layout_work_wavelength:
                Log.d(TAG, "wavelength = " + setting);
                mWavelengthValue.setText(setting + " " + getString(R.string.nm));
                DeviceApplication.getInstance().getSpUtils().setKeyTimescanWorkWavelength(Float.parseFloat(setting));
                break;
            case R.id.layout_start_time:
                Log.d(TAG, "start time = " + setting);
                mStartValue.setText(setting + " " + getString(R.string.s));
                DeviceApplication.getInstance().getSpUtils().setKeyTimescanStartTime(Integer.parseInt(setting));
                break;
            case R.id.layout_end_time:
                Log.d(TAG, "end time = " + setting);
                mEndValue.setText(setting + " " + getString(R.string.s));
                DeviceApplication.getInstance().getSpUtils().setKeyTimescanEndTime(Integer.parseInt(setting));
                break;
            case R.id.layout_time_interval:
                Log.d(TAG, "interval time = " + setting);
                mIntervalValue.setText(setting + " " + getString(R.string.s));
                DeviceApplication.getInstance().getSpUtils().setKeyTimescanTimeInterval(Integer.parseInt(setting));
                break;
            case R.id.layout_limit_up:
                if (DeviceApplication.getInstance().getSpUtils().getTimescanTestMode() == TEST_MODE_ABS) {
                    mLimitUpValue.setText(setting + " " + getString(R.string.abs_unit));
                } else {
                    mLimitUpValue.setText(setting + " " + getString(R.string.trans_unit));
                }
                DeviceApplication.getInstance().getSpUtils().setKeyTimescanLimitUp(Float.parseFloat(setting));
                break;
            case R.id.layout_limit_down:
                if (DeviceApplication.getInstance().getSpUtils().getTimescanTestMode() == TEST_MODE_ABS) {
                    mLimitDownValue.setText(setting + " " + getString(R.string.abs_unit));
                } else {
                    mLimitDownValue.setText(setting + " " + getString(R.string.trans_unit));
                }
                DeviceApplication.getInstance().getSpUtils().setKeyTimescanLimitDown(Float.parseFloat(setting));
                break;
            default:
                break;
        }
    }
}
