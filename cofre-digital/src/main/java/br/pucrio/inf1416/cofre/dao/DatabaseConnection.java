package br.pucrio.inf1416.cofre.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
	private static final String DATABASE_URL_STRING = "jdbc:sqlite:src/main/resources/database.db";

	public static Connection getConnection() throws SQLException {
		return DriverManager.getConnection(DATABASE_URL_STRING);
	}
}
