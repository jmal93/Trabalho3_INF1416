package br.pucrio.inf1416.cofre.service;

import java.awt.image.BufferedImage;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class QRCodeService {

	private static final String ISSUER = "Cofre Digital PUC-Rio";

	public String buildTotpUri(String login, String base32Secret) {
		String encodedIssuer = urlEncode(ISSUER);
		String encodedAccount = urlEncode(login);

		return "otpauth://totp/" + encodedIssuer + ":" + encodedAccount + "?secret=" + base32Secret + "&issuer="
				+ encodedIssuer + "&algorithm=SHA1" + "&digits=6" + "&period=30";
	}

	public BufferedImage generateQRCodeImage(String text, int width, int height) throws Exception {
		QRCodeWriter qrCodeWriter = new QRCodeWriter();

		Map<EncodeHintType, Object> hints = new HashMap<>();
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

		BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);

		return MatrixToImageWriter.toBufferedImage(bitMatrix);
	}

	private String urlEncode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}
}
