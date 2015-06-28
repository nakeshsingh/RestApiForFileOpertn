package com.nakesh.mmx.connection;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
 
public class DbManager {
 
	public static Connection getConnection() {
 
		System.out.println("-------- PostgreSQL JDBC Connection  ---------");
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			return null;
		}
		Connection connection = null;
		try {
			connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/mmx", "kp_login_role", "kp");
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		if (connection != null) {
			System.out.println("......DataBase started successfuly......");
		} else {
			System.out.println("....Failed to make connection!....");
		}
		return connection;
	}
	
	public static void closeResources(ResultSet resultSet, PreparedStatement statement, Connection connection) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            resultSet = null;
        }
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            statement = null;
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            connection = null;
        }
    }
	
	public static void main(String[] args) {
		Connection conn = DbManager.getConnection();
	}
}
