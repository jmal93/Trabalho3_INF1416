package br.pucrio.inf1416.cofre.app;

import java.io.Console;
import java.sql.Connection;

import br.pucrio.inf1416.cofre.controller.LogController;
import br.pucrio.inf1416.cofre.dao.DatabaseConnection;
import br.pucrio.inf1416.cofre.dao.DatabaseInitializer;
import br.pucrio.inf1416.cofre.dao.LogDAO;
import br.pucrio.inf1416.cofre.dao.MessageDAO;
import br.pucrio.inf1416.cofre.service.CertificateService;

public class LogViewApp {

	public static void main(String[] args) {
		try {
			if (args.length != 1) {
				System.out.println("Uso:");
				System.out.println("java br.pucrio.inf1416.cofre.app.LogViewApp <caminho-chave-privada>");
				return;
			}

			String privateKeyPath = args[0];

			String secretPhrase = readSecretPhrase();

			Connection connection = DatabaseConnection.getConnection();

			DatabaseInitializer databaseInitializer = new DatabaseInitializer(connection);
			databaseInitializer.initialize();

			LogDAO logDAO = new LogDAO(connection);
			MessageDAO messageDAO = new MessageDAO(connection);
			CertificateService certificateService = new CertificateService();

			LogController logController = new LogController(certificateService, logDAO, messageDAO);

			logController.authenticateAdminPrivateKey(privateKeyPath, secretPhrase);

			logController.printLogs();

		} catch (Exception e) {
			System.err.println("Erro ao executar logView: " + e.getMessage());
		}
	}

	private static String readSecretPhrase() {
		Console console = System.console();

		if (console != null) {
			char[] password = console.readPassword("Frase secreta: ");
			return new String(password);
		}

		/*
		 * Dentro do Eclipse, System.console() normalmente é null. Então usamos Scanner
		 * como fallback.
		 */
		System.out.print("Frase secreta: ");

		try (java.util.Scanner scanner = new java.util.Scanner(System.in)) {
			return scanner.nextLine();
		}
	}
}
