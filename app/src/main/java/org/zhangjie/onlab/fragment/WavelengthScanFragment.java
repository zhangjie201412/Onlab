package org.zhangjie.onlab.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import org.zhangjie.onlab.R;

/**
 * Created by H151136 on 5/24/2016.
 */
public class WavelengthScanFragment extends Fragment implements  View.OnClickListener {

    private static final String TAG = "Onlab.WavelengthScan";
    private ListView mListView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wavelength_scan, container, false);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_photometric_measure_start:
                break;
            default:
                break;
        }
    }
}
