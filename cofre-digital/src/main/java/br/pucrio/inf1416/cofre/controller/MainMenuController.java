package br.pucrio.inf1416.cofre.controller;

import br.pucrio.inf1416.cofre.dao.GroupDAO;
import br.pucrio.inf1416.cofre.dao.KeyringDAO;
import br.pucrio.inf1416.cofre.dao.UserDAO;
import br.pucrio.inf1416.cofre.model.User;
import br.pucrio.inf1416.cofre.service.AuditService;
import br.pucrio.inf1416.cofre.service.CertificateService;
import br.pucrio.inf1416.cofre.service.PasswordService;
import br.pucrio.inf1416.cofre.service.QRCodeService;
import br.pucrio.inf1416.cofre.service.TOTPService;
import br.pucrio.inf1416.cofre.service.VaultService;
import br.pucrio.inf1416.cofre.ui.MainMenuView;
import br.pucrio.inf1416.cofre.ui.RegisterUserView;
import br.pucrio.inf1416.cofre.ui.RegisterUserView.RegisterMode;
import br.pucrio.inf1416.cofre.ui.SecretFolderView;

public class MainMenuController {
	private final MainMenuView mainMenuView;
	private final User currentUser;

	private final CertificateService certificateService;
	private final PasswordService passwordService;
	private final TOTPService totpService;
	private final QRCodeService qrCodeService;
	private final UserDAO userDAO;
	private final KeyringDAO keyringDAO;
	private final GroupDAO groupDAO;
	private final AuditService auditService;
	private final VaultService vaultService;

	public MainMenuController(MainMenuView mainMenuView, User currentUser, CertificateService certificateService,
			PasswordService passwordService, TOTPService totpService, QRCodeService qrCodeService, UserDAO userDAO,
			KeyringDAO keyringDAO, GroupDAO groupDAO, AuditService auditService, VaultService vaultService) {
		super();
		this.mainMenuView = mainMenuView;
		this.currentUser = currentUser;
		this.certificateService = certificateService;
		this.passwordService = passwordService;
		this.totpService = totpService;
		this.qrCodeService = qrCodeService;
		this.userDAO = userDAO;
		this.keyringDAO = keyringDAO;
		this.groupDAO = groupDAO;
		this.auditService = auditService;
		this.vaultService = vaultService;

		configureActions();
	}

	private void configureActions() {
		mainMenuView.setRegisterUserAction(this::handleRegisterUser);
		mainMenuView.setOpenVaultAction(this::handleOpenVault);
		mainMenuView.setExitAction(this::handleExit);
	}

	private void handleRegisterUser() {
		try {
			if (!"Administrador".equalsIgnoreCase(currentUser.getGroupName())) {
				mainMenuView.showMessage("Apenas administradores podem cadastrar novos usuários");
				return;
			}

			RegisterUserView registerUserView = new RegisterUserView(RegisterMode.NEW_USER);

			new UserController(registerUserView, certificateService, passwordService, totpService, qrCodeService,
					userDAO, keyringDAO, groupDAO, auditService);

			registerUserView.setVisible(true);
		} catch (Exception e) {
			mainMenuView.showMessage("Erro ao abrir cadastro: " + e.getMessage());
		}
	}

	private void handleOpenVault() {
		try {
			SecretFolderView secretFolderView = new SecretFolderView(currentUser);

			new VaultController(secretFolderView, vaultService, currentUser);

			secretFolderView.setVisible(true);
		} catch (Exception e) {
			mainMenuView.showMessage("Erro ao abrir pasta secreta: " + e.getMessage());
		}
	}

	private void handleExit() {
		try {
			auditService.log(1002, currentUser);
			System.exit(0);
		} catch (Exception e) {
			mainMenuView.showMessage("Erro ao sair: " + e.getMessage());
		}
	}
}
