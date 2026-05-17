package br.pucrio.inf1416.cofre.app;

import java.sql.Connection;

import br.pucrio.inf1416.cofre.controller.AuthController;
import br.pucrio.inf1416.cofre.controller.UserController;
import br.pucrio.inf1416.cofre.dao.DatabaseConnection;
import br.pucrio.inf1416.cofre.dao.DatabaseInitializer;
import br.pucrio.inf1416.cofre.dao.GroupDAO;
import br.pucrio.inf1416.cofre.dao.KeyringDAO;
import br.pucrio.inf1416.cofre.dao.LogDAO;
import br.pucrio.inf1416.cofre.dao.UserDAO;
import br.pucrio.inf1416.cofre.service.AuditService;
import br.pucrio.inf1416.cofre.service.AuthenticationService;
import br.pucrio.inf1416.cofre.service.CertificateService;
import br.pucrio.inf1416.cofre.service.CryptoService;
import br.pucrio.inf1416.cofre.service.PasswordService;
import br.pucrio.inf1416.cofre.service.TOTPService;
import br.pucrio.inf1416.cofre.ui.LoginView;
import br.pucrio.inf1416.cofre.ui.RegisterUserView;
import br.pucrio.inf1416.cofre.ui.RegisterUserView.RegisterMode;

public class MainApp {
	public static void main(String[] args) {
		try {
			Connection connection = DatabaseConnection.getConnection();

			DatabaseInitializer databaseInitializer = new DatabaseInitializer(connection);
			databaseInitializer.initialize();

			UserDAO userDAO = new UserDAO(connection);
			LogDAO logDAO = new LogDAO(connection);
			KeyringDAO keyringDAO = new KeyringDAO(connection);
			GroupDAO groupDAO = new GroupDAO(connection);

			AuditService auditService = new AuditService(logDAO);
			PasswordService passwordService = new PasswordService();
			CryptoService cryptoService = new CryptoService();
			TOTPService totpService = new TOTPService(cryptoService);
			CertificateService certificateService = new CertificateService();

			auditService.log(1001);

			if (!userDAO.hasAnyUser()) {
				auditService.log(1005);

				RegisterUserView registerUserView = new RegisterUserView(RegisterMode.INITIAL_ADMIN);

				new UserController(registerUserView, certificateService, passwordService, totpService, userDAO,
						keyringDAO, groupDAO, auditService);

				registerUserView.setVisible(true);
			} else {
				auditService.log(1006);

				AuthenticationService authenticationService = new AuthenticationService(userDAO, passwordService,
						totpService, auditService);

				LoginView loginView = new LoginView();

				new AuthController(loginView, authenticationService, null, null);

				loginView.setVisible(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
