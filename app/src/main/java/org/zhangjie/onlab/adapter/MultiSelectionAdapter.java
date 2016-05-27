package org.zhangjie.onlab.adapter;

import android.content.Context;
import android.view.LayoutInflater;

import java.util.HashMap;
import java.util.List;

/**
 * Created by H151136 on 5/26/2016.
 */
public class MultiSelectionAdapter {
    private List<HashMap<String, String>> mData;
    private HashMap<Integer, Boolean> mIsSelected;
    private Context mContext;
    private LayoutInflater mInflater;
    private String[] mKeys;
    private int mLayout;
    private int[] mViewIds;
    private boolean mSelectMode = false;


}
