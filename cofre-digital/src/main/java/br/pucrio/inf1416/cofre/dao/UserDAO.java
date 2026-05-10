package br.pucrio.inf1416.cofre.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
	private final Connection connection;

	public UserDAO(Connection connection) {
		this.connection = connection;
	}

	public boolean hasAnyUser() throws SQLException {
		String sqlString = "SELECT COUNT(*) FROM Usuarios";

		try (PreparedStatement statement = connection.prepareStatement(sqlString);
				ResultSet resultSet = statement.executeQuery()) {
			return resultSet.next() && resultSet.getInt(1) > 0;
		}
	}
}
