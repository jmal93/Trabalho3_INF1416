package br.pucrio.inf1416.cofre.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import br.pucrio.inf1416.cofre.model.LogRecord;

public class LogDAO {
	private final Connection connection;

	public LogDAO(Connection connection) {
		this.connection = connection;
	}

	public void insert(LogRecord logRecord) throws SQLException {
		String sql = """
				INSERT INTO Registros(data_hora, mid, uid, arquivo)
				VALUES (?, ? ,? ,?)
				""";

		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, logRecord.getDateTime().toString());
			statement.setInt(2, logRecord.getMid());

			if (logRecord.getUid() == null) {
				statement.setNull(3, java.sql.Types.INTEGER);
			} else {
				statement.setInt(3, logRecord.getUid());
			}

			if (logRecord.getFileName() == null || logRecord.getFileName().isBlank()) {
				statement.setNull(4, java.sql.Types.VARCHAR);
			} else {
				statement.setString(4, logRecord.getFileName());
			}

			statement.executeUpdate();
		}
	}
}
