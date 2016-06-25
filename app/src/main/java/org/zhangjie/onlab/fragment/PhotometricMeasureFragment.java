package org.zhangjie.onlab.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
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
import org.zhangjie.onlab.device.DeviceManager;
import org.zhangjie.onlab.dialog.SaveNameDialog;
import org.zhangjie.onlab.otto.BusProvider;
import org.zhangjie.onlab.R;
import org.zhangjie.onlab.adapter.MultiSelectionAdapter;
import org.zhangjie.onlab.otto.FileOperateEvent;
import org.zhangjie.onlab.otto.SetOperateEvent;
import org.zhangjie.onlab.otto.SetOperateModeEvent;
import org.zhangjie.onlab.otto.SetWavelengthEvent;
import org.zhangjie.onlab.otto.UpdateFragmentEvent;
import org.zhangjie.onlab.otto.WaitProgressEvent;
import org.zhangjie.onlab.record.PhotoMeasureRecord;
import org.zhangjie.onlab.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by H151136 on 5/24/2016.
 */
public class PhotometricMeasureFragment extends Fragment implements View.OnClickListener {

    private boolean isFake = false;
    private static final String TAG = "Onlab.PhotometricMea";
    private ListView mListView;
    private MultiSelectionAdapter mAdapter;
    private List<HashMap<String, String>> mData;
    private boolean mIsRezeroed = false;
    private SaveNameDialog mSaveDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photometric_measure, container, false);
        initUi(view);
        mIsRezeroed = false;
        Utils.needToSave = false;
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
        mListView = (ListView) view.findViewById(R.id.lv_photometric_measure);
        mListView.setAdapter(mAdapter);
        mAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                Log.d(TAG, "onChanged!!");
                if(mData.size() > 0) {
                    Utils.needToSave = true;
                } else {
                    Utils.needToSave = false;
                }
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<Integer, Boolean> sel = mAdapter.getIsSelected();
                for (int i = 0; i < sel.size(); i++) {
                    sel.put(i, false);
                }
                mAdapter.setSelectMode(true);
                mAdapter.notifyDataSetChanged();
                BusProvider.getInstance().post(new SetOperateModeEvent(true));
                return true;
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

        Button start = (Button) view.findViewById(R.id.bt_photometric_measure_start);
        Button rezero = (Button) view.findViewById(R.id.bt_photometric_measure_rezero);
        Button setting = (Button) view.findViewById(R.id.bt_photometric_measure_setting);
        start.setOnClickListener(this);
        rezero.setOnClickListener(this);
        setting.setOnClickListener(this);
        mSaveDialog = new SaveNameDialog();
        mSaveDialog.init(-1, getString(R.string.action_save), getString(R.string.name), new SaveNameDialog.SettingInputListern() {
            @Override
            public void onSettingInputComplete(int id, String name) {

                if(name.length() < 1 || (!Utils.isValidName(name))) {
                    Toast.makeText(getActivity(), getString(R.string.notice_name_invalid), Toast.LENGTH_SHORT).show();
                    return;
                }
                List<String> saveFileList = DeviceApplication.getInstance().getPhotometricMeasureDb().getTables();
                Log.d(TAG, "Alread saved -> " + saveFileList.size() + " files.");

                for (int i = 0; i < saveFileList.size(); i++) {
                    Log.d(TAG, String.format("[%d] -> %s\n", i, saveFileList.get(i)));
                }
                String fileName = name;
                for (int i = 0; i < mData.size(); i++) {
                    int index = 0;
                    float wavelength = 0.0f;
                    float abs = 0.0f;
                    float trans = 0.0f;
                    int energy = 0;
                    long date = 0;

                    HashMap<String, String> map = mData.get(i);
                    index = Integer.parseInt(map.get("id"));
                    wavelength = Float.parseFloat(map.get("wavelength"));
                    abs = Float.parseFloat(map.get("abs"));
                    trans = Float.parseFloat(map.get("trans"));
                    energy = Integer.parseInt(map.get("energy"));
                    date = Long.parseLong(map.get("date"));

                    PhotoMeasureRecord record = new PhotoMeasureRecord(index, wavelength, abs, trans, energy, date);
                    DeviceApplication.getInstance().getPhotometricMeasureDb().saveRecord(fileName, record);
                }
                Log.d(TAG, "save to -> " + fileName);
                Utils.needToSave = false;
            }

            @Override
            public void abort() {
                getFragmentManager().popBackStack();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        Utils.needToSave = false;
    }

    private void addItem(PhotoMeasureRecord record) {
        HashMap<String, String> item = new HashMap<String, String>();
        int no = mData.size() + 1;
        record.setIndex(no);

        item.put("id", "" + no);
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
    public void onSetOperateModeEvent(SetOperateModeEvent event) {
        if (!event.isOperateMode) {
            //back to normal mode
            mAdapter.setSelectMode(false);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Subscribe
    public void onSetOperateEvent(SetOperateEvent event) {

        if (event.mode == SetOperateEvent.OP_MODE_SELECTALL) {
            HashMap<Integer, Boolean> sel = mAdapter.getIsSelected();
            for (int i = 0; i < sel.size(); i++) {
                sel.put(i, true);
            }
            mAdapter.notifyDataSetInvalidated();
        } else if (event.mode == SetOperateEvent.OP_MODE_DELETE) {
            int selectCount = 0;
            HashMap<Integer, Boolean> sel = mAdapter.getIsSelected();
            for (int i = 0; i < sel.size(); i++) {
                if (sel.get(i)) {
                    selectCount++;
                }
            }

            if (selectCount > 0) {
                showDeleteAlertDialog();
            }
        }
    }

    private void loadFileById(int id) {
        List<String> fileList = DeviceApplication.getInstance().getPhotometricMeasureDb().getTables();
        String fileName = fileList.get(id);
        List<PhotoMeasureRecord> lists = DeviceApplication.getInstance().getPhotometricMeasureDb().getRecords(fileName);
        mData.clear();
        for(int i = 0; i < lists.size(); i++) {
            addItem(lists.get(i));
        }
        Utils.needToSave = false;
    }

    @Subscribe
    public void onFileOperateEvent(FileOperateEvent event) {
        if (event.op_type == FileOperateEvent.OP_EVENT_OPEN) {
            List<String> saveFileList = DeviceApplication.getInstance().getPhotometricMeasureDb().getTables();

            Utils.showItemSelectDialog(getActivity(), getString(R.string.action_open)
                    , saveFileList.toArray(new String[saveFileList.size()]), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            loadFileById(which);
                        }
                    });

        } else if (event.op_type == FileOperateEvent.OP_EVENT_SAVE) {
            if(mData.size() < 1) {
                Toast.makeText(getActivity(), getString(R.string.notice_save_null), Toast.LENGTH_SHORT).show();
                return;
            }
            mSaveDialog.show(getFragmentManager(), "save");
        } else if (event.op_type == FileOperateEvent.OP_EVENT_PRINT) {

        }
    }

    @Subscribe
    public void onUpdateFragmentEvent(UpdateFragmentEvent event) {
        Log.d(TAG, "pm onUpdate type = " + event.getType());
        if (event.getType() == UpdateFragmentEvent.UPDATE_FRAGMENT_EVENT_TYPE_PHOTOMETRIC_MEASURE) {
            int energy = event.getEnergy();
            float wavelength = event.getWavelength();
            float abs = event.getAbs();
            float trans = event.getTrans();
            PhotoMeasureRecord record = new PhotoMeasureRecord(-1,
                    wavelength, abs, trans, energy,
                    System.currentTimeMillis());
            addItem(record);
            BusProvider.getInstance().post(new WaitProgressEvent(false));
        }
    }

    void showDeleteAlertDialog() {
        new AlertDialog.Builder(getActivity())
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(getString(R.string.notice_string))
                .setMessage(getString(R.string.sure_to_delete))
                .setPositiveButton(R.string.ok_string,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // TODO Auto-generated method stub
                                HashMap<Integer, Boolean> sel = mAdapter
                                        .getIsSelected();
                                int delCount = 0;
                                HashMap<Integer, Integer> delHashMap = new HashMap<Integer, Integer>();
                                for (int i = 0; i < sel.size(); i++) {
                                    if (sel.get(i)) {
                                        delHashMap.put(delCount, i);
                                        Log.d(TAG, "count = "
                                                + delCount + ", id = " + i);
                                        delCount = delCount + 1;
                                    }
                                }
                                Log.d(TAG, "count = " + delCount);
                                for (int i = 0; i < delCount; i++) {
                                    removeItem(delHashMap.get(i));
                                    sel = mAdapter.getIsSelected();
                                    for (int j = delHashMap.get(i); j < sel
                                            .size() - 1; j++) {
                                        sel.put(j, sel.get(j + 1));
                                    }
                                    sel.remove(sel.size() - 1);
                                    for (int j = 0; j < delHashMap.size(); j++) {
                                        delHashMap.put(j, delHashMap.get(j) - 1);
                                    }
                                    mAdapter.setIsSelected(sel);
                                }

                            }
                        })
                .setNegativeButton(getString(R.string.cancel_string),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {// 响应事件
                                // TODO Auto-generated method stub
                            }
                        }).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_photometric_measure_start:
                if (isFake) {
                    int energy = (int) (Math.random() * 1000.0f);
                    float wavelength = (float) (Math.random() * 1000.0f);
                    float abs = (float) (Math.random() * 10);
                    float trans = (float) (Math.random() * 100);

                    PhotoMeasureRecord record = new PhotoMeasureRecord(-1,
                            wavelength, abs, trans, energy,
                            System.currentTimeMillis());
                    addItem(record);
                } else {
                    if (!mIsRezeroed) {
                        Toast.makeText(getActivity(), getString(R.string.notice_rezero),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    DeviceManager.getInstance().photometricMeasureWork();
                    BusProvider.getInstance().post(new WaitProgressEvent(true));
                }
                break;
            case R.id.bt_photometric_measure_rezero:
                DeviceManager.getInstance().rezeroWork();
                mIsRezeroed = true;
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
