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

import org.zhangjie.onlab.R;
import org.zhangjie.onlab.utils.Utils;

/**
 * Created by H151136 on 8/2/2016.
 */
public class LightMgrDialog extends DialogFragment implements View.OnClickListener, WavelengthDialog.WavelengthInputListern {
    private SwitchCompat mDeuteriumSwitcher;
    private SwitchCompat mTungstenSwitcher;
    private RelativeLayout mDeuteriumClear;
    private RelativeLayout mTungstenClear;
    private RelativeLayout mSwitchWavelength;
    private WavelengthDialog mWavelengthDialog;

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

        mDeuteriumClear.setOnClickListener(this);
        mTungstenClear.setOnClickListener(this);
        mSwitchWavelength.setOnClickListener(this);

        mDeuteriumSwitcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d("####", "mDeuteriumSwitcher = " + isChecked);
            }
        });

        mTungstenSwitcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d("####", "mTungstenSwitcher = " + isChecked);
            }
        });

        mWavelengthDialog = new WavelengthDialog();
        mWavelengthDialog.setListener(this);

        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_tungsten_clear_time:
                Log.d("####", "layout_tungsten_clear_time");
                Utils.showAlertDialog(getActivity(), getString(R.string.notice), getString(R.string.tungsten_clear_time),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                break;
            case R.id.layout_deuteruim_clear_time:
                Log.d("####", "layout_deuteruim_clear_time");
                Utils.showAlertDialog(getActivity(), getString(R.string.notice), getString(R.string.deuteruim_clear_time),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                break;
            case R.id.layout_switch_wavelength:
                Log.d("####", "layout_deuteruim_clear_time");
                mWavelengthDialog.show(getFragmentManager(), "wavelength setting");
                break;
        }
    }

    @Override
    public void onWavelengthInputComplete(String wavelength) {

    }
}
