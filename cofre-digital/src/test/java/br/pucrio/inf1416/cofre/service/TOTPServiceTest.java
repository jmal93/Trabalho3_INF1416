package br.pucrio.inf1416.cofre.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pucrio.inf1416.cofre.util.TOTP;

class TOTPServiceTest {
	private TOTPService totpService;

	@BeforeEach
	void setUp() throws Exception {
		CryptoService cryptoService = new CryptoService();
		totpService = new TOTPService(cryptoService);
	}

	@Test
	void shouldGenerateBase32Secret() {
		String secret = totpService.generateBase32Secret();

		assertNotNull(secret);
		assertFalse(secret.isBlank());
		assertTrue(secret.matches("[A-Z2-7]+"));
	}

	@Test
	void shouldEncryptAndDecryptBase32Secret() throws Exception {
		String password = "13572468";
		String secret = totpService.generateBase32Secret();

		byte[] encrypted = totpService.encryptBase32Secret(secret, password);

		assertNotNull(encrypted);
		assertNotEquals(secret, new String(encrypted));

		String decrypted = totpService.decryptBase32Secret(encrypted, password);

		assertEquals(secret, decrypted);
	}

	@Test
	void shouldGenerateAndValidateCurrentCode() throws Exception {
		String secret = totpService.generateBase32Secret();

		String code = totpService.generateCurrentCodeForTests(secret);

		assertTrue(code.matches("\\d{6}"));

		TOTP totp = new TOTP(secret, 30);

		assertTrue(totp.validateCode(code));
	}
}
