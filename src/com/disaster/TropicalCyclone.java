package com.disaster;

public class TropicalCyclone extends Disaster{
	public String name;
	public int endDay;
	public int endMonth;
	public String[] countries;
	public double MaxWindSpeed;
	public double MaxStormSurge;
	
	public TropicalCyclone() {
		this.disasterType = DisasterType.Storm;
	}
}
