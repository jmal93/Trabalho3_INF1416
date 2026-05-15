package br.pucrio.inf1416.cofre.service;

import java.time.LocalDateTime;

import br.pucrio.inf1416.cofre.dao.LogDAO;
import br.pucrio.inf1416.cofre.model.LogRecord;
import br.pucrio.inf1416.cofre.model.User;

public class AuditService {
	private final LogDAO logDAO;

	public AuditService(LogDAO logDAO) {
		this.logDAO = logDAO;
	}

	public void log(int messageId) throws Exception {
		LogRecord logRecord = new LogRecord(LocalDateTime.now(), messageId, null, null);
		logDAO.insert(logRecord);
	}

	public void log(int messageId, User user) throws Exception {
		Integer uid = user == null ? null : user.getUid();
		LogRecord logRecord = new LogRecord(LocalDateTime.now(), messageId, uid, null);
		logDAO.insert(logRecord);
	}

	public void log(int messageId, User user, String filename) throws Exception {
		Integer uid = user == null ? null : user.getUid();
		LogRecord logRecord = new LogRecord(LocalDateTime.now(), messageId, uid, filename);
		logDAO.insert(logRecord);
	}
}
