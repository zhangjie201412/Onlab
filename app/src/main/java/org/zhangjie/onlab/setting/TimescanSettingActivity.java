package org.zhangjie.onlab.setting;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import org.zhangjie.onlab.R;

/**
 * Created by H151136 on 6/6/2016.
 */
public class TimescanSettingActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Onlab.TimescanSetting";
    private Toolbar mToolbar;
    private LinearLayout mResetLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_fragment_timescan);
        initToolbar();
    }

    private void initToolbar() {
        mToolbar = (Toolbar)findViewById(R.id.tb_timescan_setting);
        setSupportActionBar(mToolbar);

        mResetLayout = (LinearLayout)findViewById(R.id.layout_timescan_reset);
        mResetLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.layout_timescan_reset) {
            Log.d(TAG, "reset");
        }
    }
}
