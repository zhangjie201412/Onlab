package org.zhangjie.onlab.record;

public class DnaRecord extends BaseData {
	private int index;
	private String name;
	private float abs1;
	private float abs2;
	private float absRef;
	private float dna;
	private float protein;
	private float ratio;
	private long time;
	public DnaRecord() {
	}

	public DnaRecord(int index, String name, float abs1, float abs2,
			float absRef, float dna, float protein, float ratio, long date) {
		this.index = index;
		this.name = name;
		this.abs1 = abs1;
		this.abs2 = abs2;
		this.absRef = absRef;
		this.dna = dna;
		this.protein = protein;
		this.ratio = ratio;
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

	public float getAbs1() {
		return abs1;
	}

	public void setAbs1(float abs1) {
		this.abs1 = abs1;
	}

	public float getAbs2() {
		return abs2;
	}

	public void setAbs2(float abs2) {
		this.abs2 = abs2;
	}

	public float getAbsRef() {
		return absRef;
	}

	public void setAbsRef(float absRef) {
		this.absRef = absRef;
	}

	public float getDna() {
		return dna;
	}

	public void setDna(float dna) {
		this.dna = dna;
	}

	public float getProtein() {
		return protein;
	}

	public void setProtein(float protein) {
		this.protein = protein;
	}

	public float getRatio() {
		return ratio;
	}

	public void setRatio(float ratio) {
		this.ratio = ratio;
	}

	public long getDate() {
		return time;
	}

	public void setDate(long date) {
		this.time = date;
	}

}
