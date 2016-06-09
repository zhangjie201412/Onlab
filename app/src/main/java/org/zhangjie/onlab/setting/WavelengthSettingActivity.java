package org.zhangjie.onlab.setting;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.zhangjie.onlab.DeviceApplication;
import org.zhangjie.onlab.R;
import org.zhangjie.onlab.dialog.SettingEditDialog;

/**
 * Created by H151136 on 6/6/2016.
 */
public class WavelengthSettingActivity extends AppCompatActivity implements View.OnClickListener, SettingEditDialog.SettingInputListern {

    public static final int RESULT_OK = 0;
    public static final int RESULT_CANCEL = 1;

    public static final int TEST_MODE_ABS = 0;
    public static final int TEST_MODE_TRANS = 1;
    public static final int TEST_MODE_ENERGY = 2;

    private static final String TAG = "Onlab.WavelengthSetting";

    private int mMode = 0;

    private Button mOkButton;
    private Button mCancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_fragment_wavelengthscan);
        initView();
    }

    private void initView() {
        loadPreference();

        mOkButton = (Button)findViewById(R.id.bt_setting_wavelengthscan_ok);
        mCancelButton = (Button)findViewById(R.id.bt_setting_wavelengthscan_cancel);
        mOkButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);
    }

    private void loadPreference() {

    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.layout_wavelengthscan_reset) {
            Log.d(TAG, "reset");
        } else if(v.getId() == R.id.bt_setting_wavelengthscan_ok) {
            this.setResult(RESULT_OK);
            this.finish();
        } else if(v.getId() == R.id.bt_setting_wavelengthscan_cancel) {
            this.setResult(RESULT_CANCEL);
            this.finish();
        }
    }

    @Override
    public void onSettingInputComplete(int index, String setting) {
        switch (index) {
            default:
                break;
        }
    }
}
