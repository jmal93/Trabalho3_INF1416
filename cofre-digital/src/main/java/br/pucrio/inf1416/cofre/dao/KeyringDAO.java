package br.pucrio.inf1416.cofre.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import br.pucrio.inf1416.cofre.model.KeyPairRecord;

public class KeyringDAO {
	private Connection connection;

	public KeyringDAO(Connection connection) {
		this.connection = connection;
	}

	public int insert(KeyPairRecord keyPairRecord) throws Exception {
		String sqlString = """
				INSERT INTO Chaveiro (uid, certificado_pem, chave_privada_enc)
				VALUES (?, ?, ?)
				""";

		try (PreparedStatement statement = connection.prepareStatement(sqlString)) {
			statement.setInt(1, keyPairRecord.getUid());
			statement.setString(2, keyPairRecord.getCertificatePem());
			statement.setBytes(3, keyPairRecord.getEncryptedPrivateKey());

			return statement.executeUpdate();
		}
	}

	public KeyPairRecord findUserById(int uid) throws Exception {
		String sqlString = """
				SELECT * FROM Chaveiro
				WHERE uid = ?
				""";

		try (PreparedStatement statement = connection.prepareStatement(sqlString)) {
			statement.setInt(1, uid);

			try (ResultSet resultSet = statement.executeQuery()) {
				KeyPairRecord keyPairRecord = new KeyPairRecord();

				keyPairRecord.setKid(resultSet.getInt("kid"));
				keyPairRecord.setUid(resultSet.getInt("uid"));
				keyPairRecord.setCertificatePem(resultSet.getString("certificado_pem"));
				keyPairRecord.setEncryptedPrivateKey(resultSet.getBytes("chave_privada_enc"));

				return keyPairRecord;
			}
		}
	}

	public KeyPairRecord findById(int kid) throws Exception {
		String sqlString = """
				SELECT * FROM Chaveiro
				WHERE kid = ?
				""";

		try (PreparedStatement statement = connection.prepareStatement(sqlString)) {
			statement.setInt(1, kid);

			try (ResultSet resultSet = statement.executeQuery()) {
				KeyPairRecord keyPairRecord = new KeyPairRecord();

				keyPairRecord.setKid(resultSet.getInt("kid"));
				keyPairRecord.setUid(resultSet.getInt("uid"));
				keyPairRecord.setCertificatePem(resultSet.getString("certificado_pem"));
				keyPairRecord.setEncryptedPrivateKey(resultSet.getBytes("chave_privada_enc"));

				return keyPairRecord;
			}
		}
	}

	public void update(KeyPairRecord keyPairRecord) throws Exception {
		String sqlString = """
				UPDATE Chaveiro
				SET
					uid = ?,
					certificado_pem = ?,
					chave_privada_enc = ?
				WHERE kid = ?
				""";

		try (PreparedStatement statement = connection.prepareStatement(sqlString)) {
			statement.setInt(1, keyPairRecord.getUid());
			statement.setString(2, keyPairRecord.getCertificatePem());
			statement.setBytes(3, keyPairRecord.getEncryptedPrivateKey());
			statement.setInt(4, keyPairRecord.getKid());

			statement.execute();
		}
	}
}
