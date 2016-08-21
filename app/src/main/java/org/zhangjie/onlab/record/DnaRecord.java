package org.zhangjie.onlab.record;

public class DnaRecord extends BaseData {
	private int index;
	private String name;
	private float wavelength1;
	private float wavelength2;
	private float wavelengthRef;
	private float dna;
	private float protein;
	private long time;
	public DnaRecord() {
	}

	public DnaRecord(int index, String name, float wavelength1, float wavelength2,
			float wavelengthRef, float dna, float protein, long date) {
		this.index = index;
		this.name = name;
		this.wavelength1 = wavelength1;
		this.wavelength2 = wavelength2;
		this.wavelengthRef = wavelengthRef;
		this.dna = dna;
		this.protein = protein;
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

	public float getWavelength1() {
		return wavelength1;
	}

	public void setWavelength1(float wavelength1) {
		this.wavelength1 = wavelength1;
	}

	public float getWavelength2() {
		return wavelength2;
	}

	public void setWavelength2(float wavelength2) {
		this.wavelength2 = wavelength2;
	}

	public float getWavelengthRef() {
		return wavelengthRef;
	}

	public void setWavelengthRef(float wavelengthRef) {
		this.wavelengthRef = wavelengthRef;
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

	public long getDate() {
		return time;
	}

	public void setDate(long date) {
		this.time = date;
	}

}
