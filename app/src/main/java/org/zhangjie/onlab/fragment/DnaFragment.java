package org.zhangjie.onlab.fragment;

import android.app.Fragment;
import android.database.DataSetObserver;
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
import org.zhangjie.onlab.dialog.DnaSettingDialog;
import org.zhangjie.onlab.dialog.SaveNameDialog;
import org.zhangjie.onlab.otto.BusProvider;
import org.zhangjie.onlab.otto.DnaCallbackEvent;
import org.zhangjie.onlab.otto.SettingEvent;
import org.zhangjie.onlab.record.DnaRecord;
import org.zhangjie.onlab.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by H151136 on 5/24/2016.
 */
public class DnaFragment extends Fragment implements DnaSettingDialog.OnDnaSettingListener, View.OnClickListener {

    private static final String TAG = "Onlab.Dna";
    private DnaSettingDialog mDnaSettingDialog;
    private Button mStartTestButton;
    private Button mRezeroButton;

    private ListView mListView;
    private MultiSelectionAdapter mAdapter;
    private List<HashMap<String, String>> mData;
    private boolean mIsRezeroed = false;
    private SaveNameDialog mSaveDialog;
    private SaveNameDialog mFileExportDialog;

    public static float refWavelength;
    public static float wavelength1;
    public static float wavelength2;

    private float mAbs1;
    private float mAbs2;
    private float mAbsRef;

    private float mF1;
    private float mF2;
    private float mF3;
    private float mF4;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dna, container, false);

        initView(view);
        mDnaSettingDialog = new DnaSettingDialog();
        mDnaSettingDialog.show(getFragmentManager(), "dna");
        mDnaSettingDialog.setListener(this);
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

    void initView(View view) {
        mStartTestButton = (Button) view.findViewById(R.id.bt_dna_start);
        mRezeroButton = (Button) view.findViewById(R.id.bt_dna_rezero);
        mListView = (ListView) view.findViewById(R.id.lv_dna);
        mStartTestButton.setOnClickListener(this);
        mRezeroButton.setOnClickListener(this);
        mStartTestButton.setEnabled(false);
        mRezeroButton.setEnabled(false);
        mData = new ArrayList<HashMap<String, String>>();
        mAdapter = new MultiSelectionAdapter(getActivity(), mData,
                R.layout.item_dna,
                new String[]{"id", "wavelength", "abs", "trans", "energy"},
                new int[]{R.id.item_index, R.id.item_sample_name,
                        R.id.item_abs1, R.id.item_abs2, R.id.item_abs_ref,
                        R.id.item_dna, R.id.item_protein});
        mListView.setAdapter(mAdapter);
        mAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                Log.d(TAG, "onChanged!!");
                if (mData.size() > 0) {
                    Utils.needToSave = true;
                } else {
                    Utils.needToSave = false;
                }
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                boolean mode = ((MainActivity) getActivity()).getOperateMode();
                if (mode) {
                    MultiSelectionAdapter.ViewHolder holder = (MultiSelectionAdapter.ViewHolder) view.getTag();
                    holder.cb.toggle();
                    mAdapter.getIsSelected().put(position, holder.cb.isChecked());
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void addItem(DnaRecord record) {
        HashMap<String, String> item = new HashMap<String, String>();
        int no = mData.size() + 1;
        record.setIndex(no);

        item.put("id", "" + no);
        item.put("name", "" + record.getName());
        item.put("wavelength1", "" + record.getWavelength1());
        item.put("wavelength2", "" + record.getWavelength2());
        item.put("wavelengthRef", "" + record.getWavelengthRef());
        item.put("dna", Utils.formatConc(record.getDna()));
        item.put("protein", Utils.formatConc(record.getProtein()));
        item.put("date", "" + record.getDate());
        mData.add(item);
        mAdapter.add();
        mAdapter.notifyDataSetChanged();
        if (mData.size() > 0) {
            mListView.setSelection(mData.size() - 1);
        }
    }

    @Subscribe
    public void OnSettingEvent(SettingEvent event) {
        mDnaSettingDialog.show(getFragmentManager(), "dna");
    }

    @Subscribe
    public void OnUpdateEvent(DnaCallbackEvent event) {
        if(event.event_type == DnaCallbackEvent.EVENT_TYPE_UPDATE) {
            float wl = event.wavelength;
            float abs = event.abs;
            if(wl == wavelength1) {
                mAbs1 = abs;
            } else if(wl == wavelength2) {
                mAbs2 = abs;
            } else if(wl == refWavelength) {
                mAbsRef = abs;
            }
        } else if(event.event_type == DnaCallbackEvent.EVENT_TYPE_REZERO_DONE) {
            mStartTestButton.setEnabled(true);
        } else if(event.event_type == DnaCallbackEvent.EVENT_TYPE_TEST_DONE) {
            float dna = (mAbs1 - mAbsRef) * mF1 - (mAbs2 - mAbsRef) * mF2;
            float protein = (mAbs2 - mAbsRef) * mF3 - (mAbs1 - mAbsRef) * mF4;
            addItem(new DnaRecord(-1, "",mAbs1, mAbs2, mAbsRef, dna, protein, System.currentTimeMillis()));
        }
    }

    @Override
    public void onDnaSettingCallback(DnaSettingDialog.DnaSettingParam param, int error) {
        if (error != 0) {
            Toast.makeText(getActivity(), R.string.notice_edit_null, Toast.LENGTH_SHORT).show();
            mRezeroButton.setEnabled(false);
        } else {
            //get parameter
            Log.d(TAG, "wavelength1 = " + param.wavelength1);
            Log.d(TAG, "wavelength2 = " + param.wavelength2);
            Log.d(TAG, "wavelengthRef = " + param.wavelengthRef);
            Log.d(TAG, "F1 = " + param.f1);
            Log.d(TAG, "F2 = " + param.f2);
            Log.d(TAG, "F3 = " + param.f3);
            Log.d(TAG, "F4 = " + param.f4);
            mRezeroButton.setEnabled(true);
            refWavelength = param.wavelengthRef;
            wavelength1 = param.wavelength1;
            wavelength2 = param.wavelength2;
            mF1 = param.f1;
            mF2 = param.f2;
            mF3 = param.f3;
            mF4 = param.f4;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_dna_rezero:
                BusProvider.getInstance().post(new DnaCallbackEvent(
                        DnaCallbackEvent.EVENT_TYPE_DO_REZERO, wavelength1,
                        wavelength2, refWavelength));
                break;
            case R.id.bt_dna_start:
                BusProvider.getInstance().post(new DnaCallbackEvent(
                        DnaCallbackEvent.EVENT_TYPE_DO_TEST, wavelength1,
                        wavelength2, refWavelength));
                break;
            default:
                break;
        }

    }
}
