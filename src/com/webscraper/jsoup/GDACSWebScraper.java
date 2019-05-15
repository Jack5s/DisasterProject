package com.webscraper.jsoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.converter.DateConverter;
import com.disaster.*;

public class GDACSWebScraper {
	public Earthquake[] earthquakeArray;
	public TropicalCyclone[] tropicalCycloneArray;
	public Flood[] floodArray;
	public Volcano[] volcanoeArray;
	public Drought[] droughtArray;
	public String aa;

	public GDACSWebScraper() throws Exception {
		Document doc = Jsoup.connect("http://www.gdacs.org/").maxBodySize(0).get();
		getEarthquakes(doc);
		getTropicalCyclones(doc);
		getFloods(doc);// no event recently
		getVolcanoes(doc);// no event recently
		getDrought(doc);
	}

	private void getEarthquakes(Document document) throws Exception {
		Elements earthquakeElements = document.getElementById("gdacs_eventtype_EQ").getElementsByTag("div");
		int earthquakeCount = earthquakeElements.size();
		earthquakeArray = new Earthquake[earthquakeCount];
		int i = 0;
		for (Element ele : earthquakeElements) {
			if (ele.child(0).attributes().hasKey("href") == false) {
				earthquakeArray = null;
				return;
			}
			String earthquakeLink = ele.child(0).attributes().get("href");
			earthquakeLink = earthquakeLink.replaceAll("amp;", "");

			Document earthquakeDocumnet = Jsoup.connect(earthquakeLink).maxBodySize(0).get();
			Element earthquakeTable = earthquakeDocumnet.getElementById("alert_summary_left").child(2);
			Earthquake earthquake = new Earthquake();
			earthquake.disasterType = DisasterType.Earthquake;
			Element rowData = earthquakeTable.child(1);// Earthquake Magnitude

			String magnitudeString = rowData.child(1).text();
			magnitudeString = magnitudeString.substring(0, magnitudeString.length() - 1);
			earthquake.magnitude = Double.valueOf(magnitudeString);

			rowData = earthquakeTable.child(2);// Earthquake Depth
			String depthStr = rowData.child(1).text();
			depthStr = depthStr.substring(0, depthStr.length() - 2);
			earthquake.depth = Double.valueOf(depthStr.substring(0, depthStr.length() - 2));

			rowData = earthquakeTable.child(3);// latitude and longitude
			String latlngStr = rowData.child(1).text();
			String[] latlngArr = latlngStr.split(" ");
			earthquake.latitude = Double.valueOf(latlngArr[0]);
			earthquake.longitude = Double.valueOf(latlngArr[2]);

			rowData = earthquakeTable.child(4);// Earthquake Date
			String dateStr = rowData.child(1).text();
			String[] dateArr = dateStr.split(" ");
			earthquake.startDay = Integer.valueOf(dateArr[0]);
			earthquake.startMonth = DateConverter.getMonthFromStr(dateArr[1]);
			earthquake.startYear = Integer.valueOf(dateArr[2]);
			String[] timeArr = dateArr[3].split(":");
			earthquake.startHour = Integer.valueOf(timeArr[0]);
			earthquake.startMinute = Integer.valueOf(timeArr[1]);
			earthquakeArray[i] = earthquake;
			i++;
		}
	}

	private void getTropicalCyclones(Document document) throws Exception {
		Elements tropicalCycloneElements = document.getElementById("gdacs_eventtype_TC").getElementsByTag("div");
		int tropicalCycloneElementsCount = tropicalCycloneElements.size();

		tropicalCycloneArray = new TropicalCyclone[tropicalCycloneElementsCount];
		int i = 0;
		for (Element ele : tropicalCycloneElements) {
			if (ele.child(0).attributes().hasKey("href") == false) {
				tropicalCycloneArray = null;
				return;
			}
			String tropicalCycloneLink = ele.child(0).attributes().get("href");
			tropicalCycloneLink = tropicalCycloneLink.replaceAll("amp;", "");

			Document tropicalCycloneDocumnet = Jsoup.connect(tropicalCycloneLink).maxBodySize(0).get();
			Element tropicalCycloneTable = tropicalCycloneDocumnet.getElementById("alert_summary_left").child(2);

			TropicalCyclone tropicalCyclone = new TropicalCyclone();
			tropicalCyclone.disasterType = DisasterType.Storm;
			Element rowData = tropicalCycloneTable.child(1);// tropicalCyclone Name
			tropicalCyclone.name = rowData.child(1).text();

			rowData = tropicalCycloneTable.child(2);// tropicalCyclone start and end time
			String tropicalCycloneDurationStr = rowData.child(1).text();
			String[] tropicalCycloneDurationArr = tropicalCycloneDurationStr.split(" ");
			tropicalCyclone.startDay = Integer.valueOf(tropicalCycloneDurationArr[0]);
			tropicalCyclone.startMonth = Integer.valueOf(DateConverter.getMonthFromStr(tropicalCycloneDurationArr[1]));
			tropicalCyclone.endDay = Integer.valueOf(tropicalCycloneDurationArr[3]);
			tropicalCyclone.endMonth = Integer.valueOf(DateConverter.getMonthFromStr(tropicalCycloneDurationArr[4]));

			rowData = tropicalCycloneTable.child(3);// tropicalCyclone start and end time
			String tropicalCycloneCountiesStr = rowData.child(1).text();
			tropicalCyclone.countries = tropicalCycloneCountiesStr.split(", ");

			rowData = tropicalCycloneTable.child(5);// max wind speed
			String tropicalCycloneMaxSpeedStr = rowData.child(1).text();
			tropicalCycloneMaxSpeedStr = tropicalCycloneMaxSpeedStr.substring(0,
					tropicalCycloneMaxSpeedStr.indexOf('k') - 1);
			tropicalCyclone.MaxWindSpeed = Double.valueOf(tropicalCycloneMaxSpeedStr);

			rowData = tropicalCycloneTable.child(6);// max storm surge
			String tropicalCycloneMaxSurgeStr = rowData.child(1).text();
			int surgeUnitIndex = tropicalCycloneMaxSurgeStr.indexOf('m');
			if (surgeUnitIndex > 0) {// Sometimes it will have missing data about surge
				tropicalCycloneMaxSurgeStr = tropicalCycloneMaxSurgeStr.substring(0,
						tropicalCycloneMaxSurgeStr.indexOf('m') - 1);
				tropicalCyclone.MaxStormSurge = Double.valueOf(tropicalCycloneMaxSurgeStr);
			} else {
				tropicalCyclone.MaxStormSurge = -999;
			}

			tropicalCycloneArray[i] = tropicalCyclone;
			i++;
		}
	}

