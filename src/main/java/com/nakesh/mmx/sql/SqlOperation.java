package com.nakesh.mmx.sql;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nakesh.mmx.connection.DbManager;

public class SqlOperation {
	
	public static boolean saveToDataBase(File _inputFile, String _contentType, String _version, int _length) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(_inputFile);
			connection = DbManager.getConnection();
			preparedStatement = connection
					.prepareStatement("INSERT INTO tblStorage (fileName, fileSize, fileLength, contentType, file, fileVersion) VALUES (?, ?, ?, ?, ?, ?)");
			preparedStatement.setString(1, _inputFile.getName());
			preparedStatement.setString(2, String.valueOf(_inputFile.getTotalSpace()));
			preparedStatement.setString(3, String.valueOf(_length));
			preparedStatement.setString(4, _contentType);
			preparedStatement.setBinaryStream(5, fis, (int) _inputFile.length());
			preparedStatement.setString(6, _version);
			preparedStatement.executeUpdate();
			fis.close();
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		} finally {
			DbManager.closeResources(null, preparedStatement, connection);
		}
		return true;
	}
	
	public static List<Map<String, Object>> getDataFromDataBase() {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		Map<String, Object> dataMap = new HashMap<String, Object>();
		List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
		try {
			connection = DbManager.getConnection();
			preparedStatement = connection.prepareStatement("SELECT * FROM tblStorage WHERE fileName = ?");
			preparedStatement.setString(1, "images.jpeg");
			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				System.out.println("name : " + resultSet.getString("fileName"));
				System.out.println("type : " + resultSet.getString("contentType"));
				System.out.println("size : " + resultSet.getString("fileSize"));
				System.out.println("version : " + resultSet.getString("fileVersion"));
				System.out.println("uploadedDate : " + resultSet.getString("eventTime"));
			    dataMap.put("name", resultSet.getString("fileName"));
			    dataMap.put("type", resultSet.getString("contentType"));
			    dataMap.put("size", resultSet.getString("fileSize"));
			    dataMap.put("length", resultSet.getString("fileLength"));
			    dataMap.put("fileVersion", resultSet.getBytes("fileVersion"));
			    dataMap.put("fileData", resultSet.getBytes("file"));
			    dataList.add(dataMap);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbManager.closeResources(resultSet, preparedStatement, connection);
		}
		return dataList;
	}
	
	public static String getLatestVersion() {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		String version = null;
		try {
			String getVersion = "SELECT fileVersion FROM tblStorage order by id desc limit 1";
			connection = DbManager.getConnection();
			preparedStatement = connection.prepareStatement(getVersion);
			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				System.out.println("latestVersion : " + resultSet.getString("fileVersion"));
				version = resultSet.getString("fileVersion");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbManager.closeResources(resultSet, preparedStatement, connection);
		}
		return version;
	}
	
	public static List<Object> getfileFromDataBase(String _fileName, String _version) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		byte[] byteImg = null;
		List<Object> dataList = new ArrayList<Object>();
		try {
			connection = DbManager.getConnection();
			StringBuilder builder = new StringBuilder();
			builder.append("SELECT * FROM tblStorage WHERE fileName = ?");
			if(_version != null && !_version.isEmpty()) {
				builder.append(" AND fileVersion = ?");	
			}
			builder.append(" limit 1");
			preparedStatement = connection.prepareStatement(builder.toString());
			preparedStatement.setString(1, _fileName);
			if(_version != null && !_version.isEmpty()) {
				preparedStatement.setString(2, _version);
			}
			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				
				System.out.println("name : " + resultSet.getString("fileName"));
				System.out.println("type : " + resultSet.getString("contentType"));
				System.out.println("size : " + resultSet.getString("fileSize"));
				System.out.println("length : " + resultSet.getString("fileLength"));
				System.out.println("version : " + resultSet.getString("fileVersion"));
				System.out.println("uploadedDate : " + resultSet.getString("eventTime"));
				dataList.add(resultSet.getString("fileName"));
				dataList.add(resultSet.getString("contentType"));
				dataList.add(resultSet.getString("fileSize"));
				dataList.add(resultSet.getString("fileLength"));
				
			    byteImg = resultSet.getBytes("file");
			    dataList.add(byteImg);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbManager.closeResources(resultSet, preparedStatement, connection);
		}
		return dataList;
	}
	
	public static List<Object> getfileFromDataBase(String _version) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		byte[] byteImg = null;
		List<Object> dataList = new ArrayList<Object>();
		try {
			connection = DbManager.getConnection();
			preparedStatement = connection.prepareStatement("SELECT * FROM tblStorage WHERE fileVersion = ? limit 1");
			preparedStatement.setString(1, _version);
			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {

				System.out.println("name : " + resultSet.getString("fileName"));
				System.out.println("type : " + resultSet.getString("contentType"));
				System.out.println("size : " + resultSet.getString("fileSize"));
				System.out.println("length : " + resultSet.getString("fileLength"));
				System.out.println("version : " + resultSet.getString("fileVersion"));
				System.out.println("uploadedDate : " + resultSet.getString("eventTime"));
				dataList.add(resultSet.getString("fileName"));
				dataList.add(resultSet.getString("contentType"));
				dataList.add(resultSet.getString("fileSize"));
				dataList.add(resultSet.getString("fileLength"));

				byteImg = resultSet.getBytes("file");
				dataList.add(byteImg);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbManager.closeResources(resultSet, preparedStatement, connection);
		}
		return dataList;

	}
}
