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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.zhangjie.onlab.DeviceApplication;
import org.zhangjie.onlab.R;
import org.zhangjie.onlab.dialog.SettingEditDialog;
import org.zhangjie.onlab.utils.SharedPreferenceUtils;
import org.zhangjie.onlab.utils.Utils;

/**
 * Created by H151136 on 6/6/2016.
 */
public class WavelengthSettingActivity extends AppCompatActivity implements View.OnClickListener, SettingEditDialog.SettingInputListern {

    public static final int RESULT_OK = 0;
    public static final int RESULT_CANCEL = 1;

    public static final int TEST_MODE_ABS = 0;
    public static final int TEST_MODE_TRANS = 1;
    public static final int TEST_MODE_ENERGY = 2;

    public static final int SPEED_FAST = 0;
    public static final int SPEED_STANDARD = 1;
    public static final int SPEED_SLOW = 2;

    private static final String TAG = "Onlab.WavelengthSetting";

    private int mMode = 0;

    private Toolbar mToolbar;

    private LinearLayout mLayoutReset;
    private LinearLayout mLayoutTestMode;
    private RelativeLayout mLayoutLimitUp;
    private RelativeLayout mLayoutLimitDown;
    private RelativeLayout mLayoutStart;
    private RelativeLayout mLayoutEnd;
    private LinearLayout mLayoutInterval;
    private LinearLayout mLayoutSpeed;

    private TextView mTestModeValue;
    private TextView mLimitUpValue;
    private TextView mLimitDownValue;
    private TextView mStartValue;
    private TextView mEndValue;
    private TextView mSpeedValue;
    private TextView mIntervalValue;

    private SharedPreferenceUtils mSpUtils;

    private SettingEditDialog mDialog;
//    private EditText mLimitUpEditText;
//    private EditText mLimitDownEditText;
//    private EditText mStartEditText;
//    private EditText mEndEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.setting_fragment_wavelengthscan);
        mSpUtils = DeviceApplication.getInstance().getSpUtils();

