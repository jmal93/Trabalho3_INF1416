package br.pucrio.inf1416.cofre.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PrivateKey;

import br.pucrio.inf1416.cofre.dao.GroupDAO;
import br.pucrio.inf1416.cofre.dao.KeyringDAO;
import br.pucrio.inf1416.cofre.dao.UserDAO;
import br.pucrio.inf1416.cofre.model.CertificateInfo;
import br.pucrio.inf1416.cofre.model.KeyPairRecord;
import br.pucrio.inf1416.cofre.model.RegisterUserData;
import br.pucrio.inf1416.cofre.model.User;
import br.pucrio.inf1416.cofre.service.AuditService;
import br.pucrio.inf1416.cofre.service.CertificateService;
import br.pucrio.inf1416.cofre.service.PasswordService;
import br.pucrio.inf1416.cofre.service.TOTPService;
import br.pucrio.inf1416.cofre.ui.RegisterUserView;
import br.pucrio.inf1416.cofre.ui.RegisterUserView.RegisterMode;

public class UserController {
	private final RegisterUserView registerUserView;

	private final CertificateService certificateService;
	private final PasswordService passwordService;
	private final TOTPService totpService;
	private final UserDAO userDAO;
	private final KeyringDAO keyringDAO;
	private final GroupDAO groupDAO;
	private final AuditService auditService;

	public UserController(RegisterUserView registerUserView, CertificateService certificateService,
			PasswordService passwordService, TOTPService totpService, UserDAO userDAO, KeyringDAO keyringDAO,
			GroupDAO groupDAO, AuditService auditService) {
		super();
		this.registerUserView = registerUserView;
		this.certificateService = certificateService;
		this.passwordService = passwordService;
		this.totpService = totpService;
		this.userDAO = userDAO;
		this.keyringDAO = keyringDAO;
		this.groupDAO = groupDAO;
		this.auditService = auditService;

		configureActions();
	}

	private void configureActions() {
		registerUserView.setRegisterAction(this::handleRegister);
		registerUserView.setBackAction(this::handleBack);
	}

	private void handleRegister() {
		try {
			RegisterUserData data = collectFormData();

			validateForm(data);

			CertificateInfo certificateInfo = certificateService.loadCertificate(data.certificatePath());

			boolean confirmed = registerUserView.showCertificateConfirmation(certificateInfo.toDisplayText());

			if (!confirmed) {
				auditService.log(6009);
				registerUserView.showMessage("Cadastro cancelado");
				return;
			}

			auditService.log(6008);

			PrivateKey privateKey = certificateService.loadEncryptedPrivateKey(data.privateKeyPath(),
					data.secretPhrase());

			boolean keyPairValid = certificateService.verifyKeyPair(privateKey, certificateInfo.certificate());

			if (!keyPairValid) {
				auditService.log(6007);
				registerUserView.showMessage("A chave privada não corresponde ao certificado informado");
				return;
			}

			String login = certificateInfo.email();
			String name = certificateInfo.name();

			if (login == null || login.isBlank()) {
				registerUserView.showMessage("Não foi possível extrair o e-mail do certificado");
				return;
			}

			if (name == null || name.isBlank()) {
				registerUserView.showMessage("Não foi possível extrair o nome do certificado");
				return;
			}

			if (userDAO.existsByLogin(login)) {
				registerUserView.showMessage("Já existe um usuário cadastrado com este e-mail");
				return;
			}

			int gid = resolveGroupId(data);

			String passwordHash = passwordService.encryptPassword(data.password());

			String base32Secret = totpService.generateBase32Secret();

			byte[] encryptedTotpSecret = totpService.encryptBase32Secret(base32Secret, data.password());

			User user = new User();
			user.setLogin(login);
			user.setNome(name);
			user.setGid(gid);
			user.setPasswordHash(passwordHash);
			user.setEncryptedTOTPSecret(encryptedTotpSecret);
			user.setTotalAccesses(0);
			user.setTotalQueries(0);

			int uid = userDAO.insert(user);
			user.setUid(uid);

			String certificatePemS = Files.readString(Path.of(data.certificatePath()));
			byte[] encryptedPrivateKey = Files.readAllBytes(Path.of(data.privateKeyPath()));

			KeyPairRecord keyPairRecord = new KeyPairRecord();
			keyPairRecord.setUid(uid);
			keyPairRecord.setCertificatePem(certificatePemS);
			keyPairRecord.setEncryptedPrivateKey(encryptedPrivateKey);

			keyringDAO.insert(keyPairRecord);

			registerUserView.showTotpSecret(login, base32Secret);
			registerUserView.clearForm();

			if (registerUserView.getMode() == RegisterMode.INITIAL_ADMIN) {
				registerUserView.showMessage("Administrador cadastrado. Reinicie ou siga para a autenticação padrão");
			}

		} catch (Exception e) {
			registerUserView.showMessage("Erro ao cadastrar usuário: " + e.getMessage());
		}
	}

	private RegisterUserData collectFormData() {
		return new RegisterUserData(registerUserView.getCertificatePath(), registerUserView.getPrivateKeyPath(),
				registerUserView.getSecretPhrase(), registerUserView.getSelectedGroup(), registerUserView.getPassword(),
				registerUserView.getConfirmPassword());

	}

	private void validateForm(RegisterUserData data) throws Exception {
		if (data.certificatePath() == null || data.certificatePath().isBlank()) {
			auditService.log(6004);
			throw new IllegalArgumentException("Informe o caminho do certificado digital");
		}
		if (data.privateKeyPath() == null || data.privateKeyPath().isBlank()) {
			auditService.log(6005);
			throw new IllegalArgumentException("Informe o caminho da chave privada");
		}

		if (data.secretPhrase() == null || data.secretPhrase().isBlank()) {
			auditService.log(6006);
			throw new IllegalArgumentException("Informe a frase secreta da chave privada");
		}

		if (!Files.exists(Path.of(data.certificatePath()))) {
			auditService.log(6004);
			throw new IllegalArgumentException("Arquivo do certificado digital inválido");
		}

		if (!Files.exists(Path.of(data.privateKeyPath()))) {
			auditService.log(6005);
			throw new IllegalArgumentException("Arquivo de chave privada inválido");
		}

		if (!passwordService.passwordsAreEqual(data.password(), data.confirmPassword())) {
			auditService.log(6003);
			throw new IllegalArgumentException("A senha e a confirmação de senha não conferem");
		}

		if (!passwordService.validPassword(data.password())) {
			auditService.log(6003);
			throw new IllegalArgumentException(
					"A senha pessoal deve ter 8 a 10 dígitos, apenas números e sem dígitos repetidos em sequência");
		}

		if (registerUserView.getMode() == RegisterMode.INITIAL_ADMIN && !"Administrador".equals(data.groupName())) {
			throw new IllegalArgumentException("Na primeira execução, o grupo deve ser Administrador");
		}
	}

	private int resolveGroupId(RegisterUserData data) throws Exception {
		if (registerUserView.getMode() == RegisterMode.INITIAL_ADMIN) {
			return groupDAO.getGroupIdByName("Administrador");
		}

		return groupDAO.getGroupIdByName(data.groupName());
	}

	private void handleBack() {
		registerUserView.dispose();
	}
}
