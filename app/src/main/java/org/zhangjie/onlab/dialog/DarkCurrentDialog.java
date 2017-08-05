package org.zhangjie.onlab.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.zhangjie.onlab.DeviceApplication;
import org.zhangjie.onlab.R;
import org.zhangjie.onlab.adapter.MultiSelectionAdapter;
import org.zhangjie.onlab.device.DeviceManager;
import org.zhangjie.onlab.record.BaselineRecord;
import org.zhangjie.onlab.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by H151136 on 5/24/2016.
 */
public class DarkCurrentDialog extends DialogFragment {

    private ListView mListView;
    private int[] mDark;
    private int[] mDarkRef;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_reset_dark, null);

        initUi(view);
        builder.setView(view);
        return builder.create();
    }

    private void initUi(View view) {
        mListView = (ListView) view.findViewById(R.id.lv_dialog_dark);
        SimpleAdapter adapter = new SimpleAdapter(getActivity(),
                getData(), R.layout.item_dark_current,
                new String[]{"gain", "dark_current", "reference_dark_current"},
                new int[]{R.id.item_gain, R.id.item_sample_dark_current,
                        R.id.item_reference_dark_current});
        mListView.setAdapter(adapter);
    }

    public void setData(int[] dark, int[] darkRef) {
        mDark = dark;
        mDarkRef = darkRef;
    }

    private List<Map<String, String>> getData() {
        List<Map<String, String>> list = new ArrayList<>();

        for (int i = 0; i < mDark.length; i++) {
            Map<String, String> map = new HashMap<>();
            map.put("gain", "" + (i + 1));
            map.put("dark_current", "" + mDark[i]);
            map.put("reference_dark_current", "" + mDarkRef[i]);
            list.add(map);
        }
        return list;
    }
}
