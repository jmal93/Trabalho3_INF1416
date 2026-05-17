package br.pucrio.inf1416.cofre.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import br.pucrio.inf1416.cofre.model.Message;

public class MessageDAO {
	private Connection connection;

	public MessageDAO(Connection connection) {
		this.connection = connection;
	}

	public String findTextById(int mid) throws Exception {
		String sqlString = """
				SELECT * FROM Mensagens
				WHERE mid = ?
				""";

		try (PreparedStatement statement = connection.prepareStatement(sqlString)) {
			statement.setInt(1, mid);

			try (ResultSet resultSet = statement.executeQuery()) {
				if (!resultSet.next()) {
					return "Mensagem não cadastrada para MID " + mid;
				}
				return resultSet.getString("texto");
			}
		}
	}

	public List<Message> findAll() throws Exception {
		String sqlString = """
				SELECT * FROM Mensagens
				ORDER BY mid
				""";

		List<Message> resultMessages = new ArrayList<Message>();

		try (PreparedStatement statement = connection.prepareStatement(sqlString);
				ResultSet resultSet = statement.executeQuery()) {

			while (resultSet.next()) {
				Message message = new Message();

				message.setMid(resultSet.getInt("mid"));
				message.setTexto(resultSet.getString("texto"));

				resultMessages.add(message);
			}

		}

		return resultMessages;
	}

	public void insertIfNotExists(int mid, String text) throws Exception {
		String sqlString = """
				INSERT OR IGNORE INTO Mensagens
				VALUES (?, ?)
				""";

		try (PreparedStatement statement = connection.prepareStatement(sqlString)) {
			statement.setInt(1, mid);
			statement.setString(2, text);

			statement.executeUpdate();
		}
	}
}
