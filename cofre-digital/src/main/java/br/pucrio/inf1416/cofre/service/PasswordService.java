package br.pucrio.inf1416.cofre.service;

import java.security.SecureRandom;

import org.bouncycastle.crypto.generators.OpenBSDBCrypt;

public class PasswordService {
	private static final int BCRYPT_COST = 8;

	public boolean validPassword(String password) {
		boolean correctLength = passwordHasCorrectLength(password);
		boolean hasRepeatingCharactersInSequence = passwordHasRepeatingCharactersInSequence(password);
		boolean hasOnlyDigits = passwordHasOnlyDigits(password);

		return correctLength & !hasRepeatingCharactersInSequence & hasOnlyDigits;
	}

	private boolean passwordHasCorrectLength(String password) {
		if (password.length() < 8 || password.length() > 10) {
			return false;
		}

		return true;
	}

	private boolean passwordHasRepeatingCharactersInSequence(String password) {
		char[] passwordArray = password.toCharArray();
		char lastCharacter = passwordArray[0];

		for (int i = 1; i < password.length(); i++) {
			if (lastCharacter == passwordArray[i]) {
				return true;
			}

			lastCharacter = passwordArray[i];
		}

		return false;
	}

	private boolean passwordHasOnlyDigits(String password) {
		for (char character : password.toCharArray()) {
			if (!Character.isDigit(character)) {
				return false;
			}
		}

		return true;
	}

	public boolean passwordsAreEqual(String password, String confirmPassword) {
		return password.equals(confirmPassword);
	}

	public String encryptPassword(String password) {
		byte[] salt = new byte[16];
		new SecureRandom().nextBytes(salt);

		return OpenBSDBCrypt.generate("2y", password.toCharArray(), salt, BCRYPT_COST);
	}

	public boolean checkPassword(String password, String hash) {
		return OpenBSDBCrypt.checkPassword(hash, password.toCharArray());

	}
}
