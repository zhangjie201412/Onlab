package org.zhangjie.onlab.otto;

/**
 * Created by Administrator on 2016/6/4.
 */
public class UpdateFragmentEvent {
    public static final int UPDATE_FRAGMENT_EVENT_TYPE_PHOTOMETRIC_MEASURE = 1;
    public static final int UPDATE_FRAGMENT_EVENT_TYPE_TIME_SCAN = 2;

    private int type;
    private float wavelength;
    private float abs;
    private float trans;
    private int energy;

    public UpdateFragmentEvent()
    {}

    public UpdateFragmentEvent(int type, float wavelength, float abs, float trans, int energy) {
        this.type = type;
        this.wavelength = wavelength;
        this.abs = abs;
        this.trans = trans;
        this.energy = energy;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public float getWavelength() {
        return wavelength;
    }

    public void setWavelength(float wl) {
        this.wavelength = wl;
    }

    public float getAbs() {
        return abs;
    }

    public void setAbs(float abs) {
        this.abs = abs;
    }

    public float getTrans() {
        return trans;
    }

    public void setTrans(float trans) {
        this.trans = trans;
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }
}
