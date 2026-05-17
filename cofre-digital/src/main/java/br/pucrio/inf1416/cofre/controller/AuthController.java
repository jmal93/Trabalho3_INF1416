package br.pucrio.inf1416.cofre.controller;

import br.pucrio.inf1416.cofre.dao.GroupDAO;
import br.pucrio.inf1416.cofre.dao.KeyringDAO;
import br.pucrio.inf1416.cofre.dao.UserDAO;
import br.pucrio.inf1416.cofre.model.User;
import br.pucrio.inf1416.cofre.service.AuditService;
import br.pucrio.inf1416.cofre.service.AuthenticationService;
import br.pucrio.inf1416.cofre.service.CertificateService;
import br.pucrio.inf1416.cofre.service.PasswordService;
import br.pucrio.inf1416.cofre.service.TOTPService;
import br.pucrio.inf1416.cofre.service.VaultService;
import br.pucrio.inf1416.cofre.ui.LoginView;
import br.pucrio.inf1416.cofre.ui.MainMenuView;

public class AuthController {

	private final LoginView loginView;
	private final AuthenticationService authenticationService;

	private final CertificateService certificateService;
	private final PasswordService passwordService;
	private final TOTPService totpService;
	private final UserDAO userDAO;
	private final KeyringDAO keyringDAO;
	private final GroupDAO groupDAO;
	private final AuditService auditService;
	private final VaultService vaultService;

	private User currentUser;
	private String validatedPassword;

	public AuthController(LoginView loginView, AuthenticationService authenticationService,
			CertificateService certificateService, PasswordService passwordService, TOTPService totpService,
			UserDAO userDAO, KeyringDAO keyringDAO, GroupDAO groupDAO, AuditService auditService,
			VaultService vaultService) {
		this.loginView = loginView;
		this.authenticationService = authenticationService;
		this.certificateService = certificateService;
		this.passwordService = passwordService;
		this.totpService = totpService;
		this.userDAO = userDAO;
		this.keyringDAO = keyringDAO;
		this.groupDAO = groupDAO;
		this.auditService = auditService;
		this.vaultService = vaultService;

		this.currentUser = null;
		this.validatedPassword = null;

		configureActions();
	}

	private void configureActions() {
		loginView.setLoginAction(this::handleLogin);
		loginView.setPasswordConfirmAction(this::handlePassword);
		loginView.setPasswordClearAction(() -> loginView.clearPasswordInput());
		loginView.setTokenConfirmAction(this::handleTotp);
	}

	private void handleLogin() {
		try {
			String email = loginView.getEmail();

			currentUser = authenticationService.validateLogin(email);

			loginView.showPasswordStep();

		} catch (Exception e) {
			currentUser = null;
			validatedPassword = null;

			loginView.showMessage(e.getMessage());
			loginView.showLoginStep();
		}
	}

	private void handlePassword() {
		try {
			if (currentUser == null) {
				throw new IllegalStateException("Nenhum usuário selecionado");
			}

			validatedPassword = authenticationService.validatePasswordByPairs(currentUser, loginView.getPressedPairs());

			loginView.showTotpStep();

		} catch (Exception e) {
			loginView.showMessage(e.getMessage());

			if (authenticationService.isUserBlocked(currentUser)) {
				resetAuthenticationState();
				loginView.showLoginStep();
			} else {
				loginView.clearPasswordInput();
			}
		}
	}

	private void handleTotp() {
		try {
			if (currentUser == null) {
				throw new IllegalStateException("Nenhum usuário selecionado");
			}

			if (validatedPassword == null || validatedPassword.isBlank()) {
				throw new IllegalStateException("Senha pessoal ainda não foi validada");
			}

			String token = loginView.getToken();

			authenticationService.validateTotp(currentUser, token, validatedPassword);

			authenticationService.registerSuccessfulAccess(currentUser);

			openMainMenu();

		} catch (Exception e) {
			loginView.showMessage(e.getMessage());

			if (authenticationService.isUserBlocked(currentUser)) {
				resetAuthenticationState();
				loginView.showLoginStep();
			} else {
				loginView.clearTokenInput();
			}
		}
	}

	private void openMainMenu() {
		User authenticatedUser = currentUser;

		resetSensitiveStateOnly();

		loginView.dispose();

		MainMenuView mainMenuView = new MainMenuView(authenticatedUser);

		new MainMenuController(mainMenuView, authenticatedUser, certificateService, passwordService, totpService,
				userDAO, keyringDAO, groupDAO, auditService, vaultService);

		mainMenuView.setVisible(true);
	}

	private void resetAuthenticationState() {
		currentUser = null;
		validatedPassword = null;
		loginView.clearPasswordInput();
		loginView.clearTokenInput();
	}

	private void resetSensitiveStateOnly() {
		validatedPassword = null;
	}
}
