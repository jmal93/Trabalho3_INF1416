package br.pucrio.inf1416.cofre.model;

public class KeyPairRecord {
	private int kid;
	private int uid;
	private String certificatePem;
	private byte[] encryptedPrivateKey;

	public int getKid() {
		return kid;
	}

	public void setKid(int kid) {
		this.kid = kid;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public String getCertificatePem() {
		return certificatePem;
	}

	public void setCertificatePem(String certificatePem) {
		this.certificatePem = certificatePem;
	}

	public byte[] getEncryptedPrivateKey() {
		return encryptedPrivateKey;
	}

	public void setEncryptedPrivateKey(byte[] encryptedPrivateKey) {
		this.encryptedPrivateKey = encryptedPrivateKey;
	}
}
