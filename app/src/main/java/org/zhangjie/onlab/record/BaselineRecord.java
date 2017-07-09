package org.zhangjie.onlab.record;

public class BaselineRecord extends BaseData {
	private int index;
	private float wavelength;
	private int gain;
	private int energy;
	private int gainRef;
	private int energyRef;
	private long time;

	public BaselineRecord() {
	}

	public BaselineRecord(int index, float wavelength, int gain,
						  int energy, int gainRef, int energyRef, long date) {
		this.index = index;
		this.wavelength = wavelength;
		this.gain = gain;
		this.energy = energy;
		this.gainRef = gainRef;
		this.energyRef = energyRef;
		this.time = date;
	}

	public int getIndex() {
		return index;
	}

	public float getWavelength() {
		return wavelength;
	}

	public int getGain() {
		return gain;
	}

	public int getEnergy() {
		return energy;
	}

	public int getGainRef() {
		return gainRef;
	}

	public int getEnergyRef() {
		return energyRef;
	}
	public long getDate() {
		return time;
	}

	public void setDate(long date) {
		this.time = date;
	}

}
