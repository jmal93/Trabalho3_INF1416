package br.pucrio.inf1416.cofre.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pucrio.inf1416.cofre.model.Message;

class MessageDAOTest {
	private Connection connection;
	private MessageDAO messageDAO;

	@BeforeEach
	void setUp() throws Exception {
		connection = DriverManager.getConnection("jdbc:sqlite::memory:");

		try (Statement statement = connection.createStatement()) {
			statement.execute("""
					    CREATE TABLE IF NOT EXISTS Mensagens (
					        mid INTEGER PRIMARY KEY,
					        texto TEXT NOT NULL
					    );
					""");
		}

		messageDAO = new MessageDAO(connection);
	}

	@Test
	void shouldFindTextById() throws Exception {
		try (Statement statement = connection.createStatement()) {
			statement.execute("""
					INSERT INTO Mensagens (mid, texto)
					VALUES (1, 'teste')
					""");

			String texto = messageDAO.findTextById(1);

			assertEquals("teste", texto);
		}
	}

	@Test
	void shouldGetAllMessages() throws Exception {
		try (Statement statement = connection.createStatement()) {
			statement.execute("""
					INSERT INTO Mensagens (mid, texto)
					VALUES (1, 'teste 1')
					""");
			statement.execute("""
					INSERT INTO Mensagens (mid, texto)
					VALUES (2, 'teste 2')
					""");

			java.util.List<Message> messages = messageDAO.findAll();

			for (int i = 0; i < messages.size(); i++) {
				Message message = messages.get(i);

				assertEquals(i + 1, message.getMid());
			}
		}
	}

	@Test
	void insertMessageThatDontExists() throws Exception {
		messageDAO.insertIfNotExists(1, "teste");

		try (Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery("SELECT * FROM Mensagens WHERE mid = 1")) {
			assertEquals(1, resultSet.getInt("mid"));
			assertEquals("teste", resultSet.getString("texto"));
		}
	}
}