	private void getFloods(Document document) throws Exception {
		Elements floodElements = document.getElementById("gdacs_eventtype_FL").getElementsByTag("div");
		int floodElementsCount = floodElements.size();

		floodArray = new Flood[floodElementsCount];
		int i = 0;
		for (Element ele : floodElements) {
			if (ele.child(0).attributes().hasKey("href") == false) {
				floodArray = null;
				return;
			}
			String floodLink = ele.child(0).attributes().get("href");
			floodLink = floodLink.replaceAll("amp;", "");

			Document floodDocumnet = Jsoup.connect(floodLink).maxBodySize(0).get();
			Element floodTable = floodDocumnet.getElementById("alert_summary_left").child(2);
			Flood flood = new Flood();
			flood.disasterType = DisasterType.Flood;
			floodArray[i] = flood;
			i++;
		}
	}

	private void getVolcanoes(Document document) throws Exception {
		Elements volcanoeElements = document.getElementById("gdacs_eventtype_VO").getElementsByTag("div");
		int volcanoeElementsCount = volcanoeElements.size();
		volcanoeArray = new Volcano[volcanoeElementsCount];
		int i = 0;
		for (Element ele : volcanoeElements) {
			if (ele.child(0).attributes().hasKey("href") == false) {
				volcanoeArray = null;
				return;
			}
			String volcanoeLink = ele.child(0).attributes().get("href");
			volcanoeLink = volcanoeLink.replaceAll("amp;", "");

			Document volcanoeDocumnet = Jsoup.connect(volcanoeLink).maxBodySize(0).get();
			Element volcanoeTable = volcanoeDocumnet.getElementById("alert_summary_left").child(2);
			Volcano volcanoe = new Volcano();
			volcanoe.disasterType = DisasterType.Volcano;
			volcanoeArray[i] = volcanoe;
			i++;
		}
	}

	private void getDrought(Document document) throws Exception {

		Elements droughtElements = document.getElementById("gdacs_eventtype_DR").getElementsByTag("div");
		int droughtElementsCount = droughtElements.size();
		droughtArray = new Drought[droughtElementsCount];
		int i = 0;
		for (Element ele : droughtElements) {
			if (ele.child(0).attributes().hasKey("href") == false) {
				droughtArray = null;
				return;
			}

			String droughtLink = ele.child(0).attributes().get("href");
			droughtLink = droughtLink.replaceAll("amp;", "");

			Document droughtDocumnet = Jsoup.connect(droughtLink).maxBodySize(0).get();
			Element droughtTable = droughtDocumnet.getElementById("alert_summary_left").child(2);

			Drought drought = new Drought();
			drought.disasterType = DisasterType.Drought;
			Element rowData = droughtTable.child(1);// drought name
			drought.name = rowData.child(1).text();

			rowData = droughtTable.child(2);// drought counties
			drought.countries = rowData.child(1).text().split(", ");

			rowData = droughtTable.child(3);// drought start time
			String starttimeStr = rowData.child(1).text();
			String[] starttimeArr = starttimeStr.split(" ");
			// drought.StartTimeOfMonth = starttimeArr[0];
			drought.startMonth = DateConverter.getMonthFromStr(starttimeArr[2]);
			drought.startYear = Integer.valueOf(starttimeArr[3]);

			rowData = droughtTable.child(4);// drought duration
			String durationStr = rowData.child(1).text();
			durationStr = durationStr.substring(0, durationStr.length() - 5);
			// drought.duration = Integer.valueOf(durationStr);

			droughtArray[i] = drought;
			i++;
		}
	}

}
