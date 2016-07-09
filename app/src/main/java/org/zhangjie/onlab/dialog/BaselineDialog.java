package org.zhangjie.onlab.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import org.zhangjie.onlab.R;
import org.zhangjie.onlab.adapter.MultiSelectionAdapter;
import org.zhangjie.onlab.record.PhotoMeasureRecord;
import org.zhangjie.onlab.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by H151136 on 5/24/2016.
 */
public class BaselineDialog extends DialogFragment {

    private ListView mListView;
    private Button mStart;
    private Button mStop;
    private boolean stop = true;

    private MultiSelectionAdapter mAdapter;
    private List<HashMap<String, String>> mData;

    private BaselineOperateListener mListener;

    public interface BaselineOperateListener {
        void onStart();
        void onStop();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_baseline, null);

        initUi(view);
        builder.setView(view);
        return builder.create();
    }

    private void initUi(View view) {
        mData = new ArrayList<HashMap<String, String>>();
        mListView = (ListView)view.findViewById(R.id.lv_baseline);
        mAdapter = new MultiSelectionAdapter(getActivity(), mData, R.layout.item_baseline,
                new String[] {"id", "wavelength", "gain", "energy"},
                new int[] {R.id.item_index, R.id.item_wavelength, R.id.item_gain,
                R.id.item_energy});
        mListView.setAdapter(mAdapter);

        mStart = (Button)view.findViewById(R.id.bt_baseline_start);
        mStop = (Button)view.findViewById(R.id.bt_baseline_stop);

        mStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(mListener != null) {
                    mListener.onStart();
                    stop = false;
                    mStart.setEnabled(false);
                }
            }
        });
        mStop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(mListener != null) {
                    mListener.onStop();
                    stop = true;
                    mStart.setEnabled(true);
                }
            }
        });
    }

    public void doneCallback() {
        mStart.setEnabled(true);
    }

    public void setListener(BaselineOperateListener listener) {
        mListener = listener;
    }

    public void addItem(float wavelength, int gain, int energy) {
        HashMap<String, String> item = new HashMap<String, String>();
        int no = mData.size() + 1;

        item.put("id", "" + no);
        item.put("wavelength", "" + wavelength);
        item.put("gain", "" + gain);
        item.put("energy", "" + energy);
        mData.add(item);
        mAdapter.add();
        mAdapter.notifyDataSetChanged();
        if (mData.size() > 0) {
            mListView.setSelection(mData.size() - 1);
        }
    }

    public void clear() {
        mData.clear();
        mAdapter.notifyDataSetChanged();
    }
}
