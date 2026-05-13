package br.pucrio.inf1416.cofre.service;

public class PasswordService {
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
}
