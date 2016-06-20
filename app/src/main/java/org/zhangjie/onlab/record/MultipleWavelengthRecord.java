package org.zhangjie.onlab.record;

public class MultipleWavelengthRecord extends BaseData {
	private int index;
	private float wavelength;
	private float abs;
	private float trans;
	private int energy;
	private long time;

	public MultipleWavelengthRecord() {
	}

	public MultipleWavelengthRecord(int index, float wavelength, float abs,
									float trans, int energy, long date) {
		this.index = index;
		this.wavelength = wavelength;
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

	public long getDate() {
		return time;
	}

	public void setDate(long date) {
		this.time = date;
	}

}
