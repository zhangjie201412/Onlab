package org.zhangjie.onlab.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;
import org.zhangjie.onlab.DeviceApplication;
import org.zhangjie.onlab.R;
import org.zhangjie.onlab.adapter.MultipleWavelengthSettingAdapter;
import org.zhangjie.onlab.ble.BtleManager;
import org.zhangjie.onlab.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by H151136 on 5/24/2016.
 */
public class MultipleWavelengthSettingDialog extends DialogFragment implements SettingEditDialog.SettingInputListern {

    private ListView mListView;
    private MultipleWavelengthSettingAdapter mAdapter;
    private List<HashMap<String, String>> mData;
    private TextView mAddTextView;
    private SettingEditDialog mDialog;
    private MultipleWavelengthSettingCallback mCallback;

//    private Context mContext;
//
//    public MultipleWavelengthSettingDialog(Context context) {
//        mContext = context;
//    }

    public void init(MultipleWavelengthSettingCallback callback) {
        mCallback = callback;
    }

    public interface MultipleWavelengthSettingCallback {
        public void onCallback(float[] wavelengths);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_multiple_wavelength_setting, null);
        mDialog = new SettingEditDialog();

        mAddTextView = (TextView) view.findViewById(R.id.tv_add);
        mAddTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.init(-1, getString(R.string.multi_wavelength_setting),
                        getString(R.string.wavelength), MultipleWavelengthSettingDialog.this);
                mDialog.show(getFragmentManager(), "wavelength");
            }
        });
        mAddTextView.setVisibility(View.INVISIBLE);

        mData = new ArrayList<HashMap<String, String>>();
        mListView = (ListView) view.findViewById(R.id.dialog_lv_multiple_wavelength);
        mAdapter = new MultipleWavelengthSettingAdapter(getActivity(), mData);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int pos;
                if(position == (mData.size() - 1)) {
                    pos = -1;
                } else {
                    pos = position;
                }
                mDialog.init(pos, getString(R.string.multi_wavelength_setting),
                        getString(R.string.wavelength), MultipleWavelengthSettingDialog.this);
                mDialog.show(getFragmentManager(), "wavelength");
            }
        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if(position < (mData.size() - 1)) {
                    showDeleteDialog(position);
                }
                return true;
            }
        });

        builder.setView(view).setNegativeButton(getString(R.string.cancel_string), null)
                .setTitle(getString(R.string.multi_wavelength_setting)).setIcon(R.mipmap.ic_launcher);
        builder.setPositiveButton(R.string.ok_string, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int count = mData.size() - 1;
                //check all the data
                for (int i = 0; i < count; i++) {
                    String value = mData.get(i).get("wavelength");
                    if (value.length() < 1) {
                        Toast.makeText(getActivity(), getString(R.string.notice_edit_null), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                if (count > 0) {
                    float[] wavelengths = new float[count];
                    for (int i = 0; i < count; i++) {
                        wavelengths[i] = Float.parseFloat(mData.get(i).get("wavelength"));
                    }
                    mCallback.onCallback(wavelengths);
                } else {
//                    Toast.makeText(getActivity(), getString(R.string.notice_edit_null), Toast.LENGTH_SHORT).show();
                }
            }
        });
        addItem();

        int length = DeviceApplication.getInstance().getSpUtils().getMultipleWavelengthLength();
        float[] wavelengths = DeviceApplication.getInstance().getSpUtils().getMultipleWavelength();

        for (int i = 0; i < length; i++) {
            addItem("" + wavelengths[i]);
        }

        return builder.create();
    }

    private void addItem() {
        HashMap<String, String> item = new HashMap<String, String>();
        item.put("wavelength", "");
        mData.add(item);
        mAdapter.notifyDataSetChanged();
        if (mData.size() > 0) {
            mListView.setSelection(mData.size() - 1);
        }
    }

    private void addItem(String wavelength) {
        HashMap<String, String> item = new HashMap<String, String>();
        item.put("wavelength", wavelength);
        mData.remove(mData.size() - 1);
        mData.add(item);
        mAdapter.notifyDataSetChanged();
        if (mData.size() > 0) {
            mListView.setSelection(mData.size() - 1);
        }
        addItem();
    }

    private void showDeleteDialog(final int index) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.notice_delete);
        builder.setMessage(R.string.sure_to_delete);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setPositiveButton(R.string.ok_string, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mData.remove(index);
                mAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton(R.string.cancel_string, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create().show();
    }

    @Override
    public void onSettingInputComplete(int index, String wavelength) {
        Log.d("####", "index - " + index + ", " + wavelength);
        if (wavelength.length() < 1) {
            Toast.makeText(getActivity(), getString(R.string.notice_edit_null), Toast.LENGTH_SHORT).show();
            return;
        }

        float wl = Float.parseFloat(wavelength);
        if (Utils.checkWavelengthInvalid(getActivity(), wl)) {
            if (index == -1) {
                addItem(wavelength);
            } else {
                mData.get(index).put("wavelength", wavelength);
            }
            mAdapter.notifyDataSetChanged();
        }
    }
}
