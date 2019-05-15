package com.disaster;

public class Drought extends Disaster {
	public String name;
	public String[] countries;

	public Drought() {
		this.disasterType = DisasterType.Drought;
	}
}
