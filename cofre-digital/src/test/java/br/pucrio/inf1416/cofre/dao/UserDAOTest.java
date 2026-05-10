package br.pucrio.inf1416.cofre.dao;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserDAOTest {
	private Connection connection;
	private UserDAO userDAO;

	@BeforeEach
	void setUp() throws Exception {
		connection = DriverManager.getConnection("jdbc:sqlite::memory:");

		try (Statement statement = connection.createStatement()) {
			statement.execute("""
					    CREATE TABLE Usuarios (
					        uid INTEGER PRIMARY KEY AUTOINCREMENT,
					        login TEXT NOT NULL UNIQUE,
					        nome TEXT NOT NULL,
					        gid INTEGER NOT NULL,
					        senha_hash TEXT NOT NULL,
					        totp_secret_enc BLOB NOT NULL
					    );
					""");
		}

		userDAO = new UserDAO(connection);
	}

	@Test
	void shouldReturnFalseWhenThereAreNoUsers() throws Exception {
		assertFalse(userDAO.hasAnyUser());
	}

	void shouldReturnTrueWhenThereIsAtLeastOneUser() throws Exception {
		try (Statement stmt = connection.createStatement()) {
			stmt.execute("""
					    INSERT INTO Usuarios
					    (login, nome, gid, senha_hash, totp_secret_enc)
					    VALUES
					    ('admin@inf1416.puc-rio.br', 'Admin', 1, 'hash', X'001122');
					""");
		}

		assertTrue(userDAO.hasAnyUser());
	}

}
