package org.zhangjie.onlab.otto;

import com.squareup.otto.Bus;

/**
 * Created by Administrator on 2016/6/1.
 */
public final class BusProvider {

    private static final Bus BUS = new Bus();

    public static Bus getInstance() {
        return BUS;
    }

    private BusProvider() {

    }
}
