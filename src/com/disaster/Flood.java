package com.disaster;

public class Flood extends Disaster{
	public int peopleKilled;
	public int peopleDisplaced;
	
	public Flood() {
		this.disasterType = DisasterType.Flood;
	}
}
