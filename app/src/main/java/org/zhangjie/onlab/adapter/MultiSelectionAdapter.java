package org.zhangjie.onlab.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import org.zhangjie.onlab.R;

import java.util.HashMap;
import java.util.List;

/**
 * Created by H151136 on 5/26/2016.
 */
public class MultiSelectionAdapter extends BaseAdapter {

    private static final String TAG = "Onlab.MultiSelect";
    private List<HashMap<String, String>> mData;
    private HashMap<Integer, Boolean> mIsSelected;
    private Context mContext;
    private LayoutInflater mInflater;
    private String[] mKeys;
    private int mLayout;
    private int[] mViewIds;
    private boolean mSelectMode = false;

    public MultiSelectionAdapter(Context context, List<HashMap<String, String>> data,
                                 int layout, String[] keys, int[] viewIds) {
        mContext = context;
        mData = data;
        mInflater = LayoutInflater.from(context);
        mIsSelected = new HashMap<Integer, Boolean>();
        mKeys = keys;
        mLayout = layout;
        mViewIds = viewIds;
        mSelectMode = false;
        for (int i = 0; i < mData.size(); i++) {
            mIsSelected.put(i, false);
        }
    }


    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(mLayout, null);
            for (int i = 0; i < mKeys.length; i++) {
                TextView tv = (TextView) convertView.findViewById(mViewIds[i]);
                holder.setTextViewByKey(mKeys[i], tv);
            }
            holder.cb = (CheckBox) convertView.findViewById(R.id.item_checkbox);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (mSelectMode) {
            holder.cb.setVisibility(View.VISIBLE);
        } else {
            holder.cb.setVisibility(View.INVISIBLE);
        }

        //fill the textView
        for (int i = 0; i < mKeys.length; i++) {
            if (mKeys[i].equals("id")) {
                holder.id.setText(mData.get(position).get("id"));
            } else if (mKeys[i].equals("name")) {
                holder.name.setText(mData.get(position).get("name"));
            } else if (mKeys[i].equals("wavelength")) {
                holder.wavelength.setText(mData.get(position).get("wavelength"));
            } else if (mKeys[i].equals("gain")) {
                holder.gain.setText(mData.get(position).get("gain"));
            } else if (mKeys[i].equals("abs")) {
                holder.abs.setText(mData.get(position).get("abs"));
            } else if (mKeys[i].equals("trans")) {
                holder.trans.setText(mData.get(position).get("trans"));
            } else if (mKeys[i].equals("energy")) {
                holder.energy.setText(mData.get(position).get("energy"));
            } else if (mKeys[i].equals("conc")) {
                holder.conc.setText(mData.get(position).get("conc"));
            } else if (mKeys[i].equals("abs1")) {
                holder.abs1.setText(mData.get(position).get("abs1"));
            } else if (mKeys[i].equals("abs2")) {
                holder.abs2.setText(mData.get(position).get("abs2"));
            } else if (mKeys[i].equals("absRef")) {
                holder.absRef.setText(mData.get(position).get("absRef"));
            } else if (mKeys[i].equals("dna")) {
                holder.dna.setText(mData.get(position).get("dna"));
            } else if (mKeys[i].equals("protein")) {
                holder.protein.setText(mData.get(position).get("protein"));
            } else if (mKeys[i].equals("ratio")) {
                holder.ratio.setText(mData.get(position).get("ratio"));
            }
        }
        holder.cb.setTag("" + position);
        holder.cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                // TODO Auto-generated method stub
                CheckBox box = (CheckBox) arg0;
                int index = Integer.parseInt((String) box.getTag());
                Log.d(TAG, "" + "index = " + index + ": " + arg1);
                mIsSelected.put(index, arg1);
            }
        });
        holder.cb.setChecked(getIsSelected().get(position));
        return convertView;
    }

    public void setData(List<HashMap<String, String>> data) {
        mData = data;
    }

    public void setSelectMode(boolean enable) {
        mSelectMode = enable;
        notifyDataSetChanged();
    }

    public void add() {
        mIsSelected.put(getCount() - 1, false);
    }

    public HashMap<Integer, Boolean> getIsSelected() {
        return mIsSelected;
    }

    public void setIsSelected(HashMap<Integer, Boolean> isSelected) {
        mIsSelected = isSelected;
    }

    public class ViewHolder {
        public TextView id;
        public TextView name;
        public TextView wavelength;
        public TextView gain;
        public TextView abs;
        public TextView trans;
        public TextView energy;
        public TextView conc;
        public TextView abs1;
        public TextView abs2;
        public TextView absRef;
        public TextView dna;
        public TextView protein;
        public TextView ratio;
        public CheckBox cb;

        public void setTextViewByKey(String key, TextView tv) {
            if (key.equals("id")) {
                id = tv;
            } else if (key.equals("name")) {
                name = tv;
            } else if (key.equals("wavelength")) {
                wavelength = tv;
            } else if (key.equals("gain")) {
                gain = tv;
            } else if (key.equals("abs")) {
                abs = tv;
            } else if (key.equals("trans")) {
                trans = tv;
            } else if (key.equals("energy")) {
                energy = tv;
            } else if (key.equals("conc")) {
                conc = tv;
            } else if (key.equals("abs1")) {
                abs1 = tv;
            } else if (key.equals("abs2")) {
                abs2 = tv;
            } else if (key.equals("absRef")) {
                absRef = tv;
            } else if (key.equals("dna")) {
                dna = tv;
            } else if (key.equals("protein")) {
                protein = tv;
            } else if(key.equals("ratio")) {
                ratio = tv;
            }
        }
    }
}
