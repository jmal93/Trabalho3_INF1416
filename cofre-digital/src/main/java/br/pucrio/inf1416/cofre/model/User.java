package br.pucrio.inf1416.cofre.model;

import java.time.LocalDateTime;

public class User {
	private int uid;
	private String login;
	private String nome;
	private int gid;
	private String groupName;
	private String passwordHash;
	private byte[] encryptedTOTPSecret;
	private LocalDateTime blockedUntil;
	private int totalAccesses;
	private int totalQueries;

	public User() {

	}

	public User(int uid, String login, String nome) {
		this.uid = uid;
		this.login = login;
		this.nome = nome;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public int getGid() {
		return gid;
	}

	public void setGid(int gid) {
		this.gid = gid;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public byte[] getEncryptedTOTPSecret() {
		return encryptedTOTPSecret;
	}

	public void setEncryptedTOTPSecret(byte[] encryptedTOTPSecret) {
		this.encryptedTOTPSecret = encryptedTOTPSecret;
	}

	public LocalDateTime getBlockedUntil() {
		return blockedUntil;
	}

	public void setBlockedUntil(LocalDateTime blockedUntil) {
		this.blockedUntil = blockedUntil;
	}

	public int getTotalAccesses() {
		return totalAccesses;
	}

	public void setTotalAccesses(int totalAcess) {
		this.totalAccesses = totalAcess;
	}

	public int getTotalQueries() {
		return totalQueries;
	}

	public void setTotalQueries(int totalQueries) {
		this.totalQueries = totalQueries;
	}

}
