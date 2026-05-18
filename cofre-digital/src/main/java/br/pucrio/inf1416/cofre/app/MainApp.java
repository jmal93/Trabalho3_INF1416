package br.pucrio.inf1416.cofre.app;

import java.security.PrivateKey;
import java.sql.Connection;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import br.pucrio.inf1416.cofre.controller.AuthController;
import br.pucrio.inf1416.cofre.controller.UserController;
import br.pucrio.inf1416.cofre.dao.DatabaseConnection;
import br.pucrio.inf1416.cofre.dao.DatabaseInitializer;
import br.pucrio.inf1416.cofre.dao.GroupDAO;
import br.pucrio.inf1416.cofre.dao.KeyringDAO;
import br.pucrio.inf1416.cofre.dao.LogDAO;
import br.pucrio.inf1416.cofre.dao.UserDAO;
import br.pucrio.inf1416.cofre.model.CertificateInfo;
import br.pucrio.inf1416.cofre.model.KeyPairRecord;
import br.pucrio.inf1416.cofre.model.User;
import br.pucrio.inf1416.cofre.service.AuditService;
import br.pucrio.inf1416.cofre.service.AuthenticationService;
import br.pucrio.inf1416.cofre.service.CertificateService;
import br.pucrio.inf1416.cofre.service.CryptoService;
import br.pucrio.inf1416.cofre.service.PasswordService;
import br.pucrio.inf1416.cofre.service.QRCodeService;
import br.pucrio.inf1416.cofre.service.TOTPService;
import br.pucrio.inf1416.cofre.service.VaultService;
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
			QRCodeService qrCodeService = new QRCodeService();
			TOTPService totpService = new TOTPService(cryptoService);
			CertificateService certificateService = new CertificateService();

			auditService.log(1001);

			if (!userDAO.hasAnyUser()) {
				auditService.log(1005);

				RegisterUserView registerUserView = new RegisterUserView(RegisterMode.INITIAL_ADMIN);

				new UserController(registerUserView, certificateService, passwordService, totpService, qrCodeService,
						userDAO, keyringDAO, groupDAO, auditService);

				registerUserView.setVisible(true);

			} else {
				auditService.log(1006);

				String adminSecretPhrase = requestAdminSecretPhrase();

				validateAdminSecretPhrase(userDAO, keyringDAO, certificateService, adminSecretPhrase);

				VaultService vaultService = new VaultService(cryptoService, certificateService, keyringDAO, userDAO,
						auditService, adminSecretPhrase);

				AuthenticationService authenticationService = new AuthenticationService(userDAO, passwordService,
						totpService, auditService);

				LoginView loginView = new LoginView();

				new AuthController(loginView, authenticationService, certificateService, passwordService, totpService,
						qrCodeService, userDAO, keyringDAO, groupDAO, auditService, vaultService);

				loginView.setVisible(true);
			}

		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Erro ao iniciar o sistema: " + e.getMessage(), "Erro",
					JOptionPane.ERROR_MESSAGE);

			e.printStackTrace();
			System.exit(1);
		}
	}

	private static String requestAdminSecretPhrase() {
		JPasswordField passwordField = new JPasswordField(30);

		int option = JOptionPane.showConfirmDialog(null, passwordField,
				"Frase secreta da chave privada do administrador", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);

		if (option != JOptionPane.OK_OPTION) {
			throw new IllegalArgumentException("Inicialização cancelada.");
		}

		String secretPhrase = new String(passwordField.getPassword());

		if (secretPhrase.isBlank()) {
			throw new IllegalArgumentException("Frase secreta do administrador não informada.");
		}

		return secretPhrase;
	}

	private static void validateAdminSecretPhrase(UserDAO userDAO, KeyringDAO keyringDAO,
			CertificateService certificateService, String adminSecretPhrase) throws Exception {

		User adminUser = userDAO.findFirstAdmin();

		if (adminUser == null) {
			throw new IllegalArgumentException("Administrador não encontrado.");
		}

		KeyPairRecord keyPairRecord = keyringDAO.findByUserId(adminUser.getUid());

		if (keyPairRecord == null) {
			throw new IllegalArgumentException("Chaveiro do administrador não encontrado.");
		}

		CertificateInfo certificateInfo = certificateService.loadCertificateFromPem(keyPairRecord.getCertificatePem());

		PrivateKey privateKey = certificateService.loadEncryptedPrivateKey(keyPairRecord.getEncryptedPrivateKey(),
				adminSecretPhrase);

		boolean valid = certificateService.verifyKeyPair(privateKey, certificateInfo.certificate());

		if (!valid) {
			throw new IllegalArgumentException(
					"Frase secreta do administrador inválida ou chave privada incompatível.");
		}
	}
}
