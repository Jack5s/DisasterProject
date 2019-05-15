package Common;

import com.disaster.DisasterType;

public class Common {
	// Time period
	public static int startYear = 2014;
	public static int startMonth = 8;
	public static int startDay = 24;

	public static int endYear = 2014;
	public static int endMonth = 8;
	public static int endDay = 25;

	// output Result fileName, split by ','
	public static String reliefFileName = "C:\\Users\\s1061395\\Course\\Software Development\\napa_earthquake\\Relief.csv";
	public static String gdacsFileName = "C:\\Users\\s1061395\\Course\\Software Development\\napa_earthquake\\GDACS.csv";
	public static String emsFileName = "C:\\Users\\s1061395\\Course\\Software Development\\napa_earthquake\\EMS.csv";
	public static String gdeltFileName = "C:\\Users\\s1061395\\Course\\Software Development\\napa_earthquake\\GDELT.csv";

	public static DisasterType disasterType = DisasterType.Earthquake;
}
