package br.pucrio.inf1416.cofre.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.pucrio.inf1416.cofre.dao.UserDAO;
import br.pucrio.inf1416.cofre.model.User;

public class AuthenticationService {
	private static final int BLOCK_MINUTES = 2;

	private final UserDAO userDAO;
	private final PasswordService passwordService;
	private final TOTPService totpService;
	private final AuditService auditService;

	private final Map<Integer, Integer> passwordErrors = new HashMap<Integer, Integer>();
	private final Map<Integer, Integer> tokenErrors = new HashMap<Integer, Integer>();

	public AuthenticationService(UserDAO userDAO, PasswordService passwordService, TOTPService totpService,
			AuditService auditService) {
		this.userDAO = userDAO;
		this.passwordService = passwordService;
		this.totpService = totpService;
		this.auditService = auditService;
	}

	public User validateLogin(String login) throws Exception {
		auditService.log(2001);

		if (login == null || login.isBlank()) {
			auditService.log(2005);
			throw new IllegalArgumentException("Informe o login/e-mail");
		}

		login = login.trim();

		if (!isValidEmail(login)) {
			auditService.log(2005);
			throw new IllegalArgumentException("Login deve ser um e-mail válido");
		}

		User user = userDAO.findByLogin(login);

		if (user == null) {
			auditService.log(2005);
			throw new IllegalArgumentException("Login não identificado");
		}

		if (isUserBlocked(user)) {
			auditService.log(2004, user);
			throw new IllegalArgumentException("Usuário bloqueado temporariamente. Tente novamente");
		}

		auditService.log(2003, user);
		auditService.log(2002, user);

		return user;
	}

	private boolean isValidEmail(String email) {
		return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
	}

	public boolean isUserBlocked(User user) {
		if (user == null || user.getBlockedUntil() == null) {
			return false;
		}

		return user.getBlockedUntil().isAfter(LocalDateTime.now());
	}

	public String validatePasswordByPairs(User user, List<int[]> pressedPairs) throws Exception {
		if (user == null) {
			throw new IllegalStateException("Nenhum usuário selecionado para autenticação");
		}

		auditService.log(3001, user);

		if (pressedPairs == null || pressedPairs.size() < 8 || pressedPairs.size() > 10) {
			registerPasswordError(user);
			throw new IllegalArgumentException("A senha pessoal deve ter 8, 9 ou 10 dígitos");
		}

		List<String> candidates = generatePasswordCandidates(pressedPairs);

		for (String candidate : candidates) {
			if (passwordService.checkPassword(candidate, user.getPasswordHash())) {
				resetPasswordErrors(user);
				auditService.log(3003, user);
				auditService.log(3002, user);
				return candidate;
			}
		}

		registerPasswordError(user);

		if (isUserBlocked(user)) {
			auditService.log(3007, user);
			throw new IllegalArgumentException("Senha incorreta. Usuário bloqueado por 2 minutos");
		}

		throw new IllegalArgumentException("Senha pessoal inválida");
	}

	private void registerPasswordError(User user) throws Exception {
		int errors = passwordErrors.getOrDefault(user.getUid(), 0) + 1;
		passwordErrors.put(user.getUid(), errors);

		if (errors == 1) {
			auditService.log(3004, user);
		} else if (errors == 2) {
			auditService.log(3005, user);
		} else if (errors >= 3) {
			auditService.log(3006, user);
			blockUserForTwoMinutes(user);
			passwordErrors.remove(user.getUid());
		}
	}

	private void blockUserForTwoMinutes(User user) throws Exception {
		LocalDateTime blockerUntil = LocalDateTime.now().plusMinutes(BLOCK_MINUTES);

		userDAO.updateBlockedUntil(user.getUid(), blockerUntil);

		user.setBlockedUntil(blockerUntil);
	}

	private List<String> generatePasswordCandidates(List<int[]> pressedPairs) {
		List<String> candidates = new ArrayList<String>();

		generatePasswordCandidatesRecursive(pressedPairs, 0, new StringBuilder(), candidates);

		return candidates;
	}

	private void generatePasswordCandidatesRecursive(List<int[]> pairs, int index, StringBuilder current,
			List<String> candidates) {
		if (index == pairs.size()) {
			candidates.add(current.toString());
			return;
		}

		int[] pair = pairs.get(index);

		current.append(pair[0]);
		generatePasswordCandidatesRecursive(pairs, index + 1, current, candidates);
		current.deleteCharAt(current.length() - 1);

		current.append(pair[1]);
		generatePasswordCandidatesRecursive(pairs, index + 1, current, candidates);
		current.deleteCharAt(current.length() - 1);
	}

	private void resetPasswordErrors(User user) {
		passwordErrors.remove(user.getUid());
	}

	public void validateTotp(User user, String inputToken, String validatedPassword) throws Exception {
		if (user == null) {
			throw new IllegalStateException("Nenhum usuário selecionado para autenticação");
		}

		if (validatedPassword == null || validatedPassword.isBlank()) {
			throw new IllegalStateException("Senha pessoal não validada");
		}

		auditService.log(4001, user);

		if (inputToken == null || !inputToken.matches("\\d{6}")) {
			registerTokenError(user);

			if (isUserBlocked(user)) {
				auditService.log(4007, user);
				throw new IllegalArgumentException("Token inválido. Usuário bloqueado por 2 minutos");
			}

			throw new IllegalArgumentException("Token deve conter exatamente 6 dígitos");
		}

		boolean valid = totpService.validateToken(user, inputToken, validatedPassword);

		if (!valid) {
			registerTokenError(user);

			if (isUserBlocked(user)) {
				auditService.log(4007, user);
				throw new IllegalArgumentException("Token inválido. Usuário bloqueado por 2 minutos");
			}

			throw new IllegalArgumentException("Token inválido");
		}

		resetTokenErrors(user);
		auditService.log(4003, user);
		auditService.log(4002, user);
	}

	private void registerTokenError(User user) throws Exception {
		int errors = tokenErrors.getOrDefault(user.getUid(), 0) + 1;
		tokenErrors.put(user.getUid(), errors);

		if (errors == 1) {
			auditService.log(4004, user);
		} else if (errors == 2) {
			auditService.log(4005, user);
		} else if (errors >= 3) {
			auditService.log(4006, user);
			blockUserForTwoMinutes(user);
			tokenErrors.remove(user.getUid());
		}
	}

	private void resetTokenErrors(User user) {
		tokenErrors.remove(user.getUid());
	}

	public void registerSuccessfulAccess(User user) throws Exception {
		userDAO.incrementAccessCount(user.getUid());

		user.setTotalAccesses(user.getTotalAccesses() + 1);

		auditService.log(1003, user);
	}
}
