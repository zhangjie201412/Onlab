package org.zhangjie.onlab.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import org.zhangjie.onlab.R;

import java.util.HashMap;
import java.util.List;

/**
 * Created by H151136 on 6/20/2016.
 */
public class MultipleWavelengthSettingAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private List<HashMap<String, String>> mData;
    private Context mContext;

    public MultipleWavelengthSettingAdapter(Context context, List<HashMap<String, String>> data) {
        mInflater = LayoutInflater.from(context);
        mData = data;
        mContext = context;
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
        if(convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_multiple_wavelength_setting, null);
            holder.id = (TextView)convertView.findViewById(R.id.tv_wavelength_index);
            holder.value = (TextView)convertView.findViewById(R.id.tv_wavelength_value);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }
        holder.id.setText(mContext.getString(R.string.wavelength)
                + "" + (position + 1));
        if(mData.get(position).get("wavelength").length() < 1) {
            holder.value.setText(mContext.getString(R.string.summary_timescan_work_wavelength));
        } else {
            holder.value.setText(mData.get(position).get("wavelength") + "\t" + mContext.getString(R.string.nm));
        }
        return convertView;
    }

    public class ViewHolder {
        public TextView id;
        public TextView value;
    }
}
