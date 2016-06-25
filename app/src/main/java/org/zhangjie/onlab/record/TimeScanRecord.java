package org.zhangjie.onlab.record;

public class TimeScanRecord extends BaseData {
    private int index;
    private float abs;
    private float trans;
    private int energy;
    private long time;
    private int second;

    public TimeScanRecord() {
    }

    public TimeScanRecord(int index, int second, float abs,
                          float trans, int energy, long date) {
        this.index = index;
        this.second = second;
        this.abs = abs;
        this.trans = trans;
        this.energy = energy;
        this.time = date;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
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

    public long getDate() {
        return time;
    }

    public void setDate(long date) {
        this.time = date;
    }

}
