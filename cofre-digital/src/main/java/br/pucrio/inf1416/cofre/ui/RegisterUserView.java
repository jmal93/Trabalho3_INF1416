package br.pucrio.inf1416.cofre.ui;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class RegisterUserView extends BaseFormView {
	public enum RegisterMode {
		INITIAL_ADMIN, NEW_USER
	}

	private final RegisterMode mode;

	private JTextField certificatePathField;
	private JButton certificateBrowseButton;
	private JTextField privateKeyPathField;
	private JButton privateKeyBrowseButton;
	private JPasswordField secretPhraseField;
	private JComboBox<String> groupComboBox;
	private JPasswordField passwordField;
	private JPasswordField confirmPasswordField;
	private JButton registerButton;
	private JButton backButton;

	public RegisterUserView(RegisterMode mode) {
		this.mode = mode;

		String titleString = mode == RegisterMode.INITIAL_ADMIN ? "Cadastro inicial de administrador"
				: "Cadastro de novo usuário";
		configureWindow(titleString, 600, 420);
		createComponents();
		initializeBaseLayout(titleString);
		buildLayout();
		configureMode();
	}

	private void createComponents() {
		certificatePathField = new JTextField(30);
		certificatePathField.setEditable(false);
		certificateBrowseButton = new JButton("Procurar...");
		certificateBrowseButton.addActionListener(e -> chooseFile(certificatePathField));

		privateKeyPathField = new JTextField(30);
		privateKeyPathField.setEditable(false);
		privateKeyBrowseButton = new JButton("Procurar...");
		privateKeyBrowseButton.addActionListener(e -> chooseFile(privateKeyPathField));

		secretPhraseField = new JPasswordField(30);
		groupComboBox = new JComboBox<String>(new String[] { "Administrador", "Usuário" });
		passwordField = new JPasswordField(10);
		confirmPasswordField = new JPasswordField(10);

		registerButton = new JButton(mode == RegisterMode.INITIAL_ADMIN ? "Cadastrar administrador" : "Cadastrar");
		backButton = new JButton("Voltar ao menu principal");
	}

	private void buildLayout() {
		addRow(0, "Caminho do certificado digital:",
				createFileFieldPanel(certificatePathField, certificateBrowseButton));
		addRow(1, "Caminho da chave privada:", createFileFieldPanel(privateKeyPathField, privateKeyBrowseButton));
		addRow(2, "Frase secreta:", secretPhraseField);
		addRow(3, "Grupo:", groupComboBox);
		addRow(5, "Senha:", passwordField);
		addRow(6, "Confirmar senha:", confirmPasswordField);

		buttonPanel.add(registerButton);
		buttonPanel.add(backButton);
	}

	private void configureMode() {
		if (mode == RegisterMode.INITIAL_ADMIN) {
			groupComboBox.setSelectedItem("Administrador");
			groupComboBox.setEnabled(false);
			backButton.setVisible(false);
		}
	}

	public String getCertificatePath() {
		return certificatePathField.getText().trim();
	}

	public String getPrivateKeyPath() {
		return privateKeyPathField.getText().trim();
	}

	public String getSecretPhrase() {
		return new String(secretPhraseField.getPassword());
	}

	public String getSelectedGroup() {
		return (String) groupComboBox.getSelectedItem();
	}

	public String getPassword() {
		return new String(passwordField.getPassword());
	}

	public String getConfirmPassword() {
		return new String(confirmPasswordField.getPassword());
	}

	public RegisterMode getMode() {
		return mode;
	}

	public void setRegisterAction(Runnable action) {
		registerButton.addActionListener(e -> action.run());
	}

	public void setBackAction(Runnable action) {
		backButton.addActionListener(e -> action.run());
	}

	public void showMessage(String message) {
		JOptionPane.showMessageDialog(this, message);
	}

	public boolean showCertificateConfirmation(String certificateInfo) {
		int option = JOptionPane.showConfirmDialog(this, certificateInfo, "Confirmação do certificado",
				JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

		return option == JOptionPane.YES_OPTION;
	}

	public void showTotpSecret(String login, String base32Secret) {
		JOptionPane.showMessageDialog(this,
				"Usuário cadastrado com sucesso.\n\n" + "Configure o Google Authenticator com os dados abaixo:\n\n"
						+ "Conta: " + login + "\n" + "Segredo: " + base32Secret,
				"Segredo TOTP", JOptionPane.INFORMATION_MESSAGE);
	}

	public void showTotpQRCode(String login, String base32Secret, BufferedImage qrCodeImage) {
		JLabel qrCodeLabel = new JLabel(new ImageIcon(qrCodeImage));

		JTextArea textArea = new JTextArea("Usuário cadastrado com sucesso.\n\n"
				+ "Escaneie o QR Code abaixo no Google Authenticator.\n\n" + "Conta: " + login + "\n" + "Chave manual: "
				+ base32Secret + "\n\n" + "Guarde a chave manual apenas para teste/recuperação.");

		textArea.setEditable(false);
		textArea.setOpaque(false);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);

		JPanel panel = new JPanel(new BorderLayout(10, 10));
		panel.add(textArea, BorderLayout.NORTH);
		panel.add(qrCodeLabel, BorderLayout.CENTER);

		JOptionPane.showMessageDialog(this, panel, "Configurar Google Authenticator", JOptionPane.INFORMATION_MESSAGE);
	}

	public void clearForm() {
		certificatePathField.setText("");
		privateKeyPathField.setText("");
		secretPhraseField.setText("");
		passwordField.setText("");
		confirmPasswordField.setText("");

		if (mode == RegisterMode.NEW_USER) {
			groupComboBox.setSelectedIndex(0);
		}
	}
}
