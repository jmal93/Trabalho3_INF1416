package br.pucrio.inf1416.cofre.service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.SecretKey;

import br.pucrio.inf1416.cofre.dao.KeyringDAO;
import br.pucrio.inf1416.cofre.model.CertificateInfo;
import br.pucrio.inf1416.cofre.model.KeyPairRecord;
import br.pucrio.inf1416.cofre.model.SecretFileEntry;
import br.pucrio.inf1416.cofre.model.User;

public class VaultService {

	private final CryptoService cryptoService;
	private final CertificateService certificateService;
	private final KeyringDAO keyringDAO;
	private final AuditService auditService;

	public VaultService(CryptoService cryptoService, CertificateService certificateService, KeyringDAO keyringDAO,
			AuditService auditService) {
		this.cryptoService = cryptoService;
		this.certificateService = certificateService;
		this.keyringDAO = keyringDAO;
		this.auditService = auditService;
	}

	public List<SecretFileEntry> listVisibleFiles(User user, Path folderPath, String secretPhrase) throws Exception {
		validateFolder(folderPath);

		auditService.log(7001, user);

		byte[] indexBytes = decryptProtectedFile(folderPath, "index", user, secretPhrase);

		List<SecretFileEntry> allEntries = parseIndex(indexBytes);

		List<SecretFileEntry> visibleEntries = filterVisibleFiles(user, allEntries);

		auditService.log(7002, user);

		return visibleEntries;
	}

	public void decryptSelectedFile(User user, Path folderPath, SecretFileEntry entry, String secretPhrase)
			throws Exception {

		if (entry == null) {
			throw new IllegalArgumentException("Nenhum arquivo selecionado.");
		}

		validateFolder(folderPath);

		auditService.log(7010, user, entry.getCodeName());

		if (!canDecryptFile(user, entry)) {
			auditService.log(7011, user, entry.getCodeName());
			throw new IllegalArgumentException("Você não tem permissão para descriptografar este arquivo.");
		}

		byte[] decryptedBytes = decryptProtectedFile(folderPath, entry.getCodeName(), user, secretPhrase);

		Path outputPath = folderPath.resolve(entry.getSecretName());

		Files.write(outputPath, decryptedBytes);

		auditService.log(7013, user, entry.getCodeName());
	}

	private byte[] decryptProtectedFile(Path folderPath, String baseName, User user, String secretPhrase)
			throws Exception {
		Path encPath = folderPath.resolve(baseName + ".enc");
		Path envPath = folderPath.resolve(baseName + ".env");
		Path asdPath = folderPath.resolve(baseName + ".asd");

		if (!Files.exists(encPath)) {
			throw new IllegalArgumentException("Arquivo não encontrado: " + encPath.getFileName());
		}

		if (!Files.exists(envPath)) {
			throw new IllegalArgumentException("Envelope não encontrado: " + envPath.getFileName());
		}

		if (!Files.exists(asdPath)) {
			throw new IllegalArgumentException("Assinatura não encontrada: " + asdPath.getFileName());
		}

		KeyPairRecord keyPairRecord = keyringDAO.findByUserId(user.getUid());

		if (keyPairRecord == null) {
			throw new IllegalArgumentException("Chaveiro do usuário não encontrado.");
		}

		CertificateInfo certificateInfo = certificateService.loadCertificateFromPem(keyPairRecord.getCertificatePem());

		X509Certificate certificate = certificateInfo.certificate();

		PrivateKey privateKey = certificateService.loadEncryptedPrivateKey(keyPairRecord.getEncryptedPrivateKey(),
				secretPhrase);

		boolean keyPairValid = certificateService.verifyKeyPair(privateKey, certificate);

		if (!keyPairValid) {
			throw new IllegalArgumentException("Frase secreta inválida ou chave privada incompatível.");
		}

		byte[] encryptedSeed = Files.readAllBytes(envPath);

		byte[] seedBytes = cryptoService.decryptWithPrivateKey(encryptedSeed, privateKey);

		SecretKey aesKey = cryptoService.generateAESKeyFromSeed(seedBytes);

		byte[] encryptedContent = Files.readAllBytes(encPath);

		byte[] decryptedContent = cryptoService.decryptWithAESKey(encryptedContent, aesKey);

		byte[] signatureBytes = readPossiblyBase64File(asdPath);

		PublicKey publicKey = certificate.getPublicKey();

		System.out.println("Algoritmo do certificado: " + certificate.getSigAlgName());

		boolean signatureValidOverEncrypted = cryptoService.verifySignatureWithAlgorithms(encryptedContent,
				signatureBytes, publicKey);

		boolean signatureValidOverDecrypted = cryptoService.verifySignatureWithAlgorithms(decryptedContent,
				signatureBytes, publicKey);

		System.out.println("Assinatura sobre .enc válida? " + signatureValidOverEncrypted);
		System.out.println("Assinatura sobre conteúdo decriptado válida? " + signatureValidOverDecrypted);

		if (!signatureValidOverEncrypted && !signatureValidOverDecrypted) {
			throw new IllegalArgumentException("Assinatura digital inválida.");
		}

		return decryptedContent;
	}

	private void validateFolder(Path folderPath) {
		if (folderPath == null) {
			throw new IllegalArgumentException("Pasta não informada.");
		}

		if (!Files.exists(folderPath)) {
			throw new IllegalArgumentException("Pasta não existe.");
		}

		if (!Files.isDirectory(folderPath)) {
			throw new IllegalArgumentException("O caminho informado não é uma pasta.");
		}
	}

	private List<SecretFileEntry> parseIndex(byte[] indexBytes) {
		String content = new String(indexBytes, StandardCharsets.UTF_8);

		List<SecretFileEntry> entries = new ArrayList<>();

		String[] lines = content.split("\\R");

		for (String line : lines) {
			if (line == null || line.isBlank()) {
				continue;
			}

			String[] parts = line.trim().split("\\s+");

			if (parts.length < 4) {
				continue;
			}

			SecretFileEntry entry = new SecretFileEntry();
			entry.setCodeName(parts[0]);
			entry.setSecretName(parts[1]);
			entry.setOwner(parts[2]);
			entry.setGroupName(parts[3]);

			entries.add(entry);
		}

		return entries;
	}

	private List<SecretFileEntry> filterVisibleFiles(User user, List<SecretFileEntry> entries) {
		List<SecretFileEntry> result = new ArrayList<>();

		for (SecretFileEntry entry : entries) {
			if (isVisibleToUser(user, entry)) {
				result.add(entry);
			}
		}

		return result;
	}

	private boolean isVisibleToUser(User user, SecretFileEntry entry) {
		if (user.getLogin().equalsIgnoreCase(entry.getOwner())) {
			return true;
		}

		return user.getGroupName() != null && user.getGroupName().equalsIgnoreCase(entry.getGroupName());
	}

	private boolean canDecryptFile(User user, SecretFileEntry entry) {
		return user.getLogin().equalsIgnoreCase(entry.getOwner());
	}

	private byte[] readPossiblyBase64File(Path path) throws Exception {
		byte[] rawBytes = Files.readAllBytes(path);

		String text = new String(rawBytes, StandardCharsets.UTF_8).trim();

		if (text.matches("[A-Za-z0-9+/=\\r\\n\\s]+")) {
			try {
				return java.util.Base64.getDecoder().decode(text.replaceAll("\\s", ""));
			} catch (IllegalArgumentException ignored) {
				return rawBytes;
			}
		}

		return rawBytes;
	}
}
