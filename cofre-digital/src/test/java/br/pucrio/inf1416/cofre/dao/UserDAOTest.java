package br.pucrio.inf1416.cofre.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pucrio.inf1416.cofre.model.User;

class UserDAOTest {
	private Connection connection;
	private UserDAO userDAO;

	@BeforeEach
	void setUp() throws Exception {
		connection = DriverManager.getConnection("jdbc:sqlite::memory:");

		try (Statement statement = connection.createStatement()) {
			statement.execute("""
					    CREATE TABLE IF NOT EXISTS Usuarios (
					        uid INTEGER PRIMARY KEY AUTOINCREMENT,
					        login TEXT NOT NULL UNIQUE,
					        nome TEXT NOT NULL,
					        gid INTEGER NOT NULL,
					        senha_hash TEXT NOT NULL,
					        totp_secret_enc BLOB NOT NULL,
					        bloqueado_ate DATETIME,
					        total_acessos INTEGER DEFAULT 0,
					        total_consultas INTEGER DEFAULT 0,
					        FOREIGN KEY (gid) REFERENCES Grupos(gid)
					    );
					""");
			statement.execute("""
					    CREATE TABLE IF NOT EXISTS Grupos (
					    	gid INTEGER PRIMARY KEY,
					    	nome TEXT NOT NULL UNIQUE
					    );
					""");
		}

		userDAO = new UserDAO(connection);
	}

	@Test
	void shouldReturnFalseWhenThereAreNoUsers() throws Exception {
		assertFalse(userDAO.hasAnyUser());
	}

	@Test
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

	@Test
	void shouldFindUserIfExists() throws Exception {
		try (Statement statement = connection.createStatement()) {
			statement.execute("""
					    INSERT INTO Usuarios
					    (login, nome, gid, senha_hash, totp_secret_enc)
					    VALUES
					    ('teste@teste.com.br', 'teste', 1, 'hash', X'001122');
					""");
			statement.execute("""
					    INSERT INTO Grupos
					    (gid, nome)
					    VALUES
					    (1, 'teste');
					""");
		}
		User user = userDAO.findByLogin("teste@teste.com.br");
		assertEquals(user.getNome(), "teste");
	}

	@Test
	void updateBlockedUntil() throws Exception {
		try (Statement statement = connection.createStatement()) {
			statement.execute("""
					    INSERT INTO Usuarios
					    (login, nome, uid, gid, senha_hash, totp_secret_enc, bloqueado_ate)
					    VALUES
					    ('teste@teste.com.br', 'teste', 1, 1, 'hash', X'001122', '2007-12-03T10:15:30');
					""");

			LocalDateTime localDateTime = LocalDateTime.of(2026, 2, 10, 13, 12, 5);

			userDAO.updateBlockedUntil(1, localDateTime);

			try (ResultSet resultSet = statement.executeQuery("SELECT bloqueado_ate FROM Usuarios WHERE uid = 1")) {
				assertEquals(localDateTime.toString(), resultSet.getString("bloqueado_ate"));
			}
		}
	}

	@Test
	void shouldIncrementAcessCount() throws Exception {
		try (Statement statement = connection.createStatement()) {
			statement.execute("""
					    INSERT INTO Usuarios
					    (login, nome, gid, uid, senha_hash, totp_secret_enc, total_acessos)
					    VALUES
					    ('teste@teste.com.br', 'teste', 1, 1, 'hash', X'001122', 0);
					""");

			userDAO.incrementAccessCount(1);

			try (ResultSet resultSet = statement.executeQuery("SELECT total_acessos FROM Usuarios WHERE uid = 1")) {
				assertEquals(1, resultSet.getInt("total_acessos"));
			}
		}
	}
}
