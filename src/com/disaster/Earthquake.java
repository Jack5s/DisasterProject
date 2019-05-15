package com.disaster;

public class Earthquake extends Disaster {
	public double magnitude;
	public double depth;// Unit:km
	
	public Earthquake() {
		this.disasterType = DisasterType.Earthquake;
	}
}
