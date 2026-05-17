package br.pucrio.inf1416.cofre.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import br.pucrio.inf1416.cofre.model.User;

public class MainMenuView extends JFrame {

	private final User user;

	private JButton registerUserButton;
	private JButton openVaultButton;
	private JButton exitButton;

	public MainMenuView(User user) {
		this.user = user;

		configureWindow();
		createComponents();
		buildLayout();
		configurePermissions();
	}

	private void configureWindow() {
		setTitle("Menu Principal - Cofre Digital");
		setSize(600, 420);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	private void createComponents() {
		registerUserButton = new JButton("Cadastrar novo usuário");
		openVaultButton = new JButton("Consultar pasta de arquivos secretos");
		exitButton = new JButton("Sair do sistema");
	}

	private void buildLayout() {
		JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		JLabel titleLabel = new JLabel("Menu Principal", SwingConstants.CENTER);
		titleLabel.setFont(new Font("Arial", Font.BOLD, 18));

		JPanel infoPanel = new JPanel(new GridLayout(4, 1));
		infoPanel.add(new JLabel("Login: " + user.getLogin()));
		infoPanel.add(new JLabel("Grupo: " + user.getGroupName()));
		infoPanel.add(new JLabel("Nome: " + user.getNome()));
		infoPanel.add(new JLabel("Total de acessos: " + user.getTotalAccesses()));

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(registerUserButton);
		buttonPanel.add(openVaultButton);
		buttonPanel.add(exitButton);

		mainPanel.add(titleLabel, BorderLayout.NORTH);
		mainPanel.add(infoPanel, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);

		setContentPane(mainPanel);
	}

	private void configurePermissions() {
		if (!"Administrador".equalsIgnoreCase(user.getGroupName())) {
			registerUserButton.setVisible(false);
		}
	}

	public void setRegisterUserAction(Runnable action) {
		registerUserButton.addActionListener(e -> action.run());
	}

	public void setOpenVaultAction(Runnable action) {
		openVaultButton.addActionListener(e -> action.run());
	}

	public void setExitAction(Runnable action) {
		exitButton.addActionListener(e -> action.run());
	}

	public void showMessage(String message) {
		javax.swing.JOptionPane.showMessageDialog(this, message);
	}
}
