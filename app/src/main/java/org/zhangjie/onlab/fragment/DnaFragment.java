package org.zhangjie.onlab.fragment;

import android.app.Fragment;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import org.zhangjie.onlab.DeviceApplication;
import org.zhangjie.onlab.MainActivity;
import org.zhangjie.onlab.R;
import org.zhangjie.onlab.adapter.MultiSelectionAdapter;
import org.zhangjie.onlab.device.DeviceManager;
import org.zhangjie.onlab.dialog.DnaSettingDialog;
import org.zhangjie.onlab.dialog.FileExportDialog;
import org.zhangjie.onlab.dialog.SaveNameDialog;
import org.zhangjie.onlab.otto.BusProvider;
import org.zhangjie.onlab.otto.DnaCallbackEvent;
import org.zhangjie.onlab.otto.FileOperateEvent;
import org.zhangjie.onlab.otto.SettingEvent;
import org.zhangjie.onlab.record.DnaRecord;
import org.zhangjie.onlab.utils.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by H151136 on 5/24/2016.
 */
public class DnaFragment extends Fragment implements DnaSettingDialog.OnDnaSettingListener, View.OnClickListener {

    private static final String TAG = "Onlab.Dna";
    private DnaSettingDialog mDnaSettingDialog;
    private Button mStartTestButton;
    private Button mRezeroButton;

    private ListView mListView;
    private MultiSelectionAdapter mAdapter;
    private List<HashMap<String, String>> mData;
    private SaveNameDialog mSaveDialog;
    private FileExportDialog mFileExportDialog;

    public static float refWavelength;
    public static float wavelength1;
    public static float wavelength2;

    private float mAbs1;
    private float mAbs2;
    private float mAbsRef;

    private float mF1;
    private float mF2;
    private float mF3;
    private float mF4;

    private SaveNameDialog mNameDialog;
    private boolean loadFile = false;
    private int loadFileIndex = -1;

    private int mFileType = FileExportDialog.FILE_TYPE_TXT;

    private TextView mAbs1TextView;
    private TextView mAbs2TextView;
    private TextView mRefTextView;
    private TextView mRatioTextView;
    private static final float METHOD1_WAVELENGTH1 = 260.0f;
    private static final float METHOD1_WAVELENGTH2 = 280.0f;
    private static final float METHOD1_WAVELENGTH_REF = 320.0f;

