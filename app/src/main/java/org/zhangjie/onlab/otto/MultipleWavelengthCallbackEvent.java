package org.zhangjie.onlab.otto;

/**
 * Created by Administrator on 2016/6/15.
 */
public class MultipleWavelengthCallbackEvent {

    public static final int EVENT_TYPE_DO_REZERO = 20;
    public static final int EVENT_TYPE_DO_TEST = 21;
    public static final int EVENT_TYPE_UPDATE = 22;
    public static final int EVENT_TYPE_REZERO_DONE = 23;
    public static final int EVENT_TYPE_TEST_DONE = 24;

    public float[] wavelengths;

    public int event_type;
    public float wavelength;
    public float abs;
    public float trans;
    public int energy;

    public MultipleWavelengthCallbackEvent(int event) {
        event_type = event;
    }

    public MultipleWavelengthCallbackEvent(int event, float[] wavelengths) {
        event_type = event;
        this.wavelengths = wavelengths;
    }

    public MultipleWavelengthCallbackEvent(int event, float wavelength, float abs, float trans, int energy) {
        this.event_type = event;
        this.wavelength = wavelength;
        this.abs = abs;
        this.trans = trans;
        this.energy = energy;
    }

}
