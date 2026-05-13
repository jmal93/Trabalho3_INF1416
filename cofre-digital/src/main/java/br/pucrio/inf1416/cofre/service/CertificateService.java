package br.pucrio.inf1416.cofre.service;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

import br.pucrio.inf1416.cofre.model.CertificateInfo;

public class CertificateService {

	public CertificateInfo loadCertificate(String certificatePath) throws Exception {
		String pem = Files.readString(Path.of(certificatePath));

		int begin = pem.indexOf("-----BEGIN CERTIFICATE-----");
		int end = pem.indexOf("-----END CERTIFICATE-----");

		if (begin == -1 || end == -1) {
			throw new IllegalArgumentException("Arquivo de certificado não está no formato PEM esperado.");
		}

		String base64 = pem.substring(begin + "-----BEGIN CERTIFICATE-----".length(), end).replaceAll("\\s", "");

		byte[] certificateBytes = Base64.getDecoder().decode(base64);

		CertificateFactory factory = CertificateFactory.getInstance("X.509");

		X509Certificate certificate = (X509Certificate) factory
				.generateCertificate(new ByteArrayInputStream(certificateBytes));

		String subject = certificate.getSubjectX500Principal().getName();

		return new CertificateInfo(certificate, String.valueOf(certificate.getVersion()),
				certificate.getSerialNumber().toString(),
				certificate.getNotBefore() + " até " + certificate.getNotAfter(), certificate.getSigAlgName(),
				certificate.getIssuerX500Principal().getName(), subject, extractCommonName(subject),
				extractEmail(subject));
	}

	private String extractCommonName(String subject) {
		for (String part : subject.split(",")) {
			String trimmed = part.trim();

			if (trimmed.startsWith("CN=")) {
				return trimmed.substring(3);
			}
		}

		return "";
	}

	private String extractEmail(String subject) {
		for (String part : subject.split(",")) {
			String trimmed = part.trim();

			if (trimmed.startsWith("EMAILADDRESS=")) {
				return trimmed.substring("EMAILADDRESS=".length());
			}

			if (trimmed.startsWith("E=")) {
				return trimmed.substring(2);
			}

			if (trimmed.startsWith("1.2.840.113549.1.9.1=")) {
				String value = trimmed.substring("1.2.840.113549.1.9.1=".length());

				if (value.startsWith("#")) {
					return decodeAsn1HexEmail(value);
				}

				return value;
			}
		}

		return "";
	}

	private String decodeAsn1HexEmail(String value) {
		String hex = value.substring(1);
		System.out.println(hex);

		if (hex.startsWith("16") && hex.length() >= 4) {
			hex = hex.substring(4);
		}

		StringBuilder result = new StringBuilder();

		for (int i = 0; i < hex.length(); i += 2) {
			String byteHex = hex.substring(i, i + 2);
			int character = Integer.parseInt(byteHex, 16);
			result.append((char) character);
		}

		return result.toString();
	}

	public PrivateKey loadEncryptedPrivateKey(String privateKeyPath, String secretPhrase) throws Exception {
		byte[] encryptedPrivateKeyBytes = Files.readAllBytes(Path.of(privateKeyPath));

		byte[] decryptedBytes = decryptPrivateKey(encryptedPrivateKeyBytes, secretPhrase);

		byte[] privateKeyBytes = extractPrivateKeyBytes(decryptedBytes);

		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);

		KeyFactory keyFactory = KeyFactory.getInstance("RSA");

		return keyFactory.generatePrivate(keySpec);
	}

	private byte[] extractPrivateKeyBytes(byte[] decryptedBytes) {
		String text = new String(decryptedBytes, java.nio.charset.StandardCharsets.UTF_8).trim();

		if (text.contains("-----BEGIN PRIVATE KEY-----")) {
			String base64 = text.replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "")
					.replaceAll("\\s", "");

			return Base64.getDecoder().decode(base64);
		}

		if (looksLikeBase64(text)) {
			return Base64.getDecoder().decode(text.replaceAll("\\s", ""));
		}

		return decryptedBytes;
	}

	private boolean looksLikeBase64(String text) {
		String normalized = text.replaceAll("\\s", "");

		if (normalized.isEmpty()) {
			return false;
		}

		return normalized.matches("[A-Za-z0-9+/=]+");
	}

	private byte[] decryptPrivateKey(byte[] encryptedPrivateKeyBytes, String secretPhrase) throws Exception {
		KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");

		SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
		secureRandom.setSeed(secretPhrase.getBytes(java.nio.charset.StandardCharsets.UTF_8));

		keyGenerator.init(256, secureRandom);

		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, keyGenerator.generateKey());

		return cipher.doFinal(encryptedPrivateKeyBytes);
	}

	public boolean verifyKeyPair(PrivateKey privateKey, X509Certificate certificate) throws Exception {
		byte[] randomData = new byte[9216];
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		random.nextBytes(randomData);

		Signature signature = Signature.getInstance(certificate.getSigAlgName());
		signature.initSign(privateKey);
		signature.update(randomData);

		byte[] signatureBytes = signature.sign();

		PublicKey publicKey = certificate.getPublicKey();

		Signature verifier = Signature.getInstance(certificate.getSigAlgName());
		verifier.initVerify(publicKey);
		verifier.update(randomData);

		return verifier.verify(signatureBytes);
	}
}
