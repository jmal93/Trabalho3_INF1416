package br.pucrio.inf1416.cofre.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public abstract class BaseFormView extends JFrame {

	protected JPanel mainPanel;
	protected JPanel formPanel;
	protected JPanel buttonPanel;
	protected GridBagConstraints gbc;

	protected void configureWindow(String title, int width, int height) {
		setTitle(title);
		setSize(width, height);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	protected void initializeBaseLayout(String title) {
		mainPanel = new JPanel(new BorderLayout(10, 10));
		formPanel = new JPanel(new java.awt.GridBagLayout());
		buttonPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

		mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
		titleLabel.setFont(new Font("Arial", Font.BOLD, 18));

		gbc = new GridBagConstraints();
		gbc.insets = new Insets(6, 6, 6, 6);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		mainPanel.add(titleLabel, BorderLayout.NORTH);
		mainPanel.add(formPanel, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);

		setContentPane(mainPanel);
	}

	protected void addRow(int row, String label, JComponent field) {
		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.weightx = 0;

		formPanel.add(new JLabel(label), gbc);

		gbc.gridx = 1;
		gbc.weightx = 1;

		formPanel.add(field, gbc);
	}

	protected JPanel createFileFieldPanel(JTextField field, JButton button) {
		JPanel panel = new JPanel(new BorderLayout(5, 0));
		panel.add(field, BorderLayout.CENTER);
		panel.add(button, BorderLayout.EAST);
		return panel;
	}

	protected void chooseFile(JTextField targetField) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		int result = fileChooser.showOpenDialog(this);

		if (result == JFileChooser.APPROVE_OPTION) {
			targetField.setText(fileChooser.getSelectedFile().getAbsolutePath());
		}
	}
}
