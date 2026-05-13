package br.pucrio.inf1416.cofre.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PasswordServiceTest {

	private PasswordService passwordService;

	@BeforeEach
	void setUp() throws Exception {
		passwordService = new PasswordService();
	}

	@Test
	void passwordShouldHaveLengthOf8To10() {
		boolean result = passwordService.validPassword("12345678");
		assertTrue(result);

		result = passwordService.validPassword("1");
		assertFalse(result);

		result = passwordService.validPassword("12345678901");
		assertFalse(result);
	}

	@Test
	void passwordShouldNotHaveRepeatingCharactersInSequence() {
		boolean result = passwordService.validPassword("112345678");
		assertFalse(result);

		result = passwordService.validPassword("234511678");
		assertFalse(result);

		result = passwordService.validPassword("234567811");
		assertFalse(result);
	}

	@Test
	void passwordShouldHaveOnlyDigits() {
		boolean result = passwordService.validPassword("a123456677");
		assertFalse(result);
	}

	@Test
	void passwordAndConfirmPasswordShouldBeEquals() {
		boolean result = passwordService.passwordsAreEqual("12345678", "12345678");
		assertTrue(result);

		result = passwordService.passwordsAreEqual("123456789", "12345678");
		assertFalse(result);
	}
}
