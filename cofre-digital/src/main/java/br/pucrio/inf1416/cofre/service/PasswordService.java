package br.pucrio.inf1416.cofre.service;

import java.security.SecureRandom;

import org.bouncycastle.crypto.generators.OpenBSDBCrypt;

public class PasswordService {
	private static final int BCRYPT_COST = 8;

	public boolean validPassword(String password) {
		if (password == null) {
			return false;
		}

		boolean correctLength = passwordHasCorrectLength(password);
		boolean hasRepeatingCharactersInSequence = passwordHasRepeatingCharactersInSequence(password);
		boolean hasOnlyDigits = passwordHasOnlyDigits(password);

		return correctLength && !hasRepeatingCharactersInSequence && hasOnlyDigits;
	}

	private boolean passwordHasCorrectLength(String password) {
		if (password.length() < 8 || password.length() > 10) {
			return false;
		}

		return true;
	}

	private boolean passwordHasRepeatingCharactersInSequence(String password) {
		if (password.isEmpty()) {
			return false;
		}

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
		if (password == null || confirmPassword == null) {
			return false;
		}

		return password.equals(confirmPassword);
	}

	public String encryptPassword(String password) {
		byte[] salt = new byte[16];
		new SecureRandom().nextBytes(salt);

		return OpenBSDBCrypt.generate("2y", password.toCharArray(), salt, BCRYPT_COST);
	}

	public boolean checkPassword(String password, String hash) {
		if (password == null || hash == null || hash.isBlank()) {
			return false;
		}

		return OpenBSDBCrypt.checkPassword(hash, password.toCharArray());

	}
}
