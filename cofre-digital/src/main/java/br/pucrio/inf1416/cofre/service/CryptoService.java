package br.pucrio.inf1416.cofre.service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class CryptoService {
	public byte[] encryptWithPassword(byte[] data, String password) throws Exception {
		SecretKey secretKey = generateAESKeyFromPassword(password);

		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);

		return cipher.doFinal(data);
	}

	private SecretKey generateAESKeyFromPassword(String password) throws Exception {
		KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");

		SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
		secureRandom.setSeed(password.getBytes(StandardCharsets.UTF_8));

		keyGenerator.init(256, secureRandom);

		return keyGenerator.generateKey();
	}

	public byte[] decryptWithPassword(byte[] encryptedData, String password) throws Exception {
		SecretKey secretKey = generateAESKeyFromPassword(password);

		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, secretKey);

		return cipher.doFinal(encryptedData);
	}
}
