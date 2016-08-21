package org.zhangjie.onlab.otto;

/**
 * Created by Administrator on 2016/6/15.
 */
public class DnaCallbackEvent {

    public static final int EVENT_TYPE_DO_REZERO = 30;
    public static final int EVENT_TYPE_DO_TEST = 31;
    public static final int EVENT_TYPE_UPDATE = 32;
    public static final int EVENT_TYPE_REZERO_DONE = 33;
    public static final int EVENT_TYPE_TEST_DONE = 34;

    public int event_type;
    public float wl1;
    public float wl2;
    public float wlRef;

    public float wavelength;
    public float abs;

    public DnaCallbackEvent(int event) {
        event_type = event;
    }

    public DnaCallbackEvent(int event, float wl1, float wl2, float wlRef) {
        this.wl1 = wl1;
        this.wl2 = wl2;
        this.wlRef = wlRef;
        this.event_type = event;
    }

    public DnaCallbackEvent(int event, float wavelength, float abs) {
        this.event_type = event;
        this.wavelength = wavelength;
        this.abs = abs;
    }

}
