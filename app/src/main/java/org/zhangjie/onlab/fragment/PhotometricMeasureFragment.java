package org.zhangjie.onlab.fragment;

import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.squareup.otto.Produce;

import org.zhangjie.onlab.otto.BusProvider;
import org.zhangjie.onlab.R;
import org.zhangjie.onlab.adapter.MultiSelectionAdapter;
import org.zhangjie.onlab.otto.SetWavelengthEvent;
import org.zhangjie.onlab.record.PhotoMeasureRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by H151136 on 5/24/2016.
 */
public class PhotometricMeasureFragment extends Fragment implements  View.OnClickListener {

    private boolean isFake = true;
    private static final String TAG = "Onlab.PhotometricMea";
    private ListView mListView;
    private MultiSelectionAdapter mAdapter;
    private List<HashMap<String, String>> mData;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photometric_measure, container, false);
        initUi(view);
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
        if(Build.VERSION.SDK_INT >= 23) {
            mAdapter = new MultiSelectionAdapter(getContext(), mData,
                    R.layout.item_photometric_measure,
                    new String[] {"id", "wavelength", "abs", "trans", "energy"},
                    new int[] {R.id.item_index, R.id.item_wavelength,
                    R.id.item_abs, R.id.item_trans, R.id.item_energy});
        } else {
            mAdapter = new MultiSelectionAdapter(getActivity(), mData,
                    R.layout.item_photometric_measure,
                    new String[] {"id", "wavelength", "abs", "trans", "energy"},
                    new int[] {R.id.item_index, R.id.item_wavelength,
                            R.id.item_abs, R.id.item_trans, R.id.item_energy});
        }
        mListView = (ListView)view.findViewById(R.id.lv_photometric_measure);
        mListView.setAdapter(mAdapter);

        Button start = (Button)view.findViewById(R.id.bt_photometric_measure_start);
        Button setting = (Button)view.findViewById(R.id.bt_photometric_measure_setting);
        start.setOnClickListener(this);
        setting.setOnClickListener(this);
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
        item.put("abs", String.format("%.3f", record.getAbs()));
        item.put("trans", "" + record.getTrans());
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_photometric_measure_start:
                if(isFake) {
                    int energy = (int)(Math.random() * 1000.0f);
                    float wavelength = (float)(Math.random() * 1000.0f);
                    float abs = (float)(Math.random() * 10);
                    float trans = (float)(Math.random() * 100);

                    PhotoMeasureRecord record = new PhotoMeasureRecord(-1,
                            wavelength, abs, trans, energy,
                            System.currentTimeMillis());
                    addItem(record);
                }
                break;
            case R.id.bt_photometric_measure_setting:
                Log.d(TAG, "photometric_measure_setting");
                BusProvider.getInstance().post(new SetWavelengthEvent());
                break;
            default:
                break;
        }
    }
}
