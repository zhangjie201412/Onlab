package org.zhangjie.onlab.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.zhangjie.onlab.R;
import org.zhangjie.onlab.otto.AboutExitEvent;
import org.zhangjie.onlab.otto.BusProvider;

/**
 * Created by H151136 on 5/24/2016.
 */
public class AboutFragment extends Fragment {


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_about, container, false);

        BusProvider.getInstance().register(this);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        BusProvider.getInstance().post(new AboutExitEvent());
        BusProvider.getInstance().unregister(this);
    }
}
