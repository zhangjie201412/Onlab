package org.zhangjie.onlab.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import org.zhangjie.onlab.DeviceApplication;
import org.zhangjie.onlab.MainActivity;
import org.zhangjie.onlab.R;
import org.zhangjie.onlab.adapter.MultiSelectionAdapter;
import org.zhangjie.onlab.device.DeviceManager;
import org.zhangjie.onlab.dialog.MultipleWavelengthSettingDialog;
import org.zhangjie.onlab.dialog.SaveNameDialog;
import org.zhangjie.onlab.otto.BusProvider;
import org.zhangjie.onlab.otto.FileOperateEvent;
import org.zhangjie.onlab.otto.MultipleWavelengthCallbackEvent;
import org.zhangjie.onlab.otto.SetOperateEvent;
import org.zhangjie.onlab.otto.SetOperateModeEvent;
import org.zhangjie.onlab.otto.SetWavelengthEvent;
import org.zhangjie.onlab.otto.SettingEvent;
import org.zhangjie.onlab.otto.UpdateFragmentEvent;
import org.zhangjie.onlab.otto.WaitProgressEvent;
import org.zhangjie.onlab.record.MultipleWavelengthRecord;
import org.zhangjie.onlab.utils.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Created by H151136 on 5/24/2016.
 */
public class MultipleWavelengthFragment extends Fragment implements View.OnClickListener, MultipleWavelengthSettingDialog.MultipleWavelengthSettingCallback {
    private static final String TAG = "Onlab.MultipleWave";
    private boolean isFake = true;
    private ListView mListView;
    private MultiSelectionAdapter mAdapter;
    private List<HashMap<String, String>> mData;
    private Button mStart;
    private Button mRezero;
    private boolean isRezeroed = false;

    private MultipleWavelengthSettingDialog mSettingDialog;
    public static float[] mWavelengths;
    public static float[] mOrderWavelengths;

    private int mMainIndex = 1;
    private int mSubIndex = 1;
    private SaveNameDialog mSaveDialog;
    private SaveNameDialog mFileExportDialog;

