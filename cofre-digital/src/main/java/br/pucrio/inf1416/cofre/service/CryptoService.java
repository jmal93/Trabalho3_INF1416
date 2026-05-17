package br.pucrio.inf1416.cofre.service;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class CryptoService {
	public byte[] encryptWithPassword(byte[] data, String password) throws Exception {
		SecretKey secretKey = generateAESKeyFromPassword(password);

		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);

		return cipher.doFinal(data);
	}

	public byte[] decryptWithPassword(byte[] encryptedData, String password) throws Exception {
		SecretKey secretKey = generateAESKeyFromPassword(password);

		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, secretKey);

		return cipher.doFinal(encryptedData);
	}

	private SecretKey generateAESKeyFromPassword(String password) throws Exception {
		KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");

		SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
		secureRandom.setSeed(password.getBytes(StandardCharsets.UTF_8));

		keyGenerator.init(256, secureRandom);

		return keyGenerator.generateKey();
	}

	public byte[] decryptWithPrivateKey(byte[] encryptedData, PrivateKey privateKey) throws Exception {
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.DECRYPT_MODE, privateKey);

		return cipher.doFinal(encryptedData);
	}

	public SecretKey restoreAESKey(byte[] keyBytes) {
		return new SecretKeySpec(keyBytes, "AES");
	}

	public byte[] decryptWithAESKey(byte[] encryptedData, SecretKey secretKey) throws Exception {
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, secretKey);

		return cipher.doFinal(encryptedData);
	}

	public boolean verifySignature(byte[] data, byte[] signatureBytes, PublicKey publicKey, String algorithm)
			throws Exception {
		Signature signature = Signature.getInstance(algorithm);
		signature.initVerify(publicKey);
		signature.update(data);

		return signature.verify(signatureBytes);
	}

	public SecretKey generateAESKeyFromSeed(byte[] seedBytes) throws Exception {
		KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");

		SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
		secureRandom.setSeed(seedBytes);

		keyGenerator.init(256, secureRandom);

		return keyGenerator.generateKey();
	}

	public boolean verifySignatureWithAlgorithms(byte[] data, byte[] signatureBytes, PublicKey publicKey)
			throws Exception {
		String[] algorithms = { "SHA1withRSA", "SHA256withRSA", "SHA384withRSA", "SHA512withRSA", "MD5withRSA" };

		for (String algorithm : algorithms) {
			try {
				boolean valid = verifySignature(data, signatureBytes, publicKey, algorithm);

				System.out.println("Testando algoritmo " + algorithm + ": " + valid);

				if (valid) {
					return true;
				}
			} catch (Exception e) {
				System.out.println("Algoritmo falhou: " + algorithm + " - " + e.getMessage());
			}
		}

		return false;
	}
}
