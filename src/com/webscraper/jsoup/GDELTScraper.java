package com.webscraper.jsoup;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import com.connection.SQLConnection;
import com.disaster.Disaster;
import com.disaster.DisasterType;

import Common.Common;

public class GDELTScraper {

	private String url = "http://data.gdeltproject.org/gdeltv2/masterfilelist.txt";
	private String exportZIPDownloadPath = "C:/Data - Copy/ExportZIPFile";
	private String exportCSVDownloadPath = "C:/Data - Copy/ExportCSVFile";

	private String gkgZIPDownloadPath = "C:/Data - Copy/GKGZIPFile";
	private String gkgCSVDownloadPath = "C:/Data - Copy/GKGCSVFile";
	private String gkgTotalCSVPath = "C:/Data - Copy/GKG";

	private String mentionsZIPDownloadPath = "C:/Data - Copy/MentionsZIPFile";
	private String mentionsCSVDownloadPath = "C:/Data - Copy/MentionsCSVFile";

	// private String url = "data.gdeltproject.org/gdeltv2/masterfilelist.txt";

	public GDELTScraper() throws Exception {
		// fileToDatabase();
		scrpeFromWeb();
	}

	private void scrpeFromWeb() throws Exception {
		int maxCount = 1;

		Document doc;
		doc = Jsoup.connect(url).maxBodySize(0).timeout(0).get();
		StringReader stringReader = new StringReader(doc.wholeText());
		BufferedReader bufferedReader = new BufferedReader(stringReader);
		int i = 0;
		Stack<String> dataLineStack = new Stack<>();
		String dataLine = bufferedReader.readLine();
		while (dataLine != null) {
			dataLineStack.push(dataLine);
			dataLine = bufferedReader.readLine();
		}
		bufferedReader.close();
		System.out.println("Stack Finished");

		PrintWriter clearWriter = new PrintWriter(Common.gdeltFileName);
		clearWriter.close();

		dataLine = dataLineStack.pop();
		while (dataLine != null) {
			try {
				Disaster disaster = new Disaster();
				String[] dataArray = dataLine.split(" ");
				disaster.id = dataArray[0];
				String downloadUrl = dataArray[2];
				String fileName = downloadUrl.substring(downloadUrl.lastIndexOf('/') + 1, downloadUrl.lastIndexOf('.'));

				String timeStr = fileName.substring(0, 14);
				Disaster disasterTime = new Disaster();
				disasterTime.startYear = Integer.valueOf(timeStr.substring(0, 4));
				disasterTime.startMonth = Integer.valueOf(timeStr.substring(4, 6));
				disasterTime.startDay = Integer.valueOf(timeStr.substring(6, 8));
				disasterTime.startHour = Integer.valueOf(timeStr.substring(8, 10));
				disasterTime.startMinute = Integer.valueOf(timeStr.substring(10, 12));
				if (disasterTime.startYear >= Common.startYear && disasterTime.startYear <= Common.endYear
						&& disasterTime.startMonth >= Common.startMonth && disasterTime.startMonth <= Common.endMonth
						&& disasterTime.startDay >= Common.startDay && disasterTime.startDay <= Common.endDay) {
				} else {
					dataLine = dataLineStack.pop();
					continue;
				}

				String[] strArray = fileName.split("\\.");
				String fileType = strArray[1].toLowerCase();
				String downloadZIPFileName = "";
				String csvFileName = "";
				switch (fileType) {
				case "export":
					downloadZIPFileName = exportZIPDownloadPath + "/" + fileName + ".zip";
					csvFileName = exportCSVDownloadPath + "/" + fileName + ".csv";
					break;
				case "mentions":
					downloadZIPFileName = mentionsZIPDownloadPath + "/" + fileName + ".zip";
					csvFileName = mentionsCSVDownloadPath + "/" + fileName + ".csv";
					break;
				case "gkg":
					downloadZIPFileName = gkgZIPDownloadPath + "/" + fileName + ".zip";
					csvFileName = gkgCSVDownloadPath + "/" + fileName + ".csv";
					// boolean checkFileCanInsert = SQLConnection.checkFileList(fileName);
					boolean checkFileCanInsert = true;
					System.out.println(fileName + ": " + checkFileCanInsert);

					if (checkFileCanInsert == true) {
						boolean checkDownloadFile = downloadFile(downloadUrl, downloadZIPFileName);
						if (checkDownloadFile == false) {
							errorLog("Fail to download file" + downloadZIPFileName);
						}
						unzipFile(downloadZIPFileName, csvFileName);
						insertGKGFileToDatabase(fileName);

						File zipFile = new File(downloadZIPFileName);
						boolean checkZIPFile = zipFile.delete();
						if (checkZIPFile == false) {
							errorLog("Fail to delete file" + downloadZIPFileName);
						}

						File csvFile = new File(gkgCSVDownloadPath + "/" + fileName + ".csv");
						boolean checkcsvFile = csvFile.delete();
						if (checkcsvFile == false) {
							errorLog("Fail to delete file" + csvFileName);
						}
						i++;
					}
				default:
					break;
				}

				if (i >= maxCount) {
					// break;
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("ERROR" + e.getMessage());
			} finally {
				dataLine = dataLineStack.pop();
			}
		}
		System.out.println("Finish");
	}

	private boolean downloadFile(String downloadUrl, String downloadZIPFileName) throws IOException {
		try {
			InputStream in;
			in = new URL(downloadUrl).openStream();
			Files.copy(in, Paths.get(downloadZIPFileName), StandardCopyOption.REPLACE_EXISTING);
			return true;

		} catch (IOException e) {
			errorLog("Error Download: " + downloadUrl);
			return false;
		}
	}

	private void unzipFile(String downloadZIPFileName, String dataFileName) throws IOException {
		byte[] buffer = new byte[1024];
		ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(downloadZIPFileName));
		try {
			ZipEntry zipEntry = zipInputStream.getNextEntry();
			while (zipEntry != null) {
				File newFile = new File(dataFileName);
				FileOutputStream fileOutputStream = new FileOutputStream(newFile);
				int len = zipInputStream.read(buffer);
				while (len > 0) {
					fileOutputStream.write(buffer, 0, len);
					len = zipInputStream.read(buffer);
				}
				fileOutputStream.close();
				zipEntry = zipInputStream.getNextEntry();
			}
		} catch (IOException e) {
			errorLog("Error Unzip: " + downloadZIPFileName);
		} finally {
			zipInputStream.closeEntry();
			zipInputStream.close();
		}
	}

