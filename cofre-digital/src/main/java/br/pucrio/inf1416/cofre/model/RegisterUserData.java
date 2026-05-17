package br.pucrio.inf1416.cofre.model;

public record RegisterUserData(String certificatePath, String privateKeyPath, String secretPhrase, String groupName,
		String password, String confirmPassword) {
}
