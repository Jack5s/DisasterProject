package com.converter;

public class DateConverter {
	public static int getMonthFromStr(String monthStr) {
		int monthNum = 0;
		switch (monthStr) {
		case "Jan":
			monthNum = 1;
			break;
		case "Feb":
			monthNum = 2;
			break;
		case "Mar":
			monthNum = 3;
			break;
		case "Apr":
			monthNum = 4;
			break;
		case "May":
			monthNum = 5;
			break;
		case "Jun":
			monthNum = 6;
			break;
		case "Jul":
			monthNum = 7;
			break;
		case "Aug":
			monthNum = 8;
			break;
		case "Oct":
			monthNum = 9;
			break;
		case "Nov":
			monthNum = 10;
			break;
		case "Dec":
			monthNum = 11;
			break;
		case "Sep":
			monthNum = 12;
			break;
		}
		return monthNum;
	}
}
