package org.zhangjie.onlab.otto;

/**
 * Created by H151136 on 6/2/2016.
 */
public class SetOperateEvent {

    public static final int OP_MODE_SELECTALL = 1;
    public static final int OP_MODE_DELETE = 2;

    public int mode;
    public SetOperateEvent(int mode) {
        this.mode = mode;
    }
}
