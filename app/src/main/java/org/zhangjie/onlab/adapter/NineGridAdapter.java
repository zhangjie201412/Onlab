package org.zhangjie.onlab.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.zhangjie.onlab.view.NineViewHolder;
import org.zhangjie.onlab.R;

public class NineGridAdapter extends BaseAdapter {
    private Context mContext;

    public int[] img_text = {R.string.photometric_measurement, R.string.quantitative_analysis,
            R.string.wavelength_scan, R.string.time_scan, R.string.multi_wavelength,
    };
    public int[] imgs = {R.mipmap.ic_wavelength_scan, R.mipmap.ic_wavelength_scan,
            R.mipmap.ic_wavelength_scan, R.mipmap.ic_wavelength_scan,
            R.mipmap.ic_wavelength_scan};

    public NineGridAdapter(Context mContext) {
        super();
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return img_text.length;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.item_nine_grid, parent, false);
        }
        TextView tv = NineViewHolder.get(convertView, R.id.tv_item);
        ImageView iv = NineViewHolder.get(convertView, R.id.iv_item);
        iv.setBackgroundResource(imgs[position]);

        tv.setText(mContext.getString(img_text[position]));
        return convertView;
    }

}
