package com.webscraper.jsoup;

import java.io.PrintWriter;
import java.util.ArrayList;

import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.connection.SQLConnection;
import com.disaster.Disaster;
import com.disaster.DisasterType;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import Common.Common;

public class EMSWebScraper {
	public ArrayList<Disaster> disastersList;
	private String EMSUrl = "https://emergency.copernicus.eu";

	public EMSWebScraper() throws Exception {
		Document doc = Jsoup.connect("https://emergency.copernicus.eu/mapping/list-of-activations-rapid").maxBodySize(0)
				.get();
		Element tableEle = doc.getElementsByAttributeValue("class", "views-table cols-7").first();
		Elements trElements = tableEle.getElementsByTag("tbody").first().getElementsByTag("tr");
		int disasterCount = trElements.size();
		disastersList = new ArrayList<>(disasterCount);

		PrintWriter clearWriter = new PrintWriter(Common.emsFileName);
		clearWriter.close();

		for (Element rowElement : trElements) {
			Disaster disaster = new Disaster();
			String diasaterId = rowElement.child(1).text().trim();
			disaster.id = diasaterId;
			String disasterLink = EMSUrl + rowElement.child(2).child(0).attributes().get("href");

			getAttributes(disasterLink, disaster);
			System.out.println(disaster.startYear + "-" + disaster.startMonth + "-" + disaster.startDay);
			disastersList.add(disaster);
			if (disaster.startYear >= Common.startYear && disaster.startYear <= Common.endYear
					&& disaster.startMonth >= Common.startMonth && disaster.startMonth <= Common.endMonth
					&& disaster.startDay >= Common.startDay && disaster.startDay <= Common.endDay
					&& disaster.disasterType == Common.disasterType) {
//				SQLConnection.insertToFile(disaster, Common.emsFileName);
				System.out.println("ok");
			} else {

			}
		}
		System.out.println("ok!!");
	}

	private void getAttributes(String disasterLink, Disaster disaster) throws Exception {
		Document disasterDoc = Jsoup.connect(disasterLink).maxBodySize(0).get();
		disaster.url = disasterLink;
		Element contentElement = disasterDoc
				.getElementsByAttributeValue("class", "obsolete-components-header--field-obsolete clearfix").first();
		Element rowElement = contentElement.child(2);
		// The first data row. The value can be event time or disaster type
		String checkStr = rowElement.child(0).text();
		String disasterTypeStr = "";
		// System.out.println(checkStr);
		if (checkStr.compareTo("Event Time (UTC):") == 0) {// The first row is event time
			String timeStr = rowElement.child(1).text();
			int spaceIndex = timeStr.indexOf(' ');
			String[] dateArray = timeStr.substring(0, spaceIndex).split("-");
			disaster.startYear = Integer.valueOf(dateArray[0]);
			disaster.startMonth = Integer.valueOf(dateArray[1]);
			disaster.startDay = Integer.valueOf(dateArray[2]);
			String[] timeArray = timeStr.substring(spaceIndex + 1, timeStr.length()).split(":");
			disaster.startHour = Integer.valueOf(timeArray[0]);
			disaster.startMinute = Integer.valueOf(timeArray[1]);

			rowElement = contentElement.child(4);// get disaster type
			disasterTypeStr = rowElement.child(1).text();
			disaster.disasterType = DisasterType.getDisasterTypeFromString(disasterTypeStr);
		} else if (checkStr.compareTo("Event Type:") == 0) {// The first row is disaster type
			rowElement = contentElement.child(3);
			// System.out.println(disasterTypeStr);
			String timeStr = rowElement.child(1).text();
			int spaceIndex = timeStr.indexOf(' ');
			String[] dateArray = timeStr.substring(0, spaceIndex).split("-");
			disaster.startYear = Integer.valueOf(dateArray[0]);
			disaster.startMonth = Integer.valueOf(dateArray[1]);
			disaster.startDay = Integer.valueOf(dateArray[2]);
			String[] timeArray = timeStr.substring(spaceIndex + 1, timeStr.length()).split(":");
			disaster.startHour = Integer.valueOf(timeArray[0]);
			disaster.startMinute = Integer.valueOf(timeArray[1]);

			rowElement = contentElement.child(2);// get disaster type
			disasterTypeStr = rowElement.child(1).text();
			disaster.disasterType = DisasterType.getDisasterTypeFromString(disasterTypeStr);
		}
		if (disaster.disasterType == DisasterType.SpecialEvent) {
			Element titleElement = contentElement.child(0);
			String titleStr = titleElement.text().trim();
			disaster.disasterType = DisasterType.getDisasterTypeFromString(titleStr);

			if (disaster.disasterType == DisasterType.SpecialEvent) {
				System.out.println(titleStr);
				System.out.println(disaster.url);
			}
		}
		// get Position
		Element headElement = disasterDoc.getElementsByTag("head").first();
		Elements scriptElements = headElement.getElementsByTag("script");
		// System.out.println(scriptElements.size());
		for (Element element : scriptElements) {
			String scriptStr = element.html();
			// sample: ["wkt":"GEOMETRYCOLLECTION(POINT(1525997.63605671 5994608.04079255))]
			int pointStartIndex = scriptStr.indexOf("POINT(");
			// System.out.println(scriptStr);
			// System.out.println(element.html());
			int pointEndIndex = scriptStr.indexOf(')', pointStartIndex);
			if (pointStartIndex == -1 || pointEndIndex == -1) {
				continue;
			}
			String pointPositionStr = scriptStr.substring(pointStartIndex + 6, pointEndIndex);
			// System.out.println(pointPositionStr);
			String[] positionArray = pointPositionStr.split(" ");
			double x = Double.valueOf(positionArray[0]);
			double y = Double.valueOf(positionArray[1]);
			// System.out.println(x + "," + y);
			GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
			Coordinate coordinate = new Coordinate(x, y);
			Point point = geometryFactory.createPoint(coordinate);
			CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:3857");// web Mecator
			CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:4326");// WGS 1984
			MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);
			Point resultPoint = (Point) JTS.transform(point, transform);
			// System.out.println(resultPoint.getX() + "," + resultPoint.getY());
			disaster.latitude = resultPoint.getY();
			disaster.longitude = resultPoint.getX();
		}
	}
}
