package com.disaster;

//UTC time
public class Disaster {
	public String id;
	public double longitude;
	public double latitude;

	// if only get one information about date, the start data would be set to the
	// date, end date would be default value
	public int startMinute;
	public int startHour;
	public int startDay;
	public int startMonth;
	public int startYear;

	public int endMinute;
	public int endHour;
	public int endDay;
	public int endMonth;
	public int endYear;

	public String webSource;
	public String url;
	public DisasterType disasterType;

	public Disaster() {
		this.longitude = -999;
		this.latitude = -999;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.url + "," + this.disasterType;
	}
}