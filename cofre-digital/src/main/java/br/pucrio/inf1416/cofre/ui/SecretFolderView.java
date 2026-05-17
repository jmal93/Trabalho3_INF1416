package br.pucrio.inf1416.cofre.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import br.pucrio.inf1416.cofre.model.SecretFileEntry;
import br.pucrio.inf1416.cofre.model.User;

public class SecretFolderView extends JFrame {

	private final User user;

	private JTextField folderPathField;
	private JButton browseFolderButton;

	private JPasswordField secretPhraseField;

	private JButton listFilesButton;
	private JButton decryptSelectedButton;
	private JButton backButton;

	private JTable filesTable;
	private DefaultTableModel tableModel;

	private List<SecretFileEntry> currentEntries;

	public SecretFolderView(User user) {
		this.user = user;

		configureWindow();
		createComponents();
		buildLayout();
	}

	private void configureWindow() {
		setTitle("Pasta de Arquivos Secretos");
		setSize(800, 500);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	private void createComponents() {
		folderPathField = new JTextField(35);
		folderPathField.setEditable(false);

		browseFolderButton = new JButton("Procurar...");
		browseFolderButton.addActionListener(e -> chooseFolder());

		secretPhraseField = new JPasswordField(30);

		listFilesButton = new JButton("Listar arquivos");
		decryptSelectedButton = new JButton("Decriptar selecionado");
		backButton = new JButton("Voltar");

		tableModel = new DefaultTableModel(new Object[] { "Código", "Nome secreto", "Dono", "Grupo" }, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		filesTable = new JTable(tableModel);
		filesTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
	}

	private void buildLayout() {
		JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		JLabel titleLabel = new JLabel("Consulta de Pasta Secreta", SwingConstants.CENTER);
		titleLabel.setFont(new Font("Arial", Font.BOLD, 18));

		JPanel topPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = defaultConstraints();

		addRow(topPanel, gbc, 0, "Usuário:", new JLabel(user.getLogin()));
		addRow(topPanel, gbc, 1, "Pasta secreta:", createFolderPanel());
		addRow(topPanel, gbc, 2, "Frase secreta:", secretPhraseField);

		JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		actionPanel.add(listFilesButton);

		JPanel northPanel = new JPanel(new BorderLayout());
		northPanel.add(titleLabel, BorderLayout.NORTH);
		northPanel.add(topPanel, BorderLayout.CENTER);
		northPanel.add(actionPanel, BorderLayout.SOUTH);

		JScrollPane scrollPane = new JScrollPane(filesTable);

		JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		bottomPanel.add(decryptSelectedButton);
		bottomPanel.add(backButton);

		mainPanel.add(northPanel, BorderLayout.NORTH);
		mainPanel.add(scrollPane, BorderLayout.CENTER);
		mainPanel.add(bottomPanel, BorderLayout.SOUTH);

		setContentPane(mainPanel);
	}

	private JPanel createFolderPanel() {
		JPanel panel = new JPanel(new BorderLayout(5, 0));
		panel.add(folderPathField, BorderLayout.CENTER);
		panel.add(browseFolderButton, BorderLayout.EAST);
		return panel;
	}

	private GridBagConstraints defaultConstraints() {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(6, 6, 6, 6);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		return gbc;
	}

	private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, java.awt.Component field) {
		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.weightx = 0;

		panel.add(new JLabel(label), gbc);

		gbc.gridx = 1;
		gbc.weightx = 1;

		panel.add(field, gbc);
	}

	private void chooseFolder() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		int result = chooser.showOpenDialog(this);

		if (result == JFileChooser.APPROVE_OPTION) {
			folderPathField.setText(chooser.getSelectedFile().getAbsolutePath());
		}
	}

	public String getFolderPath() {
		return folderPathField.getText().trim();
	}

	public String getSecretPhrase() {
		return new String(secretPhraseField.getPassword());
	}

	public void showFiles(List<SecretFileEntry> entries) {
		this.currentEntries = entries;

		tableModel.setRowCount(0);

		for (SecretFileEntry entry : entries) {
			tableModel.addRow(new Object[] { entry.getCodeName(), entry.getSecretName(), entry.getOwner(),
					entry.getGroupName() });
		}
	}

	public SecretFileEntry getSelectedFile() {
		int selectedRow = filesTable.getSelectedRow();

		if (selectedRow < 0 || currentEntries == null) {
			return null;
		}

		return currentEntries.get(selectedRow);
	}

	public void setListFilesAction(Runnable action) {
		listFilesButton.addActionListener(e -> action.run());
	}

	public void setDecryptSelectedAction(Runnable action) {
		decryptSelectedButton.addActionListener(e -> action.run());
	}

	public void setBackAction(Runnable action) {
		backButton.addActionListener(e -> action.run());
	}

	public void showMessage(String message) {
		JOptionPane.showMessageDialog(this, message);
	}
}
