package br.pucrio.inf1416.cofre.dao;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {

	private static final Path DATABASE_PATH = Path.of("src", "main", "resources", "database.db");

	public static Connection getConnection() throws Exception {
		Path parent = DATABASE_PATH.getParent();

		if (parent != null && !Files.exists(parent)) {
			Files.createDirectories(parent);
		}

		return DriverManager.getConnection("jdbc:sqlite:" + DATABASE_PATH.toString());
	}
}
