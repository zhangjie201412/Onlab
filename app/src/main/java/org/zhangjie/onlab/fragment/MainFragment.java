package org.zhangjie.onlab.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import org.zhangjie.onlab.DeviceApplication;
import org.zhangjie.onlab.MainActivity;
import org.zhangjie.onlab.R;
import org.zhangjie.onlab.adapter.NineGridAdapter;
import org.zhangjie.onlab.dialog.WavelengthDialog;
import org.zhangjie.onlab.otto.BusProvider;
import org.zhangjie.onlab.otto.FileOperateEvent;
import org.zhangjie.onlab.otto.SettingEvent;
import org.zhangjie.onlab.utils.Utils;
import org.zhangjie.onlab.view.NineGridView;

import java.util.List;

/**
 * Created by H151136 on 5/24/2016.
 */
public class MainFragment extends Fragment {
    private static final String TAG = "Onlab.MainFragment";
    private FragmentCallbackListener mListener;
    private NineGridView mGridView;

    public static final int ITEM_PHOTOMETRIC_MEASURE = 100;
    public static final int ITEM_QUANTITATIVE_ANALYSIS = 101;
    public static final int ITEM_WAVELENGTH_SCAN = 102;
    public static final int ITEM_TIME_SCAN = 103;
    public static final int ITEM_MULTI_WAVELENGTH = 104;
    public static final int ITEM_DNA = 105;
    public static final int ITEM_SYSTEM_SETTING = 106;
    public static final int ITEM_ABOUT = 107;

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
                    case ITEM_SYSTEM_SETTING:
                        break;
                    case ITEM_ABOUT:
                        break;
                    case ITEM_DNA:
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
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
    }

    @Subscribe
    public void OnSettingEvent(SettingEvent event) {
        Context context = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context = getContext();
        } else {
            context = getActivity();
        }
        int backStackCount = getFragmentManager().getBackStackEntryCount();

        if (backStackCount <= 1) {
            ((MainActivity) getActivity()).showSystemSettingDialog();
        }
    }

    @Subscribe
    public void onFileOperateEvent(FileOperateEvent event) {

        int backStackCount = getFragmentManager().getBackStackEntryCount();

        if (backStackCount <= 1) {
            if (event.op_type == FileOperateEvent.OP_EVENT_OPEN) {

                String[] items = getResources().getStringArray(R.array.open_titles);
                Utils.showItemSelectDialog(getActivity(), getString(R.string.action_open)
                        , items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, final int which) {
//                                if (getFragmentManager().getBackStackEntryCount() <= 1) {
//                                    mListener.onMainClick(which);
//                                }
                                Log.d(TAG, "which = " + which);
                                switch (which + 100) {
                                    case ITEM_PHOTOMETRIC_MEASURE:
                                        List<String> saveFileList0 = DeviceApplication.getInstance().getPhotometricMeasureDb().getTables();

                                        Utils.showItemSelectDialog(getActivity(), getResources().getStringArray(R.array.open_titles)[0]
                                                , saveFileList0.toArray(new String[saveFileList0.size()]), new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        mListener.loadFile(which, id);
                                                    }
                                                });
                                        break;
                                    case ITEM_QUANTITATIVE_ANALYSIS:
                                        List<String> saveFileList1 = DeviceApplication.getInstance().getQuantitativeAnalysisDb().getTables();

                                        Utils.showItemSelectDialog(getActivity(), getResources().getStringArray(R.array.open_titles)[1]
                                                , saveFileList1.toArray(new String[saveFileList1.size()]), new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        mListener.loadFile(which, id);
                                                    }
                                                });
                                        break;
                                    case ITEM_WAVELENGTH_SCAN:
                                        List<String> saveFileList2 = DeviceApplication.getInstance().getWavelengthScanDb().getTables();

                                        Utils.showItemSelectDialog(getActivity(), getResources().getStringArray(R.array.open_titles)[2]
                                                , saveFileList2.toArray(new String[saveFileList2.size()]), new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        mListener.loadFile(which, id);
                                                    }
                                                });
                                        break;
                                    case ITEM_TIME_SCAN:
                                        List<String> saveFileList3 = DeviceApplication.getInstance().getTimeScanDb().getTables();

                                        Utils.showItemSelectDialog(getActivity(), getResources().getStringArray(R.array.open_titles)[3]
                                                , saveFileList3.toArray(new String[saveFileList3.size()]), new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        mListener.loadFile(which, id);
                                                    }
                                                });
                                        break;
                                    case ITEM_MULTI_WAVELENGTH:
                                        List<String> saveFileList4 = DeviceApplication.getInstance().getMultipleWavelengthDb().getTables();

                                        Utils.showItemSelectDialog(getActivity(), getResources().getStringArray(R.array.open_titles)[4]
                                                , saveFileList4.toArray(new String[saveFileList4.size()]), new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        mListener.loadFile(which, id);
                                                    }
                                                });
                                        break;
                                    case ITEM_DNA:
                                        List<String> saveFileList5 = DeviceApplication.getInstance().getDnaDb().getTables();

                                        Utils.showItemSelectDialog(getActivity(), getResources().getStringArray(R.array.open_titles)[5]
                                                , saveFileList5.toArray(new String[saveFileList5.size()]), new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        mListener.loadFile(which, id);
                                                    }
                                                });
                                        break;

                                }
                            }
                        });

            } else if (event.op_type == FileOperateEvent.OP_EVENT_SAVE) {
//                if(mData.size() < 1) {
//                    Toast.makeText(getActivity(), getString(R.string.notice_save_null), Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                mSaveDialog.show(getFragmentManager(), "save");
            } else if (event.op_type == FileOperateEvent.OP_EVENT_PRINT) {

            }
        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
//        getFragmentManager().beginTransaction().remove(this).commit();
    }
}
