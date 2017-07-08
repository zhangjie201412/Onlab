package org.zhangjie.onlab.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.zhangjie.onlab.DeviceApplication;
import org.zhangjie.onlab.MainActivity;
import org.zhangjie.onlab.R;
import org.zhangjie.onlab.adapter.MultiSelectionAdapter;
import org.zhangjie.onlab.device.DeviceManager;
import org.zhangjie.onlab.record.BaselineRecord;
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
    private Button mSave;
    private Button mApply;
    private boolean stop = true;

    private MultiSelectionAdapter mAdapter;
    private List<HashMap<String, String>> mData;

    private BaselineOperateListener mListener;
    private SaveNameDialog mSaveBaselineDialog;
    private int mLoadFileId = -1;

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

    public void setLoadFileId(int id) {
        mLoadFileId = id;
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
        mSave = (Button)view.findViewById(R.id.bt_baseline_save);
        mApply = (Button)view.findViewById(R.id.bt_baseline_apply);

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
        mSave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(mData.size() == 0) {
                    Toast.makeText(getActivity(), R.string.notice_save_null, Toast.LENGTH_SHORT).show();
                } else {
                    mSaveBaselineDialog.show(getFragmentManager(), "save_baseline");
                }
//                if(mListener != null) {
//                    mListener.onStop();
//                    stop = true;
//                    mStart.setEnabled(true);
//                }
            }
        });
        mApply.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                for (int i = 0; i < mData.size(); i++) {
                    int index = 0;
                    float wavelength = 0.0f;
                    int gain = 0;
                    int energy = 0;
                    long date = 0;

                    HashMap<String, String> map = mData.get(i);
                    index = Integer.parseInt(map.get("id"));
                    wavelength = Float.parseFloat(map.get("wavelength"));
                    gain = Integer.parseInt(map.get("gain"));
                    energy = Integer.parseInt(map.get("energy"));
                    DeviceManager.getInstance().setGain((int)wavelength, gain);
                }
                DeviceManager.getInstance().saveBaseline();
                Toast.makeText(getActivity(), R.string.apply_done, Toast.LENGTH_SHORT).show();
            }
        });
        mSaveBaselineDialog = new SaveNameDialog();
        mSaveBaselineDialog.init(-1, getString(R.string.action_save), getString(R.string.name), new SaveNameDialog.SettingInputListern() {
            @Override
            public void onSettingInputComplete(int id, String name) {
                if(name.length() < 1 || (!Utils.isValidName(name))) {
                    Toast.makeText(getActivity(), getString(R.string.notice_name_invalid), Toast.LENGTH_SHORT).show();
                    return;
                }

                String fileName = name;
                saveFile(fileName);
            }

            public void abort() {
            }
        });
        mSaveBaselineDialog.setAbort(false);
        if(mLoadFileId >= 0) {
            loadFileById(mLoadFileId);
        }
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

    public void saveFile(String name) {
        DeviceApplication.getInstance().getBaselineDb().saveRecord(name, mData);
    }

    public void loadFileById(int id) {
        mData.clear();
        List<String> fileList = DeviceApplication.getInstance().getBaselineDb().getTables();
        String fileName = fileList.get(id);
        List<BaselineRecord> lists = DeviceApplication.getInstance().getBaselineDb().getRecords(fileName);
        mData.clear();
        for(int i = 0; i < lists.size(); i++) {
            addItem(lists.get(i).getWavelength(), lists.get(i).getGain(), lists.get(i).getEnergy());
        }
    }

    public void clear() {
        mData.clear();
        mAdapter.notifyDataSetChanged();
    }
}
