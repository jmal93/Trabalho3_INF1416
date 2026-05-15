package br.pucrio.inf1416.cofre.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pucrio.inf1416.cofre.dao.UserDAO;
import br.pucrio.inf1416.cofre.model.User;

class AuthenticationServiceTest {

	private UserDAO userDAO;
	private PasswordService passwordService;
	private TOTPService totpService;
	private AuditService auditService;

	private AuthenticationService authenticationService;

	@BeforeEach
	void setUp() {
		userDAO = mock(UserDAO.class);
		passwordService = mock(PasswordService.class);
		totpService = mock(TOTPService.class);
		auditService = mock(AuditService.class);

		authenticationService = new AuthenticationService(userDAO, passwordService, totpService, auditService);
	}

	@Test
	void shouldValidateLoginWhenUserExistsAndIsNotBlocked() throws Exception {
		User user = createUser();

		when(userDAO.findByLogin("user02@inf1416.puc-rio.br")).thenReturn(user);

		User result = authenticationService.validateLogin("user02@inf1416.puc-rio.br");

		assertNotNull(result);
		assertEquals("user02@inf1416.puc-rio.br", result.getLogin());

		verify(userDAO).findByLogin("user02@inf1416.puc-rio.br");
		verify(auditService).log(2001);
		verify(auditService).log(2003, user);
		verify(auditService).log(2002, user);
	}

	@Test
	void shouldRejectLoginWhenEmailFormatIsInvalid() {
		Exception exception = assertThrows(IllegalArgumentException.class,
				() -> authenticationService.validateLogin("email-invalido"));

		assertTrue(exception.getMessage().contains("e-mail"));

		verifyNoInteractions(userDAO);
	}

	@Test
	void shouldRejectLoginWhenUserDoesNotExist() throws Exception {
		when(userDAO.findByLogin("inexistente@inf1416.puc-rio.br")).thenReturn(null);

		Exception exception = assertThrows(IllegalArgumentException.class,
				() -> authenticationService.validateLogin("inexistente@inf1416.puc-rio.br"));

		assertTrue(exception.getMessage().contains("Login não identificado"));

		verify(userDAO).findByLogin("inexistente@inf1416.puc-rio.br");
		verify(auditService, atLeastOnce()).log(2005);
	}

	@Test
	void shouldRejectLoginWhenUserIsBlocked() throws Exception {
		User user = createUser();
		user.setBlockedUntil(LocalDateTime.now().plusMinutes(1));

		when(userDAO.findByLogin(user.getLogin())).thenReturn(user);

		Exception exception = assertThrows(IllegalArgumentException.class,
				() -> authenticationService.validateLogin(user.getLogin()));

		assertTrue(exception.getMessage().contains("bloqueado"));

		verify(auditService).log(2004, user);
	}

	@Test
	void shouldValidatePasswordFromVirtualKeyboardPairs() throws Exception {
		User user = createUser();

		List<int[]> pressedPairs = List.of(new int[] { 1, 9 }, new int[] { 3, 9 }, new int[] { 5, 9 },
				new int[] { 7, 9 }, new int[] { 2, 9 }, new int[] { 4, 9 }, new int[] { 6, 9 }, new int[] { 8, 9 });

		when(passwordService.checkPassword(eq("13572468"), eq(user.getPasswordHash()))).thenReturn(true);

		String result = authenticationService.validatePasswordByPairs(user, pressedPairs);

		assertEquals("13572468", result);

		verify(passwordService, atLeastOnce()).checkPassword(anyString(), eq(user.getPasswordHash()));

		verify(auditService).log(3001, user);
		verify(auditService).log(3003, user);
		verify(auditService).log(3002, user);
	}

	@Test
	void shouldRejectPasswordWhenNumberOfPressedButtonsIsInvalid() {
		User user = createUser();

		List<int[]> pressedPairs = List.of(new int[] { 1, 2 }, new int[] { 3, 4 });

		Exception exception = assertThrows(IllegalArgumentException.class,
				() -> authenticationService.validatePasswordByPairs(user, pressedPairs));

		assertTrue(exception.getMessage().contains("8, 9 ou 10"));

		verifyNoInteractions(passwordService);
	}

