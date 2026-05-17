package br.pucrio.inf1416.cofre.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

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

	public List<LogRecord> findAllChronological() throws Exception {
		String sql = """
				SELECT rid, data_hora, mid, uid, arquivo
				FROM Registros
				ORDER BY data_hora ASC
				""";

		List<LogRecord> records = new java.util.ArrayList<>();

		try (PreparedStatement statement = connection.prepareStatement(sql);
				ResultSet resultSet = statement.executeQuery()) {

			while (resultSet.next()) {
				LogRecord record = new LogRecord();

				record.setRid(resultSet.getInt("rid"));
				record.setDateTime(java.time.LocalDateTime.parse(resultSet.getString("data_hora")));
				record.setMid(resultSet.getInt("mid"));

				int uid = resultSet.getInt("uid");
				if (resultSet.wasNull()) {
					record.setUid(null);
				} else {
					record.setUid(uid);
				}

				record.setFileName(resultSet.getString("arquivo"));

				records.add(record);
			}
		}

		return records;
	}
}
