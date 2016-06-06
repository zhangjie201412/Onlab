package org.zhangjie.onlab.setting;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;

import org.zhangjie.onlab.R;

/**
 * Created by H151136 on 6/6/2016.
 */
public class TimescanSettingActivity extends Activity{

    private TimescanSettingLeft mLeft;
    private TimescanSettingRight mRight;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_fragment_timescan);
        mLeft = new TimescanSettingLeft();
        mRight = new TimescanSettingRight();

        getFragmentManager().beginTransaction().replace(R.id.layout_setting_left, mLeft).commit();
        getFragmentManager().beginTransaction().replace(R.id.layout_setting_right, mRight).commit();
    }

    public static class TimescanSettingLeft extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{
        private EditTextPreference mWavelengthPreference;
        private EditTextPreference mStartPreference;
        private EditTextPreference mEndPreference;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference_timescan_left);

            mWavelengthPreference = (EditTextPreference)getPreferenceScreen().
                    findPreference(getString(R.string.key_timescan_setting_work_wavelength));
            mStartPreference = (EditTextPreference)getPreferenceScreen().
                    findPreference(getString(R.string.key_timescan_setting_start));
            mEndPreference = (EditTextPreference)getPreferenceScreen().
                    findPreference(getString(R.string.key_timescan_setting_end));
        }

        private void initView() {
            if(!mWavelengthPreference.getText().equals("")) {
                mWavelengthPreference.setSummary(mWavelengthPreference.getText());
            }
            if(!mStartPreference.getText().equals("")) {
                mStartPreference.setSummary(mStartPreference.getText());
            }
            if(!mEndPreference.getText().equals("")) {
                mEndPreference.setSummary(mEndPreference.getText());
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            initView();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if(key.equals(getString(R.string.key_timescan_setting_work_wavelength))) {
                mWavelengthPreference.setSummary(mWavelengthPreference.getText());
            } else if(key.equals(getString(R.string.key_timescan_setting_start))) {
                mStartPreference.setSummary(mStartPreference.getText());
            } else if(key.equals(getString(R.string.key_timescan_setting_end))) {
                mEndPreference.setSummary(mEndPreference.getText());
            }
        }
    }

    public static class TimescanSettingRight extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
//            addPreferencesFromResource(R.xml.preference_timescan_left);
        }
    }

}