	@Test
	void shouldBlockUserAfterThreePasswordErrors() throws Exception {
		User user = createUser();

		List<int[]> pressedPairs = List.of(new int[] { 1, 2 }, new int[] { 3, 4 }, new int[] { 5, 6 },
				new int[] { 7, 8 }, new int[] { 9, 0 }, new int[] { 1, 3 }, new int[] { 2, 4 }, new int[] { 5, 7 });

		when(passwordService.checkPassword(anyString(), eq(user.getPasswordHash()))).thenReturn(false);

		assertThrows(IllegalArgumentException.class,
				() -> authenticationService.validatePasswordByPairs(user, pressedPairs));

		assertThrows(IllegalArgumentException.class,
				() -> authenticationService.validatePasswordByPairs(user, pressedPairs));

		Exception thirdError = assertThrows(IllegalArgumentException.class,
				() -> authenticationService.validatePasswordByPairs(user, pressedPairs));

		assertTrue(thirdError.getMessage().contains("bloqueado"));
		assertNotNull(user.getBlockedUntil());
		assertTrue(user.getBlockedUntil().isAfter(LocalDateTime.now()));

		verify(userDAO).updateBlockedUntil(eq(user.getUid()), any(LocalDateTime.class));
		verify(auditService).log(3006, user);
		verify(auditService).log(3007, user);
	}

	@Test
	void shouldValidateTotpWhenTokenIsCorrect() throws Exception {
		User user = createUser();
		String validatedPassword = "13572468";
		String token = "123456";

		when(totpService.validateToken(user, token, validatedPassword)).thenReturn(true);

		assertDoesNotThrow(() -> authenticationService.validateTotp(user, token, validatedPassword));

		verify(totpService).validateToken(user, token, validatedPassword);
		verify(auditService).log(4001, user);
		verify(auditService).log(4003, user);
		verify(auditService).log(4002, user);
	}

	@Test
	void shouldRejectTotpWhenTokenHasInvalidFormat() {
		User user = createUser();

		Exception exception = assertThrows(IllegalArgumentException.class,
				() -> authenticationService.validateTotp(user, "ABC", "13572468"));

		assertTrue(exception.getMessage().contains("6 dígitos"));

		verifyNoInteractions(totpService);
	}

	@Test
	void shouldBlockUserAfterThreeTotpErrors() throws Exception {
		User user = createUser();

		when(totpService.validateToken(eq(user), anyString(), eq("13572468"))).thenReturn(false);

		assertThrows(IllegalArgumentException.class,
				() -> authenticationService.validateTotp(user, "111111", "13572468"));

		assertThrows(IllegalArgumentException.class,
				() -> authenticationService.validateTotp(user, "222222", "13572468"));

		Exception thirdError = assertThrows(IllegalArgumentException.class,
				() -> authenticationService.validateTotp(user, "333333", "13572468"));

		assertTrue(thirdError.getMessage().contains("bloqueado"));
		assertNotNull(user.getBlockedUntil());
		assertTrue(user.getBlockedUntil().isAfter(LocalDateTime.now()));

		verify(userDAO).updateBlockedUntil(eq(user.getUid()), any(LocalDateTime.class));
		verify(auditService).log(4006, user);
		verify(auditService).log(4007, user);
	}

	@Test
	void shouldRegisterSuccessfulAccess() throws Exception {
		User user = createUser();
		user.setTotalAccesses(4);

		authenticationService.registerSuccessfulAccess(user);

		assertEquals(5, user.getTotalAccesses());

		verify(userDAO).incrementAccessCount(1);
		verify(auditService).log(1003, user);
	}

	private User createUser() {
		User user = new User();

		user.setUid(1);
		user.setLogin("user02@inf1416.puc-rio.br");
		user.setNome("Usuario 02");
		user.setGid(2);
		user.setGroupName("Usuário");
		user.setPasswordHash("$2y$08$abcdefghijklmnopqrstuuV8hU7vN1yLw1qJfakehashvalue");
		user.setEncryptedTOTPSecret(new byte[] { 1, 2, 3 });
		user.setBlockedUntil(null);
		user.setTotalAccesses(0);
		user.setTotalQueries(0);

		return user;
	}
}
