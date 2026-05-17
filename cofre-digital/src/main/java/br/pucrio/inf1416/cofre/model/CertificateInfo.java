package br.pucrio.inf1416.cofre.model;

import java.security.cert.X509Certificate;

public record CertificateInfo(X509Certificate certificate, String version, String serialNumber, String validity,
		String signatureType, String issuer, String subject, String name, String email) {
	public String toDisplayText() {
		return """
				Versão: %s
				Série: %s
				Validade: %s
				Tipo de assinatura: %s
				Emissor: %s
				Sujeito: %s
				Nome: %s
				E-Mail: %s
				""".formatted(version, serialNumber, validity, signatureType, issuer, subject, name, email);
	}
}
