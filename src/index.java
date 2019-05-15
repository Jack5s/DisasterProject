
import java.io.IOException;
import java.io.PrintWriter;

import com.converter.GeoJsonConverter;
import com.webscraper.jsoup.EMSWebScraper;
import com.webscraper.jsoup.GDACSWebAllRecordScraper;
import com.webscraper.jsoup.GDACSWebScraper;
import com.webscraper.jsoup.GDELTScraper;
import com.webscraper.jsoup.ReliefWebScraper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class index
 */
@WebServlet("/index")
public class index extends HttpServlet {
	private static final long serialVersionUID = 1L;
	// private int times;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public index() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter printWriter = response.getWriter();
		String webStr = request.getParameter("web");
		System.out.println(webStr);
		try {
			String data = "";

			switch (webStr) {
			case "gdacs":
				GDACSWebAllRecordScraper gdacsWebAllRecordScraper = new GDACSWebAllRecordScraper();
				// data = GeoJsonConverter.toGeoJson(gdacsWebScraper.earthquakeArray);
				break;
			case "ems":
				EMSWebScraper emsWebScraper = new EMSWebScraper();
				// data = GeoJsonConverter.toGeoJson(emsWebScraper.disastersArray);
				break;
			case "relief":
				ReliefWebScraper reliefWebScraper = new ReliefWebScraper();
				data = GeoJsonConverter.toGeoJson(reliefWebScraper.disastersArray);
				break;
			case "gdelt":
				GDELTScraper gdeltScraper = new GDELTScraper();
				// data = GeoJsonConverter.toGeoJson(gdeltScraper.disasterList);
				break;
			default:
				break;
			}
			System.out.println("!!");
			printWriter.write(data);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		// times = 0;
		// Timer timer = new Timer();
		// System.out.println("start\r\n");
		// try {
		// timer.schedule(new TimerTask() {
		// @Override
		// public void run() {
		// try {
		// times++;
		// String str = times + "$$$";
		// Gson gson = new Gson();
		// GDACSWebpage gdacs=new GDACSWebpage();
		// str = gson.toJson(gdacs.earthquakeArray);
		// str+="\r\n\r\n";
		// BufferedWriter bufferedWriter = new BufferedWriter(
		// new FileWriter("C:/Users/s1061395/Desktop/test/t.txt",true));
		// bufferedWriter.write(times+": "+str);
		// bufferedWriter.flush();
		// bufferedWriter.close();
		//
		// System.out.println(str);
		// if (times > 1) {
		// timer.cancel();
		// System.out.println("end");
		// }
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// }
		// }, 0, 20000);

		//
		// } catch (Exception e) {
		// e.printStackTrace();
		// // timer.cancel();
		// } finally {
		//
		// }

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

}
