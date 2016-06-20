package org.zhangjie.onlab.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import org.zhangjie.onlab.MainActivity;
import org.zhangjie.onlab.R;
import org.zhangjie.onlab.adapter.MultiSelectionAdapter;
import org.zhangjie.onlab.device.DeviceManager;
import org.zhangjie.onlab.dialog.MultipleWavelengthSettingDialog;
import org.zhangjie.onlab.otto.BusProvider;
import org.zhangjie.onlab.otto.SetOperateEvent;
import org.zhangjie.onlab.otto.SetOperateModeEvent;
import org.zhangjie.onlab.otto.SetWavelengthEvent;
import org.zhangjie.onlab.otto.SettingEvent;
import org.zhangjie.onlab.otto.UpdateFragmentEvent;
import org.zhangjie.onlab.otto.WaitProgressEvent;
import org.zhangjie.onlab.record.PhotoMeasureRecord;
import org.zhangjie.onlab.setting.TimescanSettingActivity;
import org.zhangjie.onlab.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by H151136 on 5/24/2016.
 */
public class MultipleWavelengthFragment extends Fragment implements View.OnClickListener, MultipleWavelengthSettingDialog.MultipleWavelengthSettingCallback {
    private static final String TAG = "Onlab.MultipleWave";
    private ListView mListView;
    private MultiSelectionAdapter mAdapter;
    private List<HashMap<String, String>> mData;
    private Button mStart;
    private Button mRezero;

    private MultipleWavelengthSettingDialog mSettingDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_multiple_wavelength, container, false);
        initUi(view);
        mSettingDialog = new MultipleWavelengthSettingDialog();
        mSettingDialog.init(this);
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

    void initUi(View view) {
        mData = new ArrayList<HashMap<String, String>>();
        if (Build.VERSION.SDK_INT >= 23) {
            mAdapter = new MultiSelectionAdapter(getContext(), mData,
                    R.layout.item_photometric_measure,
                    new String[]{"id", "wavelength", "abs", "trans", "energy"},
                    new int[]{R.id.item_index, R.id.item_wavelength,
                            R.id.item_abs, R.id.item_trans, R.id.item_energy});
        } else {
            mAdapter = new MultiSelectionAdapter(getActivity(), mData,
                    R.layout.item_photometric_measure,
                    new String[]{"id", "wavelength", "abs", "trans", "energy"},
                    new int[]{R.id.item_index, R.id.item_wavelength,
                            R.id.item_abs, R.id.item_trans, R.id.item_energy});
        }
        mListView = (ListView) view.findViewById(R.id.lv_multiple_wavelength);
        mListView.setAdapter(mAdapter);

        mStart = (Button) view.findViewById(R.id.bt_multiple_wavelength_start);
        mRezero = (Button) view.findViewById(R.id.bt_multiple_wavelength_rezero);
        mStart.setOnClickListener(this);
        mRezero.setOnClickListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    private void addItem(PhotoMeasureRecord record) {
        HashMap<String, String> item = new HashMap<String, String>();
        int no = mData.size() + 1;
        record.setIndex(no);

        item.put("id", "" + no);
        item.put("wavelength", "" + record.getWavelength());
        item.put("abs", Utils.formatAbs(record.getAbs()));
        item.put("trans", Utils.formatTrans(record.getTrans()));
        item.put("energy", "" + record.getEnergy());
        item.put("date", "" + record.getDate());
        mData.add(item);
        mAdapter.add();
        mAdapter.notifyDataSetChanged();
        if (mData.size() > 0) {
            mListView.setSelection(mData.size() - 1);
        }
    }

    private void removeItem(int position) {
        mData.remove(position);
        for (int i = 0; i < mData.size(); i++) {
            HashMap<String, String> item = mData.get(i);
            item.put("id", "" + (i + 1));
        }
        mAdapter.notifyDataSetChanged();
    }

    @Subscribe
    public void OnSettingEvent(SettingEvent event) {
        Context context = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context = getContext();
        } else {
            context = getActivity();
        }

        mSettingDialog.show(getFragmentManager(), getString(R.string.multi_wavelength_setting));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_multiple_wavelength_start:

                break;
            case R.id.bt_multiple_wavelength_rezero:

                break;

            default:
                break;
        }
    }

    @Override
    public void onCallback(float[] wavelengths) {
        for(int i = 0; i < wavelengths.length; i++) {
            Log.d(TAG, String.format("[%d] -> %f\n", i, wavelengths[i]));
        }
    }
}
