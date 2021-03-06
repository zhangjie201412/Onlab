package org.zhangjie.onlab.record;

public class WavelengthScanRecord extends BaseData {
    private int index;
    private float wavelength;
    private float abs;
    private float trans;
    private int energy;
    private int energyRef;
    private long time;

    public WavelengthScanRecord() {
    }

    public WavelengthScanRecord(int index, float wavelength, float abs,
                                float trans, int energy, int energyRef, long date) {
        this.index = index;
        this.wavelength = wavelength;
        this.abs = abs;
        this.trans = trans;
        this.energy = energy;
        this.energyRef = energyRef;
        this.time = date;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public float getWavelength() {
        return wavelength;
    }

    public void setWavelength(float wavelength) {
        this.wavelength = wavelength;
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

    public int getEnergyRef() {
        return energyRef;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public long getDate() {
        return time;
    }

    public void setDate(long date) {
        this.time = date;
    }

}
