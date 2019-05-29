package com.webscraper.jsoup;

import java.io.PrintWriter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.connection.SQLConnection;
import com.converter.DateConverter;
import com.disaster.Disaster;
import com.disaster.DisasterType;
import com.google.gson.Gson;

import Common.Common;

public class ReliefWebScraper {
	public Disaster[] disastersArray;

	// public String testData;

	public ReliefWebScraper() throws Exception {
		Document disasterDoc = Jsoup.connect("https://reliefweb.int/disasters").maxBodySize(0).get();
		Element headElement = disasterDoc.getElementsByTag("head").first();
		Elements scriptElements = headElement.getElementsByTag("script");

		PrintWriter clearWriter = new PrintWriter(Common.reliefFileName);
		clearWriter.close();

		// System.out.println(scriptElements.size());
		for (Element element : scriptElements) {
			String scriptStr = element.html();
			// ["wkt":"GEOMETRYCOLLECTION(POINT(1525997.63605671 5994608.04079255))]
			int beginIndex = scriptStr.indexOf("\"data\":");
			int endIndex = scriptStr.lastIndexOf(';') - 4;
			if (beginIndex == -1 || endIndex == -1) {
				continue;
			}
			scriptStr = scriptStr.substring(beginIndex, endIndex);
			scriptStr = "{" + scriptStr + "}";
			// System.out.println(scriptStr);
			Gson gson = new Gson();
			ReliefGeoJson reliefGeoJson = gson.fromJson(scriptStr, ReliefGeoJson.class);
			disastersArray = new Disaster[reliefGeoJson.data.features.length];
			for (int i = 0; i < disastersArray.length; i++) {
				Disaster disaster = new Disaster();
				String iconStr = reliefGeoJson.data.features[i].properties.icon;
				// System.out.println(iconStr);
				disaster.disasterType = DisasterType.getDisasterTypeFromString(iconStr);
				disaster.longitude = reliefGeoJson.data.features[i].features[0].geometry.coordinates[0];
				disaster.latitude = reliefGeoJson.data.features[i].features[0].geometry.coordinates[1];
				String disasterLink = reliefGeoJson.data.features[i].properties.url;
				// System.out.println(disasterLink);
				getAttributes(disasterLink, disaster);
				disaster.id = "0";
				disaster.webSource = "relief";
				if (disaster.startYear >= Common.startYear && disaster.startYear <= Common.endYear
						&& disaster.startMonth >= Common.startMonth && disaster.startMonth <= Common.endMonth
						&& disaster.startDay >= Common.startDay && disaster.startDay <= Common.endDay && disaster.disasterType == Common.disasterType) {
//					SQLConnection.insertToFile(disaster, Common.reliefFileName);
				} else {
					continue;
				}

				// SQLConnection.insert(disaster);
				if (disaster.disasterType == DisasterType.SpecialEvent) {
					System.out.println(iconStr);
					System.out.println(disaster.url);
				}
				disastersArray[i] = disaster;
			}
			break;
		}
	}

	private void getAttributes(String disasterLink, Disaster disaster) throws Exception {
		disaster.url = disasterLink;
		Document doc = Jsoup.connect(disasterLink).maxBodySize(0).get();
		Element divElement = doc.getElementsByAttributeValue("class", "profile-sections-description").first();
		Elements aElements = divElement.getElementsByTag("a");
		for (Element element : aElements) {
			String content = element.text();
			// System.out.println(content);
			int beginIndex = content.lastIndexOf('(');
			int endIndex = content.lastIndexOf(')');
			String dateStr = "";
			if (beginIndex == -1 || endIndex == -1) {
				dateStr = content;
			} else {
				dateStr = content.substring(beginIndex, endIndex);
			}
			int commaIndex = dateStr.indexOf(',');
			if (commaIndex == -1) {
				continue;
			}
			dateStr = dateStr.substring(commaIndex + 2);
			// System.out.println(dateStr);
			String[] dateArray = dateStr.split(" ");
			if (dateArray.length != 3) {
				continue;
			}
			disaster.startDay = Integer.valueOf(dateArray[0]);
			disaster.startMonth = DateConverter.getMonthFromStr(dateArray[1]);
			disaster.startYear = Integer.valueOf(dateArray[2]);
			break;
		}
		Element idElement = doc.getElementsByAttributeValue("class", "glide").first();
		// Example: if Glide is FF-2019-000018-AFG
		// FF is type of disaster, Flash Flood
		// 2019 is year
		// 000018 is id, it means the disaster is the 18th disaster in 2019
		// AFG is the disaster place

		String idStr = idElement.text();
		// FF-2019-000018-AFG
		idStr = idStr.substring(7);
		String[] idArray = idStr.split("-");
	}
}
