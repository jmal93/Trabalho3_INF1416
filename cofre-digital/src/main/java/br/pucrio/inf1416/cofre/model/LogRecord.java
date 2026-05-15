package br.pucrio.inf1416.cofre.model;

import java.time.LocalDateTime;

public class LogRecord {
	private int rid;
	private LocalDateTime dateTime;

	public int getRid() {
		return rid;
	}

	public void setRid(int rid) {
		this.rid = rid;
	}

	public LocalDateTime getDateTime() {
		return dateTime;
	}

	public void setDateTime(LocalDateTime dateTime) {
		this.dateTime = dateTime;
	}

	public int getMid() {
		return mid;
	}

	public void setMid(int mid) {
		this.mid = mid;
	}

	public Integer getUid() {
		return uid;
	}

	public void setUid(Integer uid) {
		this.uid = uid;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	private int mid;
	private Integer uid;
	private String fileName;

	public LogRecord() {
	}

	public LogRecord(LocalDateTime dateTime, int mid, Integer uid, String fileName) {
		this.dateTime = dateTime;
		this.mid = mid;
		this.uid = uid;
		this.fileName = fileName;
	}
}
