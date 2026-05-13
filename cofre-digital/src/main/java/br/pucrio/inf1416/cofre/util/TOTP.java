package br.pucrio.inf1416.cofre.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class TOTP {
	private byte[] key = null;
	private long timeStepInSeconds = 30;

	public TOTP(String base32EncodedSecret, long timeStepInSeconds) throws Exception {
		if (base32EncodedSecret == null || base32EncodedSecret.isBlank()) {
			throw new IllegalArgumentException("Segredo TOTP inválido");
		}

		if (timeStepInSeconds <= 0) {
			throw new IllegalArgumentException("Intervalo de tempo inválido");
		}

		this.timeStepInSeconds = timeStepInSeconds;

		Base32 base32 = new Base32(Base32.Alphabet.BASE32, false, false);

		this.key = base32.fromString(base32EncodedSecret);

		if (this.key == null || this.key.length == 0) {
			throw new IllegalArgumentException("Erro ao decodificar segredo em base 32");
		}
	}

	private String getTOTPCodeFromHash(byte[] hash) {
		int offset = hash[hash.length - 1] & 0x0F;

		int binaryCode = ((hash[offset] & 0x7F) << 24) | ((hash[offset + 1] & 0xFF) << 16)
				| ((hash[offset + 2] & 0xFF) << 8) | (hash[offset + 3] & 0xFF);

		int otp = binaryCode % 1_000_000;

		return String.format("%06d", otp);
	}

	private byte[] HMAC_SHA1(byte[] counter, byte[] keyByteArray) {
		try {
			SecretKeySpec signKey = new SecretKeySpec(keyByteArray, "HmacSHA1");

			Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(signKey);

			return mac.doFinal(counter);
		} catch (Exception e) {
			throw new RuntimeException("Erro ao calcular HMAC-SHA1", e);
		}
	}

	private String TOTPCode(long timeInterval) {
	}

	public String generateCode() {
	}

	public boolean validateCode(String inputTOTP) {
	}
}