	private void insertGKGFileToDatabase(String gkgFileName) throws Exception {
		BufferedReader bufferedReader = new BufferedReader(
				new FileReader(gkgCSVDownloadPath + "/" + gkgFileName + ".csv"));
		String dataLine = bufferedReader.readLine();
		int lineNumer = 1;
		String totalFileName = gkgTotalCSVPath + "/" + gkgFileName + ".txt";
		PrintWriter clearWriter = new PrintWriter(totalFileName);
		clearWriter.close();

		while (dataLine != null) {
			try {
				String[] dataArray = dataLine.split("\t");
				String V1THEMES = dataArray[7].toLowerCase();
				int[] indexArray1 = getAllIndex(V1THEMES, "natural_disaster");

				String V2ENHANCEDTHEMES = dataArray[8].toLowerCase();
				int[] indexArray2 = getAllIndex(V2ENHANCEDTHEMES, "natural_disaster");

				String V2GCAM = dataArray[17].toLowerCase();
				int index3 = V2GCAM.indexOf("c18.156");

				if (indexArray1.length > 0 && indexArray2.length > 0 && index3 >= 0) {
					BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(totalFileName, true));
					bufferedWriter.write(dataLine + "\r\n");
					bufferedWriter.close();

					Disaster disaster = new Disaster();
					disaster.id = dataArray[0];
					String dateTimeStr = dataArray[1];
					disaster.startYear = Integer.valueOf(dateTimeStr.substring(0, 4));
					disaster.startMonth = Integer.valueOf(dateTimeStr.substring(4, 6));
					disaster.startDay = Integer.valueOf(dateTimeStr.substring(6, 8));
					disaster.startHour = Integer.valueOf(dateTimeStr.substring(8, 10));
					disaster.startMinute = Integer.valueOf(dateTimeStr.substring(10, 12));
					disaster.url = dataArray[4];

					String locationStr = dataArray[9];
					if (locationStr.length() > 0) {
						String[] locationArray = locationStr.split("#");
						String latitudeStr = locationArray[4];
						String longitudeStr = locationArray[5];
						if (latitudeStr.length() > 0 && longitudeStr.length() > 0) {
							disaster.latitude = Double.valueOf(latitudeStr);
							disaster.longitude = Double.valueOf(longitudeStr);
						} else {
							throw new Exception();
						}
					}
					String[] disasterTypeStrArray = getDisasterTypeString(V1THEMES, indexArray1, "natural_disaster");
					for (int i = 0; i < disasterTypeStrArray.length; i++) {
						String disasterTypeStr = disasterTypeStrArray[i];
						disaster.disasterType = DisasterType.getDisasterTypeFromString(disasterTypeStr);
						if (disaster.disasterType != DisasterType.SpecialEvent) {
							break;
						}
					}
					// SQLConnection.insertTextContent(disaster.id, dataLine);
					// SQLConnection.insert(disaster);
					if (disaster.disasterType == Common.disasterType) {
						SQLConnection.insertToFile(disaster, Common.gdeltFileName);
					}
				}
			} catch (Exception e) {
				errorLog(gkgFileName + ":Error in Line " + lineNumer);
				return;
			} finally {
				dataLine = bufferedReader.readLine();
				lineNumer++;
			}
		}
		bufferedReader.close();
	}

	private int[] getAllIndex(String str, String fitStr) {
		ArrayList<Integer> indexList = new ArrayList<>();
		int index = str.indexOf(fitStr);
		while (index >= 0) {
			indexList.add(Integer.valueOf(index));
			index = str.indexOf(fitStr, index + fitStr.length());
		}
		int[] indexArray = new int[indexList.size()];
		for (int i = 0; i < indexArray.length; i++) {
			indexArray[i] = indexList.get(i).intValue();
		}
		return indexArray;
	}

	private String[] getDisasterTypeString(String valueStr, int[] indexArray, String fitStr) {
		ArrayList<String> disasterTypeStrList = new ArrayList<>();
		String disasterTypeStr = "";
		for (int i = 0; i < indexArray.length; i++) {
			String str = valueStr.substring(indexArray[i] + fitStr.length(), valueStr.indexOf(';', indexArray[i]));
			if (str.length() > 0) {
				disasterTypeStr = str.substring(1, str.length());
				disasterTypeStrList.add(disasterTypeStr);
			}
		}
		String[] disasterTypeStrArray = new String[disasterTypeStrList.size()];
		disasterTypeStrList.toArray(disasterTypeStrArray);
		return disasterTypeStrArray;
	}

	private void errorLog(String msg) throws IOException {
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("C:/Data - Copy/ErrorLog.txt", true));
		bufferedWriter.write(msg + "\r\n");
		bufferedWriter.close();
	}
}
