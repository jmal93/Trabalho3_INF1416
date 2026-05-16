package br.pucrio.inf1416.cofre.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pucrio.inf1416.cofre.model.KeyPairRecord;

class KeyringDAOTest {
	private Connection connection;
	private KeyringDAO keyringDAO;

	@BeforeEach
	void setUp() throws Exception {
		connection = DriverManager.getConnection("jdbc:sqlite::memory:");

		try (Statement statement = connection.createStatement()) {
			statement.execute("""
					    CREATE TABLE IF NOT EXISTS Chaveiro (
					        kid INTEGER PRIMARY KEY AUTOINCREMENT,
					        uid INTEGER NOT NULL UNIQUE,
					        certificado_pem TEXT NOT NULL,
					        chave_privada_enc BLOB NOT NULL,
					        FOREIGN KEY (uid) REFERENCES Usuarios(uid)
					    );
					""");
		}

		keyringDAO = new KeyringDAO(connection);
	}

	@Test
	void shouldInsertKeyring() throws Exception {
		KeyPairRecord keyPairRecord = new KeyPairRecord();
		keyPairRecord.setUid(1);
		keyPairRecord.setCertificatePem("certificado");
		byte[] bytes = { 1 };
		keyPairRecord.setEncryptedPrivateKey(bytes);

		keyringDAO.insert(keyPairRecord);

		try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM Chaveiro");
				ResultSet resultSet = statement.executeQuery()) {
			assertEquals(1, resultSet.getInt("kid"));
		}
	}

	@Test
	void shouldReturnKeypairFoundByUserId() throws Exception {
		try (Statement statement = connection.createStatement()) {
			statement.execute("""
					INSERT INTO Chaveiro (kid, uid, certificado_pem, chave_privada_enc)
					VALUES (1, 1, 'certificado', X'000102')
					""");

			KeyPairRecord keyPairRecord = keyringDAO.findUserById(1);

			assertEquals(1, keyPairRecord.getKid());
			assertEquals(1, keyPairRecord.getUid());
			assertEquals("certificado", keyPairRecord.getCertificatePem());
			byte[] bytes = { 0, 1, 2 };
			assertTrue(Arrays.equals(bytes, keyPairRecord.getEncryptedPrivateKey()));
		}

	}

	@Test
	void shouldReturnKeypairFoundById() throws Exception {
		try (Statement statement = connection.createStatement()) {
			statement.execute("""
					INSERT INTO Chaveiro (kid, uid, certificado_pem, chave_privada_enc)
					VALUES (1, 1, 'certificado', X'000102')
					""");

			KeyPairRecord keyPairRecord = keyringDAO.findById(1);

			assertEquals(1, keyPairRecord.getKid());
			assertEquals(1, keyPairRecord.getUid());
			assertEquals("certificado", keyPairRecord.getCertificatePem());
			byte[] bytes = { 0, 1, 2 };
			assertTrue(Arrays.equals(bytes, keyPairRecord.getEncryptedPrivateKey()));
		}

	}

	@Test
	void shouldUpdateKeyring() throws Exception {
		KeyPairRecord keyPairRecord = new KeyPairRecord();
		keyPairRecord.setKid(1);
		keyPairRecord.setUid(1);
		keyPairRecord.setCertificatePem("novo certificado");
		byte[] bytes = { 0, 1, 2 };
		keyPairRecord.setEncryptedPrivateKey(bytes);

		try (Statement statement = connection.createStatement()) {
			statement.execute("""
					INSERT INTO Chaveiro (kid, uid, certificado_pem, chave_privada_enc)
					VALUES (1, 1, 'certificado', X'000102')
					""");

			keyringDAO.update(keyPairRecord);

			try (PreparedStatement assertStatement = connection
					.prepareStatement("SELECT * FROM Chaveiro WHERE kid = 1");
					ResultSet resultSet = assertStatement.executeQuery()) {
				assertEquals(1, keyPairRecord.getKid());
				assertEquals(1, keyPairRecord.getUid());
				assertEquals("novo certificado", keyPairRecord.getCertificatePem());
				assertTrue(Arrays.equals(bytes, keyPairRecord.getEncryptedPrivateKey()));
			}
		}
	}
}
