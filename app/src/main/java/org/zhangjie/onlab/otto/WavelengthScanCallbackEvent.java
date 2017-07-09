package org.zhangjie.onlab.otto;

/**
 * Created by Administrator on 2016/6/15.
 */
public class WavelengthScanCallbackEvent {
    public static final int EVENT_TYPE_REZERO_DONE = 10;
    public static final int EVENT_TYPE_WORK_DONE = 11;
    public static final int EVENT_TYPE_WORKING = 12;

    public int event_type;
    public float wavelength;
    public float abs;
    public float trans;
    public int energy;
    public int energyRef;

    public WavelengthScanCallbackEvent(int event) {
        event_type = event;
    }

    public WavelengthScanCallbackEvent(int event, float wavelength, float abs,
                                       float trans, int energy, int energyRef) {
        this.event_type = event;
        this.wavelength = wavelength;
        this.abs = abs;
        this.trans = trans;
        this.energy = energy;
        this.energyRef = energyRef;
    }

}
