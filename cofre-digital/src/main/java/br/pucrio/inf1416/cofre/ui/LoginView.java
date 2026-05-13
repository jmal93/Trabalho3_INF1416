package br.pucrio.inf1416.cofre.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class LoginView extends JFrame {

	private CardLayout cardLayout;
	private JPanel cards;

	private JTextField emailField;
	private JButton loginButton;

	private JPasswordField visualPasswordField;
	private JPanel keypadPanel;
	private JButton confirmPasswordButton;
	private JButton clearPasswordButton;

	private JTextField tokenField;
	private JButton confirmTokenButton;

	private final List<int[]> pressedPairs = new ArrayList<>();

	public LoginView() {
		configureWindow();
		createComponents();
		buildLayout();
	}

	private void configureWindow() {
		setTitle("Autenticação - Cofre Digital");
		setSize(600, 420);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	private void createComponents() {
		emailField = new JTextField(30);
		loginButton = new JButton("Continuar");

		visualPasswordField = new JPasswordField(10);
		visualPasswordField.setEditable(false);

		keypadPanel = new JPanel(new GridLayout(1, 5, 8, 8));
		confirmPasswordButton = new JButton("Confirmar");
		clearPasswordButton = new JButton("Limpar");

		tokenField = new JTextField(6);
		confirmTokenButton = new JButton("Confirmar");

		generateVirtualKeyboard();
	}

	private void buildLayout() {
		cardLayout = new CardLayout();
		cards = new JPanel(cardLayout);

		cards.add(buildLoginStep(), "LOGIN");
		cards.add(buildPasswordStep(), "PASSWORD");
		cards.add(buildTotpStep(), "TOTP");

		setContentPane(cards);
	}

	private JPanel buildLoginStep() {
		JPanel panel = basePanel("Etapa 1 - Login");

		JPanel form = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = defaultConstraints();

		addRow(form, gbc, 0, "Login/e-mail:", emailField);

		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttons.add(loginButton);

		panel.add(form, BorderLayout.CENTER);
		panel.add(buttons, BorderLayout.SOUTH);

		return panel;
	}

	private JPanel buildPasswordStep() {
		JPanel panel = basePanel("Etapa 2 - Senha pessoal");

		JPanel center = new JPanel(new BorderLayout(10, 10));

		JPanel passwordPanel = new JPanel(new FlowLayout());
		passwordPanel.add(new JLabel("Senha:"));
		passwordPanel.add(visualPasswordField);

		center.add(passwordPanel, BorderLayout.NORTH);
		center.add(keypadPanel, BorderLayout.CENTER);

		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttons.add(clearPasswordButton);
		buttons.add(confirmPasswordButton);

		panel.add(center, BorderLayout.CENTER);
		panel.add(buttons, BorderLayout.SOUTH);

		return panel;
	}

	private JPanel buildTotpStep() {
		JPanel panel = basePanel("Etapa 3 - Token Google Authenticator");

		JPanel form = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = defaultConstraints();

		addRow(form, gbc, 0, "Código TOTP:", tokenField);

		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttons.add(confirmTokenButton);

		panel.add(form, BorderLayout.CENTER);
		panel.add(buttons, BorderLayout.SOUTH);

		return panel;
	}

	private JPanel basePanel(String title) {
		JPanel panel = new JPanel(new BorderLayout(10, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
		titleLabel.setFont(new Font("Arial", Font.BOLD, 18));

		panel.add(titleLabel, BorderLayout.NORTH);

		return panel;
	}

	private GridBagConstraints defaultConstraints() {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(6, 6, 6, 6);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		return gbc;
	}

	private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.weightx = 0;
		panel.add(new JLabel(label), gbc);

		gbc.gridx = 1;
		gbc.weightx = 1;
		panel.add(field, gbc);
	}

	public void generateVirtualKeyboard() {
		keypadPanel.removeAll();

		List<Integer> digits = new ArrayList<>();

		for (int i = 0; i <= 9; i++) {
			digits.add(i);
		}

		Collections.shuffle(digits);

		for (int i = 0; i < 10; i += 2) {
			int first = digits.get(i);
			int second = digits.get(i + 1);

			JButton button = new JButton(first + " ou " + second);

			button.addActionListener(e -> {
				pressedPairs.add(new int[] { first, second });
				updateVisualPassword();
				generateVirtualKeyboard();
			});

			keypadPanel.add(button);
		}

		keypadPanel.revalidate();
		keypadPanel.repaint();
	}

	private void updateVisualPassword() {
		visualPasswordField.setText("*".repeat(pressedPairs.size()));
	}

	public String getEmail() {
		return emailField.getText().trim();
	}

	public List<int[]> getPressedPairs() {
		return new ArrayList<>(pressedPairs);
	}

	public String getToken() {
		return tokenField.getText().trim();
	}

	public void clearPasswordInput() {
		pressedPairs.clear();
		visualPasswordField.setText("");
		generateVirtualKeyboard();
	}

	public void clearTokenInput() {
		tokenField.setText("");
	}

	public void showLoginStep() {
		cardLayout.show(cards, "LOGIN");
	}

	public void showPasswordStep() {
		clearPasswordInput();
		cardLayout.show(cards, "PASSWORD");
	}

	public void showTotpStep() {
		clearTokenInput();
		cardLayout.show(cards, "TOTP");
	}

	public void setLoginAction(Runnable action) {
		loginButton.addActionListener(e -> action.run());
	}

	public void setPasswordConfirmAction(Runnable action) {
		confirmPasswordButton.addActionListener(e -> action.run());
	}

	public void setPasswordClearAction(Runnable action) {
		clearPasswordButton.addActionListener(e -> clearPasswordInput());
	}

	public void setTokenConfirmAction(Runnable action) {
		confirmTokenButton.addActionListener(e -> action.run());
	}

	public void showMessage(String message) {
		JOptionPane.showMessageDialog(this, message);
	}
}
