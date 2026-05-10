package br.pucrio.inf1416.cofre.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
	private final Connection connection;

	public DatabaseInitializer(Connection connection) {
		this.connection = connection;
	}

	public void initialize() throws SQLException {
		createTables();
		insertDefaultData();
	}

	private void createTables() throws SQLException {
		try (Statement statement = connection.createStatement()) {
			statement.execute("""
					    CREATE TABLE IF NOT EXISTS Grupos (
					        gid INTEGER PRIMARY KEY,
					        nome TEXT NOT NULL UNIQUE
					    );
					""");

			statement.execute("""
					    CREATE TABLE IF NOT EXISTS Usuarios (
					        uid INTEGER PRIMARY KEY AUTOINCREMENT,
					        login TEXT NOT NULL UNIQUE,
					        nome TEXT NOT NULL,
					        gid INTEGER NOT NULL,
					        senha_hash TEXT NOT NULL,
					        totp_secret_enc BLOB NOT NULL,
					        bloqueado_ate DATETIME,
					        total_acessos INTEGER DEFAULT 0,
					        total_consultas INTEGER DEFAULT 0,
					        FOREIGN KEY (gid) REFERENCES Grupos(gid)
					    );
					""");

			statement.execute("""
					    CREATE TABLE IF NOT EXISTS Chaveiro (
					        kid INTEGER PRIMARY KEY AUTOINCREMENT,
					        uid INTEGER NOT NULL UNIQUE,
					        certificado_pem TEXT NOT NULL,
					        chave_privada_enc BLOB NOT NULL,
					        FOREIGN KEY (uid) REFERENCES Usuarios(uid)
					    );
					""");

			statement.execute("""
					    CREATE TABLE IF NOT EXISTS Mensagens (
					        mid INTEGER PRIMARY KEY,
					        texto TEXT NOT NULL
					    );
					""");

			statement.execute("""
					    CREATE TABLE IF NOT EXISTS Registros (
					        rid INTEGER PRIMARY KEY AUTOINCREMENT,
					        data_hora DATETIME NOT NULL,
					        mid INTEGER NOT NULL,
					        uid INTEGER,
					        arquivo TEXT,
					        FOREIGN KEY (mid) REFERENCES Mensagens(mid),
					        FOREIGN KEY (uid) REFERENCES Usuarios(uid)
					    );
					""");
		}
	}

	private void insertDefaultData() throws SQLException {
		try (Statement statement = connection.createStatement()) {
			statement.execute("INSERT OR IGNORE INTO Grupos (gid, nome) VALUES (1, 'Administrador');");
			statement.execute("INSERT OR IGNORE INTO Grupos (gid, nome) VALUES (2, 'Usuario');");

			statement.execute("INSERT OR IGNORE INTO Mensagens (mid, texto) VALUES (1001, 'Sistema iniciado.');");
			statement.execute("INSERT OR IGNORE INTO Mensagens (mid, texto) VALUES (1002, 'Sistema encerrado.');");
			statement.execute(
					"INSERT OR IGNORE INTO Mensagens (mid, texto) VALUES (1005, 'Partida do sistema iniciada para cadastro do administrador.');");
			statement.execute(
					"INSERT OR IGNORE INTO Mensagens (mid, texto) VALUES (1006, 'Partida do sistema iniciada para operação normal pelos usuários.');");
		}
	}
}
