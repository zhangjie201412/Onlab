package org.zhangjie.onlab.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import org.zhangjie.onlab.R;
import org.zhangjie.onlab.adapter.NineGridAdapter;
import org.zhangjie.onlab.dialog.WavelengthDialog;
import org.zhangjie.onlab.view.NineGridView;

/**
 * Created by H151136 on 5/24/2016.
 */
public class MainFragment extends Fragment {
    private static final String TAG = "Onlab.MainFragment";
    private FragmentCallbackListener mListener;
    private NineGridView mGridView;

    public static final int ITEM_PHOTOMETRIC_MEASURE = 0;
    public static final int ITEM_QUANTITATIVE_ANALYSIS = 1;
    public static final int ITEM_WAVELENGTH_SCAN = 2;
    public static final int ITEM_TIME_SCAN = 3;
    public static final int ITEM_MULTI_WAVELENGTH = 4;

    @Override
    public void onAttach(Context context) {
        mListener = (FragmentCallbackListener) context;
        super.onAttach(context);
    }

    @Override
    public void onAttach(Activity activity) {
        mListener = (FragmentCallbackListener) activity;
        super.onAttach(activity);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mGridView = (NineGridView) view.findViewById(R.id.ngv);
        mGridView.setAdapter(new NineGridAdapter(getActivity()));

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case ITEM_PHOTOMETRIC_MEASURE:
                        break;
                    case ITEM_QUANTITATIVE_ANALYSIS:
                        break;
                    case ITEM_WAVELENGTH_SCAN:
                        break;
                    case ITEM_TIME_SCAN:
                        break;
                    case ITEM_MULTI_WAVELENGTH:
                        break;
                    default:
                        break;
                }
                if (getFragmentManager().getBackStackEntryCount() <= 1) {
                    mListener.onMainClick(position);
                }
            }
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
//        getFragmentManager().beginTransaction().remove(this).commit();
    }
}
