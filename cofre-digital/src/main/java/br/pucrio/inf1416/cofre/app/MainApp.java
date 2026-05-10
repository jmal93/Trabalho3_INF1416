package br.pucrio.inf1416.cofre.app;

import java.sql.Connection;

import br.pucrio.inf1416.cofre.dao.DatabaseConnection;
import br.pucrio.inf1416.cofre.dao.DatabaseInitializer;
import br.pucrio.inf1416.cofre.dao.UserDAO;

public class MainApp {
	public static void main(String[] args) {
		try (Connection connection = DatabaseConnection.getConnection()) {
			DatabaseInitializer databaseInitializer = new DatabaseInitializer(connection);
			databaseInitializer.initialize();

			UserDAO userDAO = new UserDAO(connection);

			if (!userDAO.hasAnyUser()) {
				System.out.println("Primeira execução");
			} else {
				System.out.println("Execução normal");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
