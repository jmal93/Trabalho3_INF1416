package br.pucrio.inf1416.cofre.ui;

import javax.swing.JButton;
import javax.swing.JTextField;

public class LoginView extends BaseFormView {

	private JTextField emaiLabel;
	private JButton okButton;

	public LoginView() {
		configureWindow("Login", 600, 420);
		createComponents();
		initializeBaseLayout("Login");
		buildLayout();
	}

	private void createComponents() {
		emaiLabel = new JTextField(30);
		okButton = new JButton("OK");
	}

	private void buildLayout() {
		addRow(0, "Email:", emaiLabel);
		buttonPanel.add(okButton);
	}
}
