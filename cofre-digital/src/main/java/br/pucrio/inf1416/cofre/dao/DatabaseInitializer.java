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
					"INSERT OR IGNORE INTO Mensagens (mid, texto) VALUES (1003, 'Acesso do usuário registrado.');");
			statement.execute(
					"INSERT OR IGNORE INTO Mensagens (mid, texto) VALUES (1005, 'Primeira execução detectada.');");
			statement.execute(
					"INSERT OR IGNORE INTO Mensagens (mid, texto) VALUES (1006, 'Execução normal detectada.');");

			statement.execute(
					"INSERT OR IGNORE INTO Mensagens (mid, texto) VALUES (2001, 'Autenticação etapa 1 iniciada.');");
			statement.execute(
					"INSERT OR IGNORE INTO Mensagens (mid, texto) VALUES (2002, 'Autenticação etapa 1 encerrada.');");
			statement.execute(
					"INSERT OR IGNORE INTO Mensagens (mid, texto) VALUES (2003, 'Login identificado com sucesso.');");
			statement.execute(
					"INSERT OR IGNORE INTO Mensagens (mid, texto) VALUES (2004, 'Usuário bloqueado temporariamente.');");
			statement.execute(
					"INSERT OR IGNORE INTO Mensagens (mid, texto) VALUES (2005, 'Login inválido ou não identificado.');");

			statement.execute(
					"INSERT OR IGNORE INTO Mensagens (mid, texto) VALUES (3001, 'Autenticação etapa 2 iniciada.');");
			statement.execute(
					"INSERT OR IGNORE INTO Mensagens (mid, texto) VALUES (3002, 'Autenticação etapa 2 encerrada.');");
			statement.execute(
					"INSERT OR IGNORE INTO Mensagens (mid, texto) VALUES (3003, 'Senha pessoal verificada positivamente.');");
			statement.execute(
					"INSERT OR IGNORE INTO Mensagens (mid, texto) VALUES (3004, 'Primeiro erro de senha pessoal.');");
			statement.execute(
					"INSERT OR IGNORE INTO Mensagens (mid, texto) VALUES (3005, 'Segundo erro de senha pessoal.');");
			statement.execute(
					"INSERT OR IGNORE INTO Mensagens (mid, texto) VALUES (3006, 'Terceiro erro de senha pessoal.');");
			statement.execute(
					"INSERT OR IGNORE INTO Mensagens (mid, texto) VALUES (3007, 'Usuário bloqueado após erros de senha.');");

			statement.execute(
					"INSERT OR IGNORE INTO Mensagens (mid, texto) VALUES (4001, 'Autenticação etapa 3 iniciada.');");
			statement.execute(
					"INSERT OR IGNORE INTO Mensagens (mid, texto) VALUES (4002, 'Autenticação etapa 3 encerrada.');");
			statement.execute(
					"INSERT OR IGNORE INTO Mensagens (mid, texto) VALUES (4003, 'Token TOTP verificado positivamente.');");
			statement.execute(
					"INSERT OR IGNORE INTO Mensagens (mid, texto) VALUES (4004, 'Primeiro erro de token TOTP.');");
			statement.execute(
					"INSERT OR IGNORE INTO Mensagens (mid, texto) VALUES (4005, 'Segundo erro de token TOTP.');");
			statement.execute(
					"INSERT OR IGNORE INTO Mensagens (mid, texto) VALUES (4006, 'Terceiro erro de token TOTP.');");
			statement.execute(
					"INSERT OR IGNORE INTO Mensagens (mid, texto) VALUES (4007, 'Usuário bloqueado após erros de token TOTP.');");

			statement.execute(
					"INSERT OR IGNORE INTO Mensagens (mid, texto) VALUES (6003, 'Erro na senha informada no cadastro.');");
			statement.execute(
					"INSERT OR IGNORE INTO Mensagens (mid, texto) VALUES (6004, 'Erro no arquivo de certificado digital.');");
			statement.execute(
					"INSERT OR IGNORE INTO Mensagens (mid, texto) VALUES (6005, 'Erro no arquivo de chave privada.');");
			statement.execute(
					"INSERT OR IGNORE INTO Mensagens (mid, texto) VALUES (6006, 'Erro na frase secreta da chave privada.');");
			statement.execute(
					"INSERT OR IGNORE INTO Mensagens (mid, texto) VALUES (6007, 'Chave privada incompatível com certificado.');");
			statement.execute(
					"INSERT OR IGNORE INTO Mensagens (mid, texto) VALUES (6008, 'Certificado confirmado pelo usuário.');");
			statement.execute(
					"INSERT OR IGNORE INTO Mensagens (mid, texto) VALUES (6009, 'Cadastro cancelado pelo usuário.');");

			statement.execute(
					"INSERT OR IGNORE INTO Mensagens (mid, texto) VALUES (7001, 'Consulta da pasta secreta iniciada.');");
			statement.execute(
					"INSERT OR IGNORE INTO Mensagens (mid, texto) VALUES (7002, 'Consulta da pasta secreta concluída.');");
			statement.execute(
					"INSERT OR IGNORE INTO Mensagens (mid, texto) VALUES (7010, 'Arquivo secreto selecionado.');");
			statement.execute(
					"INSERT OR IGNORE INTO Mensagens (mid, texto) VALUES (7011, 'Acesso negado ao arquivo secreto.');");
			statement.execute(
					"INSERT OR IGNORE INTO Mensagens (mid, texto) VALUES (7013, 'Arquivo secreto decriptado com sucesso.');");
		}
	}
}
