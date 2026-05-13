package br.pucrio.inf1416.cofre.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import java.nio.file.Path;
import java.security.PrivateKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pucrio.inf1416.cofre.model.CertificateInfo;

class CertificateServiceTest {

	private CertificateService certificateService;

	@BeforeEach
	void setUp() {
		certificateService = new CertificateService();
	}

	@Test
	void shouldLoadCertificateAndExtractInformation() throws Exception {

		String certificatePath = getResourcePath("user01-x509.crt");

		CertificateInfo info = certificateService.loadCertificate(certificatePath);

		assertNotNull(info);

		assertNotNull(info.certificate());

		assertEquals("admin@inf1416.puc-rio.br", info.email());

		assertFalse(info.name().isBlank());

		assertFalse(info.subject().isBlank());

		assertFalse(info.issuer().isBlank());

		assertFalse(info.signatureType().isBlank());
	}

	@Test
	void shouldLoadEncryptedPrivateKey() throws Exception {

		String privateKeyPath = getResourcePath("user01-pkcs8-aes.key");

		String secretPhrase = "user01";

		PrivateKey privateKey = certificateService.loadEncryptedPrivateKey(privateKeyPath, secretPhrase);

		assertNotNull(privateKey);

		assertEquals("RSA", privateKey.getAlgorithm());
	}

	@Test
	void shouldVerifyMatchingCertificateAndPrivateKey() throws Exception {

		String certificatePath = getResourcePath("user01-x509.crt");

		String privateKeyPath = getResourcePath("user01-pkcs8-aes.key");

		String secretPhrase = "user01";

		CertificateInfo info = certificateService.loadCertificate(certificatePath);

		PrivateKey privateKey = certificateService.loadEncryptedPrivateKey(privateKeyPath, secretPhrase);

		boolean result = certificateService.verifyKeyPair(privateKey, info.certificate());

		assertTrue(result);
	}

	@Test
	void shouldFailWhenSecretPhraseIsWrong() {

		String wrongSecretPhrase = "senha-errada";

		assertThrows(Exception.class, () -> {
			String privateKeyPath = getResourcePath("user01-pkcs8-aes.key");

			certificateService.loadEncryptedPrivateKey(privateKeyPath, wrongSecretPhrase);
		});
	}

	@Test
	void shouldFailWhenCertificateAndPrivateKeyDoNotMatch() throws Exception {

		String adminCertificatePath = getResourcePath("user02-x509.crt");

		String userPrivateKeyPath = getResourcePath("user01-pkcs8-aes.key");

		String secretPhrase = "user01";

		CertificateInfo adminCertificate = certificateService.loadCertificate(adminCertificatePath);

		PrivateKey userPrivateKey = certificateService.loadEncryptedPrivateKey(userPrivateKeyPath, secretPhrase);

		boolean result = certificateService.verifyKeyPair(userPrivateKey, adminCertificate.certificate());

		assertFalse(result);
	}

	private String getResourcePath(String resourceName) throws Exception {

		URL resource = getClass().getClassLoader().getResource(resourceName);

		assertNotNull(resource, "Arquivo de teste não encontrado: " + resourceName);

		return Path.of(resource.toURI()).toString();
	}
}
