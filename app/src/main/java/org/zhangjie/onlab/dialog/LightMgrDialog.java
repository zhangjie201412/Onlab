package org.zhangjie.onlab.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.zhangjie.onlab.DeviceApplication;
import org.zhangjie.onlab.R;
import org.zhangjie.onlab.device.DeviceManager;
import org.zhangjie.onlab.utils.Utils;

/**
 * Created by H151136 on 8/2/2016.
 */
public class LightMgrDialog extends DialogFragment implements View.OnClickListener, WavelengthDialog.WavelengthInputListern {
    private static final String TAG = "Onlab.LightMgr";
    private SwitchCompat mDeuteriumSwitcher;
    private SwitchCompat mTungstenSwitcher;
    private RelativeLayout mDeuteriumClear;
    private RelativeLayout mTungstenClear;
    private RelativeLayout mSwitchWavelength;
    private TextView mLampWavelengthTextView;
    private WavelengthDialog mWavelengthDialog;
    private float mLampWavelength = 0.0f;
    private boolean mD2Status;
    private boolean mWuStatus;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_light_mgr, null);

        mDeuteriumSwitcher = (SwitchCompat) view.findViewById(R.id.switch_deuterium);
        mTungstenSwitcher = (SwitchCompat) view.findViewById(R.id.switch_tungsten);
        mDeuteriumClear = (RelativeLayout) view.findViewById(R.id.layout_deuteruim_clear_time);
        mTungstenClear = (RelativeLayout) view.findViewById(R.id.layout_tungsten_clear_time);
        mSwitchWavelength = (RelativeLayout) view.findViewById(R.id.layout_switch_wavelength);
        mLampWavelengthTextView = (TextView)view.findViewById(R.id.tv_lamp_wavelength);

        mDeuteriumClear.setOnClickListener(this);
        mTungstenClear.setOnClickListener(this);
        mSwitchWavelength.setOnClickListener(this);

        mDeuteriumSwitcher.setChecked(mD2Status);
        mTungstenSwitcher.setChecked(mWuStatus);

        mDeuteriumSwitcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "mDeuteriumSwitcher = " + isChecked);
                if(isChecked) {
                    //on
                    DeviceManager.getInstance().doSingleCommand(DeviceManager.DEVICE_CMD_LIST_SET_D2ON);
                } else {
                    //off
                    DeviceManager.getInstance().doSingleCommand(DeviceManager.DEVICE_CMD_LIST_SET_D2OFF);
                }
                mD2Status = isChecked;
                DeviceApplication.getInstance().getSpUtils().setKeyD2Status(isChecked);
            }
        });

        mTungstenSwitcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "mTungstenSwitcher = " + isChecked);
                if(isChecked) {
                    //on
                    DeviceManager.getInstance().doSingleCommand(DeviceManager.DEVICE_CMD_LIST_SET_WUON);
                } else {
                    //off
                    DeviceManager.getInstance().doSingleCommand(DeviceManager.DEVICE_CMD_LIST_SET_WUOFF);
                }
                mWuStatus = isChecked;
                DeviceApplication.getInstance().getSpUtils().setKeyWuStatus(isChecked);
            }
        });

        mWavelengthDialog = new WavelengthDialog();
        mWavelengthDialog.setListener(this);

        mLampWavelength = DeviceApplication.getInstance().getSpUtils().getLampWavelength();
        mLampWavelengthTextView.setText("" + mLampWavelength + getString(R.string.nm));

        mDeuteriumSwitcher.setChecked(DeviceApplication.getInstance().getSpUtils().getD2Status());
        mTungstenSwitcher.setChecked(DeviceApplication.getInstance().getSpUtils().getWuStatus());

        builder.setView(view);
        return builder.create();
    }

    public void setD2Status(boolean on) {
        mD2Status = on;
    }

    public void setmWuStatus(boolean on) {
        mWuStatus = on;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_tungsten_clear_time:
                Log.d(TAG, "layout_tungsten_clear_time");
                Utils.showAlertDialog(getActivity(), getString(R.string.notice), getString(R.string.tungsten_clear_time),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                break;
            case R.id.layout_deuteruim_clear_time:
                Log.d(TAG, "layout_deuteruim_clear_time");
                Utils.showAlertDialog(getActivity(), getString(R.string.notice), getString(R.string.deuteruim_clear_time),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                break;
            case R.id.layout_switch_wavelength:
                Log.d(TAG, "layout_deuteruim_clear_time");
                mWavelengthDialog.show(getFragmentManager(), "wavelength setting");
                break;
        }
    }

    @Override
    public void onWavelengthInputComplete(String wavelength) {
        //get lamp wavelength
        if(wavelength.length() < 1) {
            Toast.makeText(getActivity(), R.string.notice_edit_null, Toast.LENGTH_SHORT).show();
        } else {
            float wl = Float.parseFloat(wavelength);
            Log.d(TAG, "set lamp wavelength = " + wl);
            mLampWavelength = wl;
            DeviceManager.getInstance().setLampWavelengthWork(wl);
            DeviceApplication.getInstance().getSpUtils().setLampWavelength(wl);
            mLampWavelengthTextView.setText("" + mLampWavelength + getString(R.string.nm));
        }
    }
}
