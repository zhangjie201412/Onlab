package org.zhangjie.onlab.otto;

/**
 * Created by Administrator on 2016/6/22.
 */
public class FileOperateEvent {
    public static final int OP_EVENT_OPEN = 0;
    public static final int OP_EVENT_SAVE = 1;
    public static final int OP_EVENT_PRINT = 2;
    public static final int OP_EVENT_FILE_EXPORT = 3;

    public int op_type;

    public FileOperateEvent(int type) {
        op_type = type;
    }
}