    private boolean loadFile = false;
    private int loadFileIndex = -1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_multiple_wavelength, container, false);
        mMainIndex = 1;
        mSubIndex = 1;
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

        mStart = (Button) view.findViewById(R.id.bt_multiple_wavelength_start);
        mRezero = (Button) view.findViewById(R.id.bt_multiple_wavelength_rezero);
        mStart.setOnClickListener(this);
        mRezero.setOnClickListener(this);
        if (!isFake) {
            mStart.setEnabled(false);
        }
        int length = DeviceApplication.getInstance().getSpUtils().getMultipleWavelengthLength();
        if (length > 0) {
            mWavelengths = DeviceApplication.getInstance().getSpUtils().getMultipleWavelength();
            mOrderWavelengths = new float[length];
            float[] temp = new float[length];
            for (int i = 0; i < length; i++) {
                temp[i] = mWavelengths[i];
            }
            Arrays.sort(temp);
            for (int i = 0; i < length; i++) {
                mOrderWavelengths[i] = temp[length - 1 - i];
            }

            for (int i = 0; i < mOrderWavelengths.length; i++) {
                Log.d(TAG, String.format("[%d] -> %f\n", i, mOrderWavelengths[i]));
            }
        }
        mSaveDialog = new SaveNameDialog();
        mFileExportDialog = new SaveNameDialog();
        mSaveDialog.init(-1, getString(R.string.action_save), getString(R.string.name), new SaveNameDialog.SettingInputListern() {
            @Override
            public void onSettingInputComplete(int id, String name) {
                if (name.length() < 1 || (!Utils.isValidName(name))) {
                    Toast.makeText(getActivity(), getString(R.string.notice_name_invalid), Toast.LENGTH_SHORT).show();
                    return;
                }
                List<String> saveFileList = DeviceApplication.getInstance().getMultipleWavelengthDb().getTables();
                Log.d(TAG, "Alread saved -> " + saveFileList.size() + " files.");

                for (int i = 0; i < saveFileList.size(); i++) {
                    Log.d(TAG, String.format("[%d] -> %s\n", i, saveFileList.get(i)));
                }
                String fileName = name;
                for (int i = 0; i < mData.size(); i++) {
                    String index = "";
                    int mId = 0;
                    int subid = 0;
                    float wavelength = 0.0f;
                    float abs = 0.0f;
                    float trans = 0.0f;
                    int energy = 0;
                    long date = 0;

                    HashMap<String, String> map = mData.get(i);
                    index = map.get("id");

                    String[] subString = index.split("-");
                    mId = Integer.parseInt(subString[0]);
                    subid = Integer.parseInt(subString[1]);
                    wavelength = Float.parseFloat(map.get("wavelength"));
                    abs = Float.parseFloat(map.get("abs"));
                    trans = Float.parseFloat(map.get("trans"));
                    energy = Integer.parseInt(map.get("energy"));
                    date = Long.parseLong(map.get("date"));

                    MultipleWavelengthRecord record = new MultipleWavelengthRecord(mId, subid, wavelength, abs, trans, energy, date);
                    DeviceApplication.getInstance().getMultipleWavelengthDb().saveRecord(fileName, record);
                }
                Log.d(TAG, "save to -> " + fileName);
                Utils.needToSave = false;
            }

            @Override
            public void abort() {
                getFragmentManager().popBackStack();
            }
        });
        mFileExportDialog.init(-1, getString(R.string.action_file_export), getString(R.string.name), new SaveNameDialog.SettingInputListern() {
            @Override
            public void onSettingInputComplete(int index, String name) {
                if(name.length() < 1 || (!Utils.isValidName(name))) {
                    Toast.makeText(getActivity(), getString(R.string.notice_name_invalid), Toast.LENGTH_SHORT).show();
                    return;
                }
                File file = Utils.getMultipleWavelengthFile(name + ".txt");
                try {
                    FileWriter out = new FileWriter(file, false);
                    BufferedWriter writer = new BufferedWriter(out);
                    String line = String.format("%s\t%s\t%s\t%s\t%s\n",
                            getString(R.string.index),
                            getString(R.string.wavelength),
                            getString(R.string.abs),
                            getString(R.string.trans),
                            getString(R.string.energy));
                    writer.write(line);
                    for (int i = 0; i < mData.size(); i++) {
                        String id = "";
                        float wavelength = 0.0f;
                        float abs = 0.0f;
                        float trans = 0.0f;
                        int energy = 0;

                        HashMap<String, String> map = mData.get(i);
                        id = map.get("id");
                        wavelength = Float.parseFloat(map.get("wavelength"));
                        abs = Float.parseFloat(map.get("abs"));
                        trans = Float.parseFloat(map.get("trans"));
                        energy = Integer.parseInt(map.get("energy"));
                        line = String.format("%s\t%f\t%f\t%f\t%d\n",
                                id, wavelength, abs, trans, energy);
                        writer.write(line);
                    }
                    writer.flush();
                    writer.close();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void abort() {

            }
        });
        mFileExportDialog.setAbort(false);

        if (loadFile) {
            loadFileById(loadFileIndex);
        }
    }

    public void prepareLoadFile(int id) {
        loadFile = true;
        loadFileIndex = id;
    }

    private void loadFileById(int id) {
        List<String> fileList = DeviceApplication.getInstance().getMultipleWavelengthDb().getTables();
        String fileName = fileList.get(id);
        List<MultipleWavelengthRecord> lists = DeviceApplication.getInstance().getMultipleWavelengthDb().getRecords(fileName);
        mData.clear();
        for (int i = 0; i < lists.size(); i++) {
            addItem(lists.get(i));
        }
        Utils.needToSave = false;
    }

    @Subscribe
    public void onFileOperateEvent(FileOperateEvent event) {
        if (event.op_type == FileOperateEvent.OP_EVENT_OPEN) {
            List<String> saveFileList = DeviceApplication.getInstance().getMultipleWavelengthDb().getTables();

            Utils.showItemSelectDialog(getActivity(), getString(R.string.action_open)
                    , saveFileList.toArray(new String[saveFileList.size()]), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            loadFileById(which);
                        }
                    });

        } else if (event.op_type == FileOperateEvent.OP_EVENT_SAVE) {
            if (mData.size() < 1) {
                Toast.makeText(getActivity(), getString(R.string.notice_save_null), Toast.LENGTH_SHORT).show();
                return;
            }
            mSaveDialog.show(getFragmentManager(), "save");
        } else if (event.op_type == FileOperateEvent.OP_EVENT_PRINT) {

        } else if (event.op_type == FileOperateEvent.OP_EVENT_FILE_EXPORT) {
            if(mData.size() < 1) {
                Toast.makeText(getActivity(), getString(R.string.notice_save_null), Toast.LENGTH_SHORT).show();
                return;
            }
            mFileExportDialog.show(getFragmentManager(), "file_export");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        Utils.needToSave = false;
        loadFile = false;
    }

    private void addItem(MultipleWavelengthRecord record) {
        HashMap<String, String> item = new HashMap<String, String>();
        int no = mData.size() + 1;
        record.setIndex(no);

        item.put("id", "" + "" + mMainIndex + "-" + mSubIndex);
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

    @Subscribe
    public void OnUpdateEvent(MultipleWavelengthCallbackEvent event) {
        if (event.event_type == MultipleWavelengthCallbackEvent.EVENT_TYPE_UPDATE) {
            addItem(new MultipleWavelengthRecord(-1, -1, event.wavelength, event.abs, event.trans, event.energy, System.currentTimeMillis()));
            mSubIndex++;
        } else if (event.event_type == MultipleWavelengthCallbackEvent.EVENT_TYPE_REZERO_DONE) {
            mStart.setEnabled(true);
        } else if (event.event_type == MultipleWavelengthCallbackEvent.EVENT_TYPE_TEST_DONE) {
            mMainIndex++;
            mSubIndex = 1;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_multiple_wavelength_start:
                Log.d(TAG, "start");
                if (isFake) {
                    int energy = (int) (Math.random() * 1000.0f);
                    float wavelength = (float) (Math.random() * 1000.0f);
                    float abs = (float) (Math.random() * 10);
                    float trans = (float) (Math.random() * 100);
                    addItem(new MultipleWavelengthRecord(-1, -1, wavelength, abs, trans, energy, System.currentTimeMillis()));
                    mSubIndex++;
                } else {
                    if (mOrderWavelengths == null || mOrderWavelengths.length < 1) {
                        Toast.makeText(getActivity(), getString(R.string.notice_setting_null), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    BusProvider.getInstance().post(new MultipleWavelengthCallbackEvent(MultipleWavelengthCallbackEvent.EVENT_TYPE_DO_TEST, mOrderWavelengths));
                }
                break;
            case R.id.bt_multiple_wavelength_rezero:
                Log.d(TAG, "rezero");
                if (mOrderWavelengths == null || mOrderWavelengths.length < 1) {
                    Toast.makeText(getActivity(), getString(R.string.notice_setting_null), Toast.LENGTH_SHORT).show();
                    return;
                }
                BusProvider.getInstance().post(new MultipleWavelengthCallbackEvent(MultipleWavelengthCallbackEvent.EVENT_TYPE_DO_REZERO, mOrderWavelengths));
                break;

            default:
                break;
        }
    }

    @Override
    public void onCallback(float[] wavelengths) {
        for (int i = 0; i < wavelengths.length; i++) {
            Log.d(TAG, String.format("[%d] -> %f\n", i, wavelengths[i]));
        }
        DeviceApplication.getInstance().getSpUtils().setKeyMultipleWavelengthLength(wavelengths.length);
        DeviceApplication.getInstance().getSpUtils().saveMultipleWavelength(wavelengths);
        mWavelengths = wavelengths;
        int length = wavelengths.length;
        if (length > 0) {
            mOrderWavelengths = new float[length];
            float[] temp = new float[length];
            for (int i = 0; i < length; i++) {
                temp[i] = mWavelengths[i];
            }
            Arrays.sort(temp);
            for (int i = 0; i < length; i++) {
                mOrderWavelengths[i] = temp[length - 1 - i];
            }

            for (int i = 0; i < mOrderWavelengths.length; i++) {
                Log.d(TAG, String.format("[%d] -> %f\n", i, mOrderWavelengths[i]));
            }
        }
    }

}