        initView();
        mDialog = new SettingEditDialog();
    }

    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.tb_wavelengthscan_setting);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(R.mipmap.ic_arrow);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WavelengthSettingActivity.this.setResult(RESULT_OK);
                finish();
            }
        });

        mLayoutReset = (LinearLayout) findViewById(R.id.layout_wavelengthscan_reset);
        mLayoutTestMode = (LinearLayout) findViewById(R.id.layout_test_mode);
        mLayoutLimitUp = (RelativeLayout) findViewById(R.id.layout_limit_up);
        mLayoutLimitDown = (RelativeLayout) findViewById(R.id.layout_limit_down);
        mLayoutStart = (RelativeLayout) findViewById(R.id.layout_wavelength_start);
        mLayoutEnd = (RelativeLayout) findViewById(R.id.layout_wavelength_end);
        mLayoutInterval = (LinearLayout) findViewById(R.id.layout_wavelength_interval);
        mLayoutSpeed = (LinearLayout)findViewById(R.id.layout_wavelengthscan_speed);

        mTestModeValue = (TextView)findViewById(R.id.test_mode_value);
        mLimitUpValue = (TextView)findViewById(R.id.tv_limit_up_value);
        mLimitDownValue = (TextView)findViewById(R.id.tv_limit_down_value);
        mStartValue = (TextView)findViewById(R.id.tv_wavelength_start_value);
        mEndValue = (TextView)findViewById(R.id.tv_wavelength_end_value);
        mSpeedValue = (TextView)findViewById(R.id.tv_speed_value);
        mIntervalValue = (TextView)findViewById(R.id.tv_wavelength_interval);

        mLayoutReset.setOnClickListener(this);
        mLayoutTestMode.setOnClickListener(this);
        mLayoutLimitUp.setOnClickListener(this);
        mLayoutLimitDown.setOnClickListener(this);
        mLayoutStart.setOnClickListener(this);
        mLayoutEnd.setOnClickListener(this);
        mLayoutInterval.setOnClickListener(this);
        mLayoutSpeed.setOnClickListener(this);

        loadPreference();
    }

    private void loadPreference() {
        int testMode = mSpUtils.getWavelengthscanTestMode();
        float limitUp = mSpUtils.getWavelengthscanLimitUp();
        float limitDown = mSpUtils.getWavelengthscanLimitDown();
        float start = mSpUtils.getWavelengthscanStart();
        float end = mSpUtils.getWavelengthscanEnd();
        int speed = mSpUtils.getWavelengthscanSpeed();
        float interval = mSpUtils.getWavelengthscanInterval();

        if(testMode == TEST_MODE_ABS) {
            mTestModeValue.setText(getString(R.string.abs_with_unit));
            //limit up and down
            mLimitUpValue.setText("" + limitUp + " " + getString(R.string.abs_unit));
            mLimitDownValue.setText("" + limitDown + " " + getString(R.string.abs_unit));
        } else if(testMode == TEST_MODE_TRANS) {
            mTestModeValue.setText(getString(R.string.trans_with_unit));
            //limit up and down
            mLimitUpValue.setText("" + limitUp + " " + getString(R.string.trans_unit));
            mLimitDownValue.setText("" + limitDown + " " + getString(R.string.trans_unit));
        } else if(testMode == TEST_MODE_ENERGY) {
            mTestModeValue.setText(getString(R.string.energy));
            //limit up and down
            mLimitUpValue.setText("" + limitUp + " ");
            mLimitDownValue.setText("" + limitDown + " ");
        }

        //start and end
        mStartValue.setText("" + start + " " + getString(R.string.nm));
        mEndValue.setText("" + end + " " + getString(R.string.nm));
        //speed
        mSpeedValue.setText(getResources().getStringArray(R.array.speeds)[speed]);
        //interval
        mIntervalValue.setText("" + interval + " " + getString(R.string.nm));
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.layout_wavelengthscan_reset) {
            Log.d(TAG, "reset");
        } else if(v.getId() == R.id.layout_test_mode) {
            final String[] items = new String[3];
            items[0] = getString(R.string.abs_with_unit);
            items[1] = getString(R.string.trans_with_unit);
            items[2] = getString(R.string.energy);
            showSelectDialog(getString(R.string.title_test_mode), items,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            mTestModeValue.setText(items[which]);
                            mSpUtils.setKeyWavelengthscanTestMode(which);
                            if(which == TEST_MODE_ABS) {
                                mSpUtils.setKeyWavelengthscanLimitUp(Utils.DEFAULT_ABS_VALUE);
                                mSpUtils.setKeyWavelengthscanLimitDown(0.0f);
                            } else if(which == TEST_MODE_TRANS) {
                                mSpUtils.setKeyWavelengthscanLimitUp(Utils.DEFAULT_TRANS_VALUE);
                                mSpUtils.setKeyWavelengthscanLimitDown(0.0f);
                            } else if(which == TEST_MODE_ENERGY) {
                                mSpUtils.setKeyWavelengthscanLimitUp(Utils.DEFAULT_ENERGY_VALUE);
                                mSpUtils.setKeyWavelengthscanLimitDown(0.0f);
                            }
                            //update limit up and dow
                            loadPreference();

                        }
                    });
        } else if(v.getId() == R.id.layout_limit_up) {
            mDialog.init(v.getId(), getString(R.string.title_wavelength_y),
                    getString(R.string.title_limit_up), this);
            mDialog.show(getFragmentManager(), "limit_up");
        } else if(v.getId() == R.id.layout_limit_down) {
            mDialog.init(v.getId(), getString(R.string.title_wavelength_y),
                    getString(R.string.title_limit_down), this);
            mDialog.show(getFragmentManager(), "limit_down");
        } else if(v.getId() == R.id.layout_wavelength_start) {
            mDialog.init(v.getId(), getString(R.string.title_wavelength_x),
                    getString(R.string.title_wavelength_start), this);
            mDialog.show(getFragmentManager(), "wavelength_start");
        } else if(v.getId() == R.id.layout_wavelength_end) {
            mDialog.init(v.getId(), getString(R.string.title_wavelength_x),
                    getString(R.string.title_wavelength_end), this);
            mDialog.show(getFragmentManager(), "wavelength_end");
        } else if(v.getId() == R.id.layout_wavelengthscan_speed) {
            final String[] items = getResources().getStringArray(R.array.speeds);
            showSelectDialog(getString(R.string.title_scan_speed), items,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            mSpeedValue.setText(items[which]);
                            mSpUtils.setKeyWavelengthscanSpeed(which);
                            loadPreference();
                        }
                    });
        } else if(v.getId() == R.id.layout_wavelength_interval) {
            final String[] items = getResources().getStringArray(R.array.intervals);
            showSelectDialog(getString(R.string.title_wavelength_interval), items,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            mIntervalValue.setText(items[which] + " " + getString(R.string.nm));
                            mSpUtils.setKeyWavelengthscanInterval(Float.parseFloat(items[which]));
                            loadPreference();
                        }
                    });
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
        if(setting.length() < 1) {
            Toast.makeText(this, getString(R.string.notice_edit_null), Toast.LENGTH_SHORT).show();
            return;
        }
        switch (index) {
            case R.id.layout_limit_up:
                if(mSpUtils.getWavelengthscanTestMode() == TEST_MODE_ABS) {
                    mLimitUpValue.setText(setting + " " + getString(R.string.abs_unit));
                } else if(mSpUtils.getWavelengthscanTestMode() == TEST_MODE_TRANS) {
                    mLimitUpValue.setText(setting + " " + getString(R.string.trans_unit));
                } else if(mSpUtils.getWavelengthscanTestMode() == TEST_MODE_ENERGY) {
                    mLimitUpValue.setText(setting + " ");
                }
                mSpUtils.setKeyWavelengthscanLimitUp(Float.parseFloat(setting));
                break;
            case R.id.layout_limit_down:
                if(mSpUtils.getWavelengthscanTestMode() == TEST_MODE_ABS) {
                    mLimitDownValue.setText(setting + " " + getString(R.string.abs_unit));
                } else if(mSpUtils.getWavelengthscanTestMode() == TEST_MODE_TRANS) {
                    mLimitDownValue.setText(setting + " " + getString(R.string.trans_unit));
                } else if(mSpUtils.getWavelengthscanTestMode() == TEST_MODE_ENERGY) {
                    mLimitDownValue.setText(setting + " ");
                }
                mSpUtils.setKeyWavelengthscanLimitDown(Float.parseFloat(setting));
                break;
            case R.id.layout_wavelength_start:
                float wl = Float.parseFloat(setting);
                if(Utils.checkWavelengthInvalid(this, wl)) {
                    mStartValue.setText(setting + " " + getString(R.string.nm));
                    mSpUtils.setKeyWavelengthscanStart(wl);
                } else {
                    mStartValue.setText("" + mSpUtils.getWavelengthscanStart() + " " + getString(R.string.nm));
                    mSpUtils.setKeyWavelengthscanStart(mSpUtils.getWavelengthscanStart());
                }

                break;
            case R.id.layout_wavelength_end:
                wl = Float.parseFloat(setting);
                if(Utils.checkWavelengthInvalid(this, wl)) {
                    mEndValue.setText(setting + " " + getString(R.string.nm));
                    mSpUtils.setKeyWavelengthscanEnd(wl);
                } else {
                    mEndValue.setText("" + mSpUtils.getWavelengthscanEnd() + " " + getString(R.string.nm));
                    mSpUtils.setKeyWavelengthscanEnd(mSpUtils.getWavelengthscanStart());
                }
                break;
            default:
                break;
        }
    }
}
