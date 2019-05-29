package com.connection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;

import com.disaster.Disaster;

public class SQLConnection {
	public static String url;
	public static String userName;
	public static String password;
	private static Connection connection;
	static {
		File file = new File("C:/Users/s1061395/Project/Java/t.txt");
		try {
			BufferedReader bufferedReaderr = new BufferedReader(new FileReader(file));
			url = bufferedReaderr.readLine();
			userName = bufferedReaderr.readLine();
			password = bufferedReaderr.readLine();
			bufferedReaderr.close();
			Class.forName("org.postgresql.Driver");
			connection = DriverManager.getConnection(url, userName, password);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public SQLConnection() throws Exception {

	}

	public static void insert(Disaster disaster) throws Exception {
		if (disaster == null) {
			return;
		}
		// check the whether the current disaster can be inserted into database
		Boolean checkresult;
		// checkresult = checkInsertFromTimeAndPosition(disaster);
		// tableName=DisasterType.getTableNameFromDisasterType(disaster.disasterType);
		String tableName = "Disaster";
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		calendar = Calendar.getInstance();
		calendar.set(disaster.startYear, disaster.startMonth - 1, disaster.startDay, disaster.startHour,
				disaster.startMinute, 0);
		Timestamp timestamp = new Timestamp(calendar.getTimeInMillis());
		String timeStr = timestamp.toString();

		String insertStr = "";
		insertStr = "insert into disaster_event.\"" + tableName + "\" VALUES ('" + disaster.id + "',"
				+ disaster.longitude + "," + disaster.latitude + ",'" + disaster.disasterType + "','" + disaster.url
				+ "','" + timeStr + "');";
		Statement statement = connection.createStatement();
		statement.executeUpdate(insertStr);
		statement.close();
	}

	public static void insertFileName(String fileName) throws SQLException {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String insertStr = "insert into disaster_event.\"FileName\" VALUES ('" + fileName + "','" + timestamp.toString()
				+ "');";
		Statement statement = connection.createStatement();
		statement.executeUpdate(insertStr);
	}

	public static void insertTextContent(String id, String content) throws SQLException {
		PreparedStatement preparedStatement = connection
				.prepareStatement("insert into disaster_event.\"Record\" values (?,?);");
		preparedStatement.setString(1, id);
		preparedStatement.setString(2, content);
		preparedStatement.executeUpdate();
	}

	public static boolean checkFileList(String fileName) throws SQLException {
		String select = "select * from disaster_event.\"FileName\" where \"fileName\"='" + fileName + "';";
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery(select);
		boolean checkHasRecord = rs.next();
		if (checkHasRecord == true) {
			return false;
		} else {
			return true;
		}
	}

	// check the whether the current disaster can be inserted into database to avoid
	// repetitive record
	// public static boolean checkInsertFromTimeAndPosition(Disaster disaster)
	// throws SQLException {
	// Statement stmt = null;
	// String query = "select * from disaster_event.\"Disaster\" where
	// abs(\"longitude\" - " + disaster.longitude
	// + ")<= 1 and abs(\"latitude\" - " + disaster.latitude + ")<= 0.01 and
	// \"startDay\" = "
	// + disaster.startDay + " and \"startMonth\" = " + disaster.startMonth + " and
	// \"startYear\" = "
	// + disaster.startYear + " and \"disasterType\" = '" +
	// disaster.disasterType.toString() + "'";
	// stmt = connection.createStatement();
	// ResultSet rs = stmt.executeQuery(query);
	//
	// boolean checkHasRecord = rs.next();
	// if (checkHasRecord == true) {
	// return false;
	// } else {
	// return true;
	// }
	// }
	//
	public static boolean checkCanInsertFromID(String id) throws SQLException {
		Statement stmt = null;
		String query = "select id from disaster_event.\"Disaster\" where \"id\" = '" + id + "'";
		stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		boolean checkHasRecord = rs.next();
		if (checkHasRecord == true) {
			return false;
		} else {
			return true;
		}
	}
	
	public static boolean checkCanInsertFromFileName(String fileName) throws SQLException {
		Statement stmt = null;
		String query = "select id from disaster_event.\"FileName\" where \"fileName\" = '" + fileName + "'";
		stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		boolean checkHasRecord = rs.next();
		if (checkHasRecord == true) {
			return false;
		} else {
			return true;
		}
	}

	// public static void insertToFile(Disaster disaster, String fileName) throws
	// Exception {
	// BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileName,
	// true));
	// String dataLine = disaster.id + "," + disaster.longitude + "," +
	// disaster.latitude + "," + disaster.startYear
	// + "," + disaster.startMonth + "," + disaster.startDay + "," +
	// disaster.startHour + ","
	// + disaster.startMinute + "," + disaster.disasterType + "," + disaster.url;
	// bufferedWriter.write(dataLine + "\r\n");
	// bufferedWriter.close();
	// }
}