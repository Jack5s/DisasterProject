package com.converter;

import java.lang.reflect.Field;
import java.util.ArrayList;

import com.disaster.Disaster;

public class GeoJsonConverter {

	public static String toGeoJson(Disaster disaster) throws Exception {
		if (disaster == null) {
			return "";
		}

		String geoGson = "{\"type\": \"Feature\",";
		geoGson += "\"geometry\": {\"type\":\"Point\",\"coordinates\": [" + disaster.longitude + "," + disaster.latitude
				+ "]},";
		geoGson += "\"properties\": {";

		Field[] fieldArray = Disaster.class.getFields();
		for (int i = 0; i < fieldArray.length; i++) {
//			Class<?> fieldType = fieldArray[i].getType();
			Field field = fieldArray[i];
			String fieldName = field.getName();
			Object value = field.get(disaster);
			// System.out.println(fieldName);
			geoGson += "\"" + fieldName + "\":\"" + value + "\",";
		}
		geoGson = geoGson.substring(0, geoGson.length() - 1);
		geoGson += "}}";
		return geoGson;
	}

	public static String toGeoJson(Disaster[] disasterArray) throws Exception {
		// String geoGson = "{\"type\":
		// \"FeatureCollection\",\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:4326\"}},\"features\":
		// [";
		String geoGson = "{\"type\": \"FeatureCollection\",\"features\": [";
		for (int i = 0; i < disasterArray.length; i++) {
			Disaster diasater = disasterArray[i];
			String disasterGeoJson = toGeoJson(diasater);
			if (disasterGeoJson == "") {
				continue;
			}
			geoGson += disasterGeoJson + ",";
		}
		geoGson = geoGson.substring(0, geoGson.length() - 1);
		geoGson += "]}";
		// System.out.println(geoGson);
		return geoGson;
	}
	
	public static String toGeoJson(ArrayList<Disaster> disasterList) throws Exception {
		// String geoGson = "{\"type\":
		// \"FeatureCollection\",\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:4326\"}},\"features\":
		// [";
		String geoGson = "{\"type\": \"FeatureCollection\",\"features\": [";
		for (Disaster disaster : disasterList) {
			String disasterGeoJson = toGeoJson(disaster);
			if (disasterGeoJson == "") {
				continue;
			}
			geoGson += disasterGeoJson + ",";
		}
		geoGson = geoGson.substring(0, geoGson.length() - 1);
		geoGson += "]}";
		// System.out.println(geoGson);
		return geoGson;
	}
}
