package br.pucrio.inf1416.cofre.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import br.pucrio.inf1416.cofre.model.User;

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

	public User findByLogin(String login) throws Exception {
		String sqlString = """
				SELECT
					u.uid,
					u.login,
					u.nome,
					u.gid,
					g.nome as grupo_nome,
					u.senha_hash,
					u.totp_secret_enc,
					u.bloqueado_ate,
					u.total_acessos,
					u.total_consultas
				FROM Usuarios u
				JOIN Grupos g ON g.gid = u.gid
				WHERE u.login = ?
				""";

		try (PreparedStatement statement = connection.prepareStatement(sqlString)) {
			statement.setString(1, login);

			try (ResultSet resultSet = statement.executeQuery()) {
				if (!resultSet.next()) {
					return null;
				}

				User user = new User();

				user.setUid(resultSet.getInt("uid"));
				user.setLogin(resultSet.getString("login"));
				user.setNome(resultSet.getString("nome"));
				user.setGid(resultSet.getInt("gid"));
				user.setGroupName(resultSet.getString("grupo_nome"));
				user.setPasswordHash(resultSet.getString("senha_hash"));
				user.setEncryptedTOTPSecret(resultSet.getBytes("totp_secret_enc"));

				String blockedUntil = resultSet.getString("bloqueado_ate");
				if (blockedUntil != null && !blockedUntil.isBlank()) {
					user.setBlockedUntil(LocalDateTime.parse(blockedUntil));
				}

				user.setTotalAccesses(resultSet.getInt("total_acessos"));
				user.setTotalQueries(resultSet.getInt("total_consultas"));

				return user;
			}
		}
	}

	public void updateBlockedUntil(int uid, LocalDateTime blockedUntil) throws Exception {
		String sqlsString = "UPDATE Usuarios SET bloqueado_ate = ? WHERE uid = ?";

		try (PreparedStatement statement = connection.prepareStatement(sqlsString)) {
			statement.setString(1, blockedUntil.toString());
			statement.setInt(2, uid);
			statement.execute();
		}
	}

	public void incrementAccessCount(int uid) throws Exception {
		String sqlString = "UPDATE Usuarios SET total_acessos = total_acessos + 1 WHERE uid = ?";

		try (PreparedStatement statement = connection.prepareStatement(sqlString)) {
			statement.setInt(1, uid);
			statement.execute();
		}
	}
}
