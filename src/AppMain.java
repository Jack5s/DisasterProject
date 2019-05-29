import com.converter.GeoJsonConverter;
import com.webscraper.jsoup.EMSWebScraper;
import com.webscraper.jsoup.GDACSWebAllRecordScraper;
import com.webscraper.jsoup.GDELTScraper;
import com.webscraper.jsoup.ReliefWebScraper;

public class AppMain {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		scraper("gdelt");
	}

	private static void scraper(String webStr) {
		System.out.println(webStr);
		try {
			String data = "";

			switch (webStr) {
//			case "gdacs":
//				GDACSWebAllRecordScraper gdacsWebAllRecordScraper = new GDACSWebAllRecordScraper();
//				// data = GeoJsonConverter.toGeoJson(gdacsWebScraper.earthquakeArray);
//				break;
//			case "ems":
//				EMSWebScraper emsWebScraper = new EMSWebScraper();
//				// data = GeoJsonConverter.toGeoJson(emsWebScraper.disastersArray);
//				break;
//			case "relief":
//				ReliefWebScraper reliefWebScraper = new ReliefWebScraper();
////				data = GeoJsonConverter.toGeoJson(reliefWebScraper.disastersArray);
//				break;
			case "gdelt":
				GDELTScraper gdeltScraper = new GDELTScraper();
				// data = GeoJsonConverter.toGeoJson(gdeltScraper.disasterList);
				break;
			default:
				break;
			}
			System.out.println("!!");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
}
