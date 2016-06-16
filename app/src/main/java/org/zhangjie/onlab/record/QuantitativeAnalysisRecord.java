package org.zhangjie.onlab.record;

public class QuantitativeAnalysisRecord extends BaseData {
    private int index;
    private String name;
    private float abs;
    private float conc;
    private long time;

    public QuantitativeAnalysisRecord() {
    }

    public QuantitativeAnalysisRecord(int index, String name, float abs,
                                      float conc, long date) {
        this.index = index;
        this.name = name;
        this.abs = abs;
        this.conc = conc;
        this.time = date;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getAbs() {
        return abs;
    }

    public void setAbs(float abs) {
        this.abs = abs;
    }

    public float getConc() {
        return conc;
    }

    public void setConc(float conc) {
        this.conc = conc;
    }

    public long getDate() {
        return time;
    }

    public void setDate(long date) {
        this.time = date;
    }

}
