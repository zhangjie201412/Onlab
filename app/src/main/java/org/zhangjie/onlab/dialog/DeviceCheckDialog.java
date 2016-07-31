package org.zhangjie.onlab.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.zhangjie.onlab.R;
import org.zhangjie.onlab.device.DeviceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2016/7/30.
 */
public class DeviceCheckDialog extends DialogFragment {
    private ListView mListView;
    private SimpleAdapter mAdapter;
    private List<HashMap<String, String>> mData;
    private Context mContext;
    private boolean canSkipWarm = false;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_device_check, null);

        mData = new ArrayList<>();
        mListView = (ListView)view.findViewById(R.id.dialog_lv_check_item);
        mAdapter = new SimpleAdapter(getActivity(), mData, R.layout.item_device_check,
                new String[] {"check"}, new int[] {R.id.item_check});
        mListView.setAdapter(mAdapter);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle(R.string.device_selfcheck);
//        builder.setNeutralButton(R.string.skip_warm, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                if(canSkipWarm)
//                    DeviceManager.getInstance().skip();
//            }
//        });
        builder.setView(view);
        builder.setCancelable(true);
        clear();
        addItem(getString(R.string.self_checking));
        canSkipWarm = false;

        return builder.create();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setCancelable(false);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void clear() {
        mData.clear();
        mAdapter.notifyDataSetChanged();
    }

    public void warm() {
        canSkipWarm = true;
    }

    public void addItem(String msg) {
        if (mData == null)
            return;

        HashMap<String, String> item = new HashMap<String, String>();
        item.put("check", msg);

        mData.add(item);
        mAdapter.notifyDataSetChanged();
    }
}
