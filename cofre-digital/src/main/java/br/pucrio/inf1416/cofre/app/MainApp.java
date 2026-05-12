package br.pucrio.inf1416.cofre.app;

import java.sql.Connection;

import br.pucrio.inf1416.cofre.dao.DatabaseConnection;
import br.pucrio.inf1416.cofre.dao.DatabaseInitializer;
import br.pucrio.inf1416.cofre.dao.UserDAO;
import br.pucrio.inf1416.cofre.ui.RegisterUserView;
import br.pucrio.inf1416.cofre.ui.RegisterUserView.RegisterMode;

public class MainApp {
	public static void main(String[] args) {
		try (Connection connection = DatabaseConnection.getConnection()) {
			DatabaseInitializer databaseInitializer = new DatabaseInitializer(connection);
			databaseInitializer.initialize();

			UserDAO userDAO = new UserDAO(connection);

			if (!userDAO.hasAnyUser()) {
				RegisterUserView registerUserView = new RegisterUserView(RegisterMode.INITIAL_ADMIN);
				registerUserView.setVisible(true);
//				LoginView loginView = new LoginView();
//				loginView.setVisible(true);
			} else {
				RegisterUserView registerUserView = new RegisterUserView(RegisterMode.INITIAL_ADMIN);
				registerUserView.setVisible(false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
