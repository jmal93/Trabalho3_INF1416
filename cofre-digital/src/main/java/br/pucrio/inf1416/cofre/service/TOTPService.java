package br.pucrio.inf1416.cofre.service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import br.pucrio.inf1416.cofre.model.User;
import br.pucrio.inf1416.cofre.util.Base32;
import br.pucrio.inf1416.cofre.util.TOTP;

public class TOTPService {
	private static final int SECRET_SIZE_BYTES = 20;
	private static final long TIME_STEP_SECONDS = 30;

	private final CryptoService cryptoService;

	public TOTPService(CryptoService cryptoService) {
		this.cryptoService = cryptoService;
	}

	public String generateBase32Secret() {
		byte[] secretBytes = new byte[SECRET_SIZE_BYTES];

		SecureRandom secureRandom = new SecureRandom();
		secureRandom.nextBytes(secretBytes);

		Base32 base32 = new Base32(Base32.Alphabet.BASE32, false, false);

		return base32.toString(secretBytes);
	}

	public byte[] encryptBase32Secret(String base32Secret, String personalPassword) throws Exception {
		if (base32Secret == null || base32Secret.isBlank()) {
			throw new IllegalArgumentException("Segredo TOTP inválido");
		}

		if (personalPassword == null || personalPassword.isBlank()) {
			throw new IllegalArgumentException("Senha pessoal inválida");
		}

		byte[] secretBytes = base32Secret.getBytes(StandardCharsets.UTF_8);

		return cryptoService.encryptWithPassword(secretBytes, personalPassword);
	}

	public String decryptBase32Secret(byte[] encryptedSecret, String personalPassword) throws Exception {
		if (encryptedSecret == null || encryptedSecret.length == 0) {
			throw new IllegalArgumentException("Segredo TOTP criptografado inválido");
		}

		if (personalPassword == null || personalPassword.isBlank()) {
			throw new IllegalArgumentException("Senha pessoal inválida");
		}

		byte[] decryptedBytes = cryptoService.decryptWithPassword(encryptedSecret, personalPassword);

		return new String(decryptedBytes, StandardCharsets.UTF_8);
	}

	public boolean validateToken(User user, String inputToken, String personalPassword) throws Exception {
		byte[] encryptedSecret = user.getEncryptedTOTPSecret();

		String base32Secret = decryptBase32Secret(encryptedSecret, personalPassword);

		System.out.println("Senha usada para abrir TOTP: " + personalPassword);
		System.out.println("Segredo TOTP decriptado: " + base32Secret);
		System.out.println("Token digitado: " + inputToken);

		TOTP totp = new TOTP(base32Secret, TIME_STEP_SECONDS);

		String currentCode = totp.generateCode();
		System.out.println("Token calculado agora pelo sistema: " + currentCode);

		return totp.validateCode(inputToken);
	}

	public String generateCurrentCodeForTests(String base32Secret) throws Exception {
		TOTP totp = new TOTP(base32Secret, TIME_STEP_SECONDS);
		return totp.generateCode();
	}
}
