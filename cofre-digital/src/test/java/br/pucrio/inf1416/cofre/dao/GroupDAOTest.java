package br.pucrio.inf1416.cofre.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pucrio.inf1416.cofre.model.Group;

class GroupDAOTest {
	private Connection connection;
	private GroupDAO groupDAO;

	@BeforeEach
	void setUp() throws Exception {
		connection = DriverManager.getConnection("jdbc:sqlite::memory:");

		try (Statement statement = connection.createStatement()) {
			statement.execute("""
					    CREATE TABLE IF NOT EXISTS Grupos (
					        gid INTEGER PRIMARY KEY,
					        nome TEXT NOT NULL UNIQUE
					    );
					""");
		}

		groupDAO = new GroupDAO(connection);
	}

	@Test
	void shouldGetGroupById() throws Exception {
		try (Statement statement = connection.createStatement()) {
			statement.execute("""
					INSERT INTO Grupos (gid, nome)
					VALUES (1, 'teste')
					""");

			Group group = groupDAO.findById(1);

			assertEquals(1, group.getGid());
			assertEquals("teste", group.getName());
		}
	}

	@Test
	void shouldGetGroupByName() throws Exception {
		try (Statement statement = connection.createStatement()) {
			statement.execute("""
					INSERT INTO Grupos (gid, nome)
					VALUES (1, 'teste')
					""");

			Group group = groupDAO.findByName("teste");

			assertEquals(1, group.getGid());
			assertEquals("teste", group.getName());
		}
	}

	@Test
	void shouldGetAllGroups() throws Exception {
		try (Statement statement = connection.createStatement()) {
			statement.execute("""
					INSERT INTO Grupos (gid, nome)
					VALUES (1, 'teste 1')
					""");
			statement.execute("""
					INSERT INTO Grupos (gid, nome)
					VALUES (2, 'teste 2')
					""");

			java.util.List<Group> groups = groupDAO.findAll();

			for (int i = 0; i < groups.size(); i++) {
				Group group = groups.get(i);

				assertEquals(i + 1, group.getGid());
			}
		}
	}

	@Test
	void shouldGetGroupIdByName() throws Exception {
		try (Statement statement = connection.createStatement()) {
			statement.execute("""
					INSERT INTO Grupos (gid, nome)
					VALUES (1, 'teste 1')
					""");

			int id = groupDAO.getGroupIdByName("teste 1");

			assertEquals(1, id);
		}
	}
}
