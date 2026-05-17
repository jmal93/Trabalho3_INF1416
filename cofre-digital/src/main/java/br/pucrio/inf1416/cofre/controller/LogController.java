package br.pucrio.inf1416.cofre.controller;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;

import br.pucrio.inf1416.cofre.dao.LogDAO;
import br.pucrio.inf1416.cofre.dao.MessageDAO;
import br.pucrio.inf1416.cofre.model.LogRecord;
import br.pucrio.inf1416.cofre.service.CertificateService;

public class LogController {

	private final CertificateService certificateService;
	private final LogDAO logDAO;
	private final MessageDAO messageDAO;

	public LogController(CertificateService certificateService, LogDAO logDAO, MessageDAO messageDAO) {
		this.certificateService = certificateService;
		this.logDAO = logDAO;
		this.messageDAO = messageDAO;
	}

	public void authenticateAdminPrivateKey(String privateKeyPath, String secretPhrase) throws Exception {
		PrivateKey privateKey = certificateService.loadEncryptedPrivateKey(privateKeyPath, secretPhrase);

		if (privateKey == null) {
			throw new IllegalArgumentException("Chave privada inválida.");
		}
	}

	public List<String> getFormattedLogs() throws Exception {
		List<LogRecord> records = logDAO.findAllChronological();

		List<String> formatted = new ArrayList<>();

		for (LogRecord record : records) {
			String message = messageDAO.findTextById(record.getMid());

			String line = formatRecord(record, message);

			formatted.add(line);
		}

		return formatted;
	}

	public void printLogs() throws Exception {
		List<String> lines = getFormattedLogs();

		for (String line : lines) {
			System.out.println(line);
		}
	}

	private String formatRecord(LogRecord record, String message) {
		String uid = record.getUid() == null ? "-" : String.valueOf(record.getUid());

		String file = record.getFileName() == null || record.getFileName().isBlank() ? "-" : record.getFileName();

		return String.format("%s | MID=%d | UID=%s | ARQUIVO=%s | %s", record.getDateTime(), record.getMid(), uid, file,
				message);
	}
}