    private static final float METHOD2_WAVELENGTH1 = 260.0f;
    private static final float METHOD2_WAVELENGTH2 = 230.0f;
    private static final float METHOD2_WAVELENGTH_REF = 320.0f;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dna, container, false);

        mFileExportDialog = new FileExportDialog();
        initView(view);
        mDnaSettingDialog = new DnaSettingDialog();
        mDnaSettingDialog.setListener(this);
        mNameDialog = new SaveNameDialog();
        mSaveDialog = new SaveNameDialog();
        mSaveDialog.init(-1, getString(R.string.action_save), getString(R.string.name), new SaveNameDialog.SettingInputListern() {
            @Override
            public void onSettingInputComplete(int id, String name) {
                if(name.length() < 1 || (!Utils.isValidName(name))) {
                    Toast.makeText(getActivity(), getString(R.string.notice_name_invalid), Toast.LENGTH_SHORT).show();
                    return;
                }
                List<String> saveFileList = DeviceApplication.getInstance().getDnaDb().getTables();
                Log.d(TAG, "Alread saved -> " + saveFileList.size() + " files.");

                for (int i = 0; i < saveFileList.size(); i++) {
                    Log.d(TAG, String.format("[%d] -> %s\n", i, saveFileList.get(i)));
                }
                String fileName = name;
                for (int i = 0; i < mData.size(); i++) {
                    int index = 0;
                    String sample_name = "";
                    float abs1 = 0.0f;
                    float abs2 = 0.0f;
                    float absRef = 0.0f;
                    float dna = 0.0f;;
                    float protein = 0.0f;
                    float ratio = 0;
                    long date = 0;

                    HashMap<String, String> map = mData.get(i);
                    index = Integer.parseInt(map.get("id"));
                    sample_name = (String)map.get("name");
                    abs1 = Float.parseFloat(map.get("abs1"));
                    abs2 = Float.parseFloat(map.get("abs2"));
                    absRef = Float.parseFloat(map.get("absRef"));
                    dna = Float.parseFloat(map.get("dna"));
                    protein = Float.parseFloat(map.get("protein"));
                    ratio = Float.parseFloat(map.get("ratio"));
                    date = Long.parseLong(map.get("date"));

                    DnaRecord record = new DnaRecord(index, sample_name, abs1, abs2, absRef, dna, protein, ratio, date);
                    DeviceApplication.getInstance().getDnaDb().saveRecord(fileName, record);
                }
                Log.d(TAG, "save to -> " + fileName);
                Utils.needToSave = false;
            }

            @Override
            public void abort() {
                getFragmentManager().popBackStack();
            }
        });

        if(loadFile) {
            loadFileById(loadFileIndex);
        } else {
            mDnaSettingDialog.show(getFragmentManager(), "dna");
        }

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        Utils.needToSave = false;
        loadFile = false;
    }

    void initView(View view) {
        mAbs1TextView = (TextView)view.findViewById(R.id.tv_title_abs1);
        mAbs2TextView = (TextView)view.findViewById(R.id.tv_title_abs2);
        mRefTextView = (TextView)view.findViewById(R.id.tv_title_abs_ref);
        mRatioTextView = (TextView)view.findViewById(R.id.tv_title_ratio);

        mAbs1TextView.setText(String.format("Abs[%d]", (int)METHOD1_WAVELENGTH1));
        mAbs2TextView.setText(String.format("Abs[%d]", (int)METHOD1_WAVELENGTH2));
        mRefTextView.setText(String.format("Abs[%d]", (int)METHOD1_WAVELENGTH_REF));
        mRatioTextView.setText(String.format("Abs[%d]/Abs[%d]", (int)METHOD1_WAVELENGTH1, (int)METHOD1_WAVELENGTH2));

        mStartTestButton = (Button) view.findViewById(R.id.bt_dna_start);
        mRezeroButton = (Button) view.findViewById(R.id.bt_dna_rezero);
        mListView = (ListView) view.findViewById(R.id.lv_dna);
        mStartTestButton.setOnClickListener(this);
        mRezeroButton.setOnClickListener(this);
        mStartTestButton.setEnabled(false);
        mRezeroButton.setEnabled(false);
        mData = new ArrayList<HashMap<String, String>>();
        mAdapter = new MultiSelectionAdapter(getActivity(), mData,
                R.layout.item_dna,
                new String[]{"id", "name", "abs1", "abs2", "absRef", "dna", "protein", "ratio"},
                new int[]{R.id.item_index, R.id.item_sample_name,
                        R.id.item_abs1, R.id.item_abs2, R.id.item_abs_ref,
                        R.id.item_dna, R.id.item_protein, R.id.item_ratio});
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

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                mNameDialog.init(position, getString(R.string.sample_name_setting), getString(R.string.name)
                        , new SaveNameDialog.SettingInputListern() {
                            @Override
                            public void onSettingInputComplete(int index, String name) {
                                if (name.length() < 1) {
                                    Toast.makeText(getActivity(), getString(R.string.notice_edit_null), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                mData.get(position).put("name", name);
                                mAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void abort() {

                            }
                        });
                mNameDialog.setAbort(false);
                mNameDialog.show(getFragmentManager(), "name");
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
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
                                        removeItem(position);
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
                return true;
            }
        });
        mFileExportDialog.init(getString(R.string.action_file_export), getString(R.string.name), new FileExportDialog.SettingInputListern() {
            @Override
            public void onSettingInputComplete(String name) {
                if(name.length() < 1 || (!Utils.isValidName(name))) {
                    Toast.makeText(getActivity(), getString(R.string.notice_name_invalid), Toast.LENGTH_SHORT).show();
                    return;
                }
                String typeString = "unknow";
                String titleFormatString = "";
                String contentFormatString = "";
                if(mFileType == FileExportDialog.FILE_TYPE_TXT) {
                    typeString = "txt";
                    titleFormatString = "%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n";
                    contentFormatString = "%d\t%s\t%f\t%f\t%f\t%f\t%f\t%f\n";
                } else if(mFileType ==FileExportDialog.FILE_TYPE_CVS) {
                    typeString = "cvs";
                    titleFormatString = "%s,%s,%s,%s,%s,%s,%s,%s\n";
                    contentFormatString = "%d,%s,%f,%f,%f,%f,%f,%f\n";
                }
                File file = Utils.getDnaFile(name + "." + typeString);
                try {
                    FileWriter out = new FileWriter(file, false);
                    BufferedWriter writer = new BufferedWriter(out);
                    String line = String.format(titleFormatString,
                            getString(R.string.index),
                            getString(R.string.sample_name),
                            getString(R.string.abs_with_unit),
                            getString(R.string.abs_with_unit),
                            getString(R.string.abs_with_unit),
                            getString(R.string.dna_with_unit),
                            getString(R.string.protein_with_unit),
                            getString(R.string.radio));
                    writer.write(line);
                    for (int i = 0; i < mData.size(); i++) {
                        int id = 0;
                        float abs1 = 0.0f;
                        float abs2 = 0.0f;
                        float absRef = 0.0f;
                        float dna = 0.0f;
                        float protein = 0.0f;
                        float ratio = 0.0f;

                        HashMap<String, String> map = mData.get(i);
                        id = Integer.parseInt(map.get("id"));
                        abs1 = Float.parseFloat(map.get("abs1"));
                        abs2 = Float.parseFloat(map.get("abs2"));
                        absRef = Float.parseFloat(map.get("absRef"));
                        dna = Float.parseFloat(map.get("dna"));
                        protein = Float.parseFloat(map.get("protein"));
                        ratio = Float.parseFloat(map.get("ratio"));
                        line = String.format(contentFormatString,
                                id, abs1, abs2, absRef, dna, protein, ratio);
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
            public void onFileTypeSelect(int type) {
                mFileType = type;
            }
        });
    }

    public void prepareLoadFile(int id) {
        loadFile = true;
        loadFileIndex = id;
    }

    private void loadFileById(int id) {
        List<String> fileList = DeviceApplication.getInstance().getDnaDb().getTables();
        String fileName = fileList.get(id);
        List<DnaRecord> lists = DeviceApplication.getInstance().getDnaDb().getRecords(fileName);
        mData.clear();
        for(int i = 0; i < lists.size(); i++) {
            addItem(lists.get(i));
        }
        Utils.needToSave = false;
    }

    private void addItem(DnaRecord record) {
        HashMap<String, String> item = new HashMap<String, String>();
        int no = mData.size() + 1;
        record.setIndex(no);

        item.put("id", "" + no);
        item.put("name", "" + record.getName());
        item.put("abs1", "" + record.getAbs1());
        item.put("abs2", "" + record.getAbs2());
        item.put("absRef", "" + record.getAbsRef());
        item.put("dna", "" + record.getDna());
        item.put("protein", "" + record.getProtein());
        item.put("ratio", "" + record.getRatio());
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
        mDnaSettingDialog.show(getFragmentManager(), "dna");
    }

    @Subscribe
    public void OnUpdateEvent(DnaCallbackEvent event) {
        if(event.event_type == DnaCallbackEvent.EVENT_TYPE_UPDATE) {
            float wl = event.wavelength;
            float abs = event.abs;
            if(wl == wavelength1) {
                mAbs1 = abs;
            } else if(wl == wavelength2) {
                mAbs2 = abs;
            } else if(wl == refWavelength) {
                mAbsRef = abs;
            }
        } else if(event.event_type == DnaCallbackEvent.EVENT_TYPE_REZERO_DONE) {
            mStartTestButton.setEnabled(true);
        } else if(event.event_type == DnaCallbackEvent.EVENT_TYPE_TEST_DONE) {
            float dna = (mAbs1 - mAbsRef) * mF1 - (mAbs2 - mAbsRef) * mF2;
            float protein = (mAbs2 - mAbsRef) * mF3 - (mAbs1 - mAbsRef) * mF4;
            addItem(new DnaRecord(-1, "",mAbs1, mAbs2, mAbsRef, dna, protein, (mAbs1 / mAbs2), System.currentTimeMillis()));
        }
    }

    @Subscribe
    public void onFileOperateEvent(FileOperateEvent event) {
        if (event.op_type == FileOperateEvent.OP_EVENT_OPEN) {
            List<String> saveFileList = DeviceApplication.getInstance().getDnaDb().getTables();

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

        } else if(event.op_type == FileOperateEvent.OP_EVENT_FILE_EXPORT) {
//            if(mData.size() < 1) {
//                Toast.makeText(getActivity(), getString(R.string.notice_save_null), Toast.LENGTH_SHORT).show();
//                return;
//            }
//            mFileExportDialog.show(getFragmentManager(), "file_export");
        } else if(event.op_type == FileOperateEvent.OP_EVENT_REZERO) {
            BusProvider.getInstance().post(new DnaCallbackEvent(
                    DnaCallbackEvent.EVENT_TYPE_DO_REZERO, wavelength1,
                    wavelength2, refWavelength));
        } else if(event.op_type == FileOperateEvent.OP_EVENT_START_TEST) {
            BusProvider.getInstance().post(new DnaCallbackEvent(
                    DnaCallbackEvent.EVENT_TYPE_DO_TEST, wavelength1,
                    wavelength2, refWavelength));
        }
    }

    @Override
    public void onDnaSettingCallback(DnaSettingDialog.DnaSettingParam param, int error) {
        if (error != 0) {
            Toast.makeText(getActivity(), R.string.notice_edit_null, Toast.LENGTH_SHORT).show();
            mRezeroButton.setEnabled(false);
        } else {
            //get parameter
            Log.d(TAG, "wavelength1 = " + param.wavelength1);
            Log.d(TAG, "wavelength2 = " + param.wavelength2);
            Log.d(TAG, "wavelengthRef = " + param.wavelengthRef);
            Log.d(TAG, "F1 = " + param.f1);
            Log.d(TAG, "F2 = " + param.f2);
            Log.d(TAG, "F3 = " + param.f3);
            Log.d(TAG, "F4 = " + param.f4);
            mRezeroButton.setEnabled(true);
            mStartTestButton.setEnabled(false);
            refWavelength = param.wavelengthRef;
            wavelength1 = param.wavelength1;
            wavelength2 = param.wavelength2;
            mF1 = param.f1;
            mF2 = param.f2;
            mF3 = param.f3;
            mF4 = param.f4;
            mAbs1TextView.setText(String.format("Abs[%d]", (int)param.wavelength1));
            mAbs2TextView.setText(String.format("Abs[%d]", (int)param.wavelength2));
            mRefTextView.setText(String.format("Abs[%d]", (int)param.wavelengthRef));
            mRatioTextView.setText(String.format("Abs[%d]/Abs[%d]", (int)param.wavelength1, (int)param.wavelength2));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_dna_rezero:
                BusProvider.getInstance().post(new DnaCallbackEvent(
                        DnaCallbackEvent.EVENT_TYPE_DO_REZERO, wavelength1,
                        wavelength2, refWavelength));
                break;
            case R.id.bt_dna_start:
                BusProvider.getInstance().post(new DnaCallbackEvent(
                        DnaCallbackEvent.EVENT_TYPE_DO_TEST, wavelength1,
                        wavelength2, refWavelength));
                break;
            default:
                break;
        }

    }
}
