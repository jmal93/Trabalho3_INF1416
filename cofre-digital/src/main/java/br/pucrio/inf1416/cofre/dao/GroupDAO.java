package br.pucrio.inf1416.cofre.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import br.pucrio.inf1416.cofre.model.Group;

public class GroupDAO {
	private Connection connection;

	public GroupDAO(Connection connection) {
		this.connection = connection;
	}

	public Group findById(int gid) throws Exception {
		String sqlString = """
				SELECT * FROM Grupos
				WHERE gid = ?
				""";

		try (PreparedStatement statement = connection.prepareStatement(sqlString)) {
			statement.setInt(1, gid);

			try (ResultSet resultSet = statement.executeQuery()) {
				Group group = new Group();

				group.setGid(resultSet.getInt("gid"));
				group.setName(resultSet.getString("nome"));

				return group;
			}
		}
	}

	public Group findByName(String name) throws Exception {
		String sqlString = """
				SELECT * FROM Grupos
				WHERE nome = ?
				""";

		try (PreparedStatement statement = connection.prepareStatement(sqlString)) {
			statement.setString(1, name);

			try (ResultSet resultSet = statement.executeQuery()) {
				Group group = new Group();

				group.setGid(resultSet.getInt("gid"));
				group.setName(resultSet.getString("nome"));

				return group;
			}
		}
	}

	public List<Group> findAll() throws Exception {
		String sqlString = """
				SELECT * FROM Grupos
				ORDER BY gid
				""";

		List<Group> resultGroups = new ArrayList<Group>();

		try (PreparedStatement statement = connection.prepareStatement(sqlString);
				ResultSet resultSet = statement.executeQuery()) {

			while (resultSet.next()) {
				Group group = new Group();

				group.setGid(resultSet.getInt("gid"));
				group.setName(resultSet.getString("nome"));

				resultGroups.add(group);
			}

		}

		return resultGroups;
	}

	public int getGroupIdByName(String name) throws Exception {
		String sqlString = """
				SELECT * FROM Grupos
				WHERE nome = ?
				""";

		try (PreparedStatement statement = connection.prepareStatement(sqlString)) {
			statement.setString(1, name);

			try (ResultSet resultSet = statement.executeQuery()) {
				return resultSet.getInt("gid");
			}
		}
	}
}
