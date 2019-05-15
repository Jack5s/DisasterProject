package com.webscraper.jsoup;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.connection.SQLConnection;
import com.converter.DateConverter;
import com.disaster.Disaster;
import com.disaster.DisasterType;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import Common.Common;

public class GDACSWebAllRecordScraper {
	public ArrayList<Disaster> disasterList = new ArrayList<>();

	public GDACSWebAllRecordScraper() throws Exception {
		Document doc = Jsoup.connect("http://www.gdacs.org/datareport/resources/").maxBodySize(0).get();
		Element preElement = doc.getElementsByTag("pre").first();
		Elements aElements = preElement.getElementsByTag("a");

		PrintWriter clearWriter = new PrintWriter(Common.gdacsFileName);
		clearWriter.close();

		for (Element element : aElements) {
			try {
				String contentStr = element.text();
				String disasterArrayLinkStr = "http://www.gdacs.org" + element.attributes().get("href");

				if (contentStr.compareTo("DR") == 0 || contentStr.compareTo("EQ") == 0
						|| contentStr.compareTo("FL") == 0 || contentStr.compareTo("TC") == 0
						|| contentStr.compareTo("VO") == 0 || contentStr.compareTo("VW") == 0) {
					System.out.println(disasterArrayLinkStr);
					Document disasterDoc = Jsoup.connect(disasterArrayLinkStr).maxBodySize(0).get();

					Element disasterPreElement = disasterDoc.getElementsByTag("pre").first();
					Elements disasteraElements = disasterPreElement.getElementsByTag("a");
					int disasterCount = disasteraElements.size() - 1;
					// disasterCount = 1;
					System.out.println(disasterCount);
					for (Element disasterElement : disasteraElements) {
						if (disasterElement.text().compareTo("[To Parent Directory]") == 0) {
							continue;
						}
						String disasterLinkStr = "http://www.gdacs.org" + disasterElement.attributes().get("href");
						String id = disasterElement.text();
						System.out.println(disasterLinkStr);
						Disaster disaster = new Disaster();
						switch (contentStr) {
						case "DR":
							disaster.id = "GDACS-DR-" + id;
							disaster.disasterType = DisasterType.Drought;
							break;
						case "EQ":
							disaster.id = "GDACS-EQ-" + id;
							disaster.disasterType = DisasterType.Earthquake;
							break;
						case "FL":
							disaster.id = "GDACS-FL-" + id;
							disaster.disasterType = DisasterType.Flood;
							break;
						case "TC":// wind
							disaster.id = "GDACS-TC-" + id;
							disaster.disasterType = DisasterType.Storm;
							break;
						case "VO":
							disaster.id = "GDACS-VO-" + id;
							disaster.disasterType = DisasterType.Volcano;
							break;
						case "VW":// wind
							disaster.id = "GDACS-TC-" + id;
							disaster.disasterType = DisasterType.Storm;
							break;
						default:
							disaster = null;
							break;
						}
						if (disaster != null) {
							if (disaster.disasterType == Common.disasterType) {
							} else {
								continue;
							}
							getDisasterFromGeoJson(disasterLinkStr, disaster);
							System.out.println(disaster.id);
							if (disaster.startYear >= Common.startYear && disaster.startYear <= Common.endYear
									&& disaster.startMonth >= Common.startMonth
									&& disaster.startMonth <= Common.endMonth && disaster.startDay >= Common.startDay
									&& disaster.startDay <= Common.endDay) {
								SQLConnection.insertToFile(disaster, Common.gdacsFileName);
							} else {
								continue;
							}
							// SQLConnection.insert(disaster);
							disasterList.add(disaster);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				continue;
			}
		}
	}

	private void getDisasterFromGeoJson(String linkStr, Disaster disaster) throws Exception {
		// http://www.gdacs.org/datareport/resources/DR/1012167/geojson_1012167_1.geojson
		disaster.url = linkStr;
		Document disasterDoc = Jsoup.connect(linkStr).maxBodySize(0).get();
		Element preElement = disasterDoc.getElementsByTag("pre").first();
		Elements aElements = preElement.getElementsByTag("a");
		boolean findGeoJsonflag = false;
		for (Element element : aElements) {
			String contentStr = element.text();
			int index = contentStr.indexOf(".geojson");
			// System.out.println(contentStr);
			if (index > 0) {
				String geoJsonLinkStr = "http://www.gdacs.org" + element.attributes().get("href");
				findGeoJsonflag = true;
				// System.out.println(geoJsonLinkStr);
				// UserAgent connot be used here because the doc of UserAgent is always empty
				URL url = new URL(geoJsonLinkStr);
				JsonReader jsonReader = new JsonReader(new InputStreamReader(url.openStream()));
				jsonReader.beginObject();
				while (jsonReader.hasNext()) {
					String name = jsonReader.nextName();
					if (name.compareTo("features") == 0) {
						jsonReader.beginArray();
						while (jsonReader.hasNext()) {
							jsonReader.beginObject();
							while (jsonReader.hasNext()) {
								String featureName = jsonReader.nextName();
								// System.out.println(featureName);
								switch (featureName) {
								case "properties":
									getProperties(jsonReader, disaster);
									break;
								case "geometry":
									jsonReader.beginObject();
									while (jsonReader.hasNext()) {
										String geometryName = jsonReader.nextName();
										if (geometryName.compareTo("coordinates") == 0) {
											jsonReader.beginArray();
											while (jsonReader.hasNext()) {
												// "true" is point feature
												if (jsonReader.peek() == JsonToken.NUMBER) {
													disaster.longitude = jsonReader.nextDouble();
													disaster.latitude = jsonReader.nextDouble();
													// System.out.println(drought.longitude + "," + drought.latitude);
												}
												// "false" is polygon feature
												else {
													jsonReader.skipValue();
												}
											}
											jsonReader.endArray();
										} else {
											jsonReader.skipValue();
										}
									}
									jsonReader.endObject();
									break;
								default:
									jsonReader.skipValue();
									break;
								}
							}
							jsonReader.endObject();
						}
						jsonReader.endArray();
					} else {
						jsonReader.skipValue();
					}
				}
				jsonReader.endObject();
				jsonReader.close();
			}
		}
		if (findGeoJsonflag == false) {
			System.out.println(linkStr);
			disaster = null;
		}
	}

	private void getProperties(JsonReader jsonReader, Disaster disaster) throws Exception {
		jsonReader.beginObject();
		while (jsonReader.hasNext()) {
			String propertyName = jsonReader.nextName();
			// System.out.println(propertyName);
			switch (disaster.disasterType) {
			case Drought:
				switch (propertyName) {
				case "alertlevel":
					jsonReader.skipValue();
					break;
				case "alertscore":
					jsonReader.skipValue();
					break;
				case "fromdate":
					String startDateStr = jsonReader.nextString();
					getStartDatefromString(startDateStr, disaster);
					break;
				case "todate":
					String endDateStr = jsonReader.nextString();
					getEndDatefromString(endDateStr, disaster);
					break;
				default:
					jsonReader.skipValue();
					break;
				}
				break;
			case Earthquake:
				switch (propertyName) {
				case "alertlevel":
					jsonReader.skipValue();
					break;
				case "alertscore":
					jsonReader.skipValue();
					break;
				case "fromdate":
					String startDateStr = jsonReader.nextString();
					getStartDatefromString(startDateStr, disaster);
					break;
				case "todate":
					String endDateStr = jsonReader.nextString();
					getEndDatefromString(endDateStr, disaster);
					break;
				default:
					jsonReader.skipValue();
					break;
				}
				break;
			default:
				break;
			}
		}
		jsonReader.endObject();

	}

	private void getStartDatefromString(String dateStr, Disaster disaster) {
		String[] dateStrArray = dateStr.split(" ");
		String[] dateArray = dateStrArray[0].split("/");
		disaster.startDay = Integer.valueOf(dateArray[0]);
		disaster.startMonth = DateConverter.getMonthFromStr(dateArray[1]);
		disaster.startYear = Integer.valueOf(dateArray[2]);
		String[] timeArray = dateStrArray[1].split(":");
		disaster.startHour = Integer.valueOf(timeArray[0]);
		disaster.startMinute = Integer.valueOf(timeArray[1]);
		disaster.startHour = Integer.valueOf(timeArray[2]);
	}

	private void getEndDatefromString(String dateStr, Disaster disaster) {
		String[] dateStrArray = dateStr.split(" ");
		String[] dateArray = dateStrArray[0].split("/");
		disaster.endDay = Integer.valueOf(dateArray[0]);
		disaster.endMonth = DateConverter.getMonthFromStr(dateArray[1]);
		disaster.endYear = Integer.valueOf(dateArray[2]);
		String[] timeArray = dateStrArray[1].split(":");
		disaster.endHour = Integer.valueOf(timeArray[0]);
		disaster.endMinute = Integer.valueOf(timeArray[1]);
	}
}
