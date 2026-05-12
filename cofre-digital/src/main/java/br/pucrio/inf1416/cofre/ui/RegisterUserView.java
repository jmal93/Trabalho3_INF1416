package br.pucrio.inf1416.cofre.ui;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPasswordField;
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
}
