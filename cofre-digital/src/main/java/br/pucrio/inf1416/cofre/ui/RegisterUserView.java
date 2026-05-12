package br.pucrio.inf1416.cofre.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class RegisterUserView extends JFrame {
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
		configureWindow();
		createComponents();
		buildLayout();
		configureMode();
	}

	private void configureWindow() {
		setTitle(mode == RegisterMode.INITIAL_ADMIN ? "Cadastro inicial de administrador" : "Cadastro de novo usuário");
		setSize(600, 420);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

	private void chooseFile(JTextField textField) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		int result = fileChooser.showOpenDialog(this);

		if (result == JFileChooser.APPROVE_OPTION) {
			textField.setText(fileChooser.getSelectedFile().getAbsolutePath());
		}
	}

	private void buildLayout() {
		JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
		JPanel formPanel = new JPanel(new GridBagLayout());

		mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		JLabel titleLabel = new JLabel(getTitle(), SwingConstants.CENTER);
		titleLabel.setFont(new Font("Arial", Font.BOLD, 18));

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.insets = new Insets(6, 6, 6, 6);
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;

		addRow(formPanel, gridBagConstraints, 0, "Caminho do certificado digital:",
				createFileFieldPanel(certificatePathField, certificateBrowseButton));
		addRow(formPanel, gridBagConstraints, 1, "Caminho da chave privada:",
				createFileFieldPanel(privateKeyPathField, privateKeyBrowseButton));
		addRow(formPanel, gridBagConstraints, 2, "Frase secreta:", secretPhraseField);
		addRow(formPanel, gridBagConstraints, 3, "Grupo:", groupComboBox);
		addRow(formPanel, gridBagConstraints, 4, "Senha:", passwordField);
		addRow(formPanel, gridBagConstraints, 5, "Confirmar senha:", confirmPasswordField);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(registerButton);
		buttonPanel.add(backButton);

		mainPanel.add(titleLabel, BorderLayout.NORTH);
		mainPanel.add(formPanel, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);

		setContentPane(mainPanel);
	}

	private void addRow(JPanel panel, GridBagConstraints gridBagConstraints, int row, String label, JComponent field) {
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = row;
		gridBagConstraints.weightx = 0;

		panel.add(new JLabel(label), gridBagConstraints);

		gridBagConstraints.gridx = 1;
		gridBagConstraints.weightx = 1;

		panel.add(field, gridBagConstraints);
	}

	private JPanel createFileFieldPanel(JTextField field, JButton button) {
		JPanel panel = new JPanel(new BorderLayout(5, 0));
		panel.add(field, BorderLayout.CENTER);
		panel.add(button, BorderLayout.EAST);

		return panel;
	}

	private void configureMode() {
		if (mode == RegisterMode.INITIAL_ADMIN) {
			groupComboBox.setSelectedItem("Administrador");
			groupComboBox.setEnabled(false);
			backButton.setVisible(false);
		}
	}
}
