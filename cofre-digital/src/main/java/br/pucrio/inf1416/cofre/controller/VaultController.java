package br.pucrio.inf1416.cofre.controller;

import java.nio.file.Path;
import java.util.List;

import br.pucrio.inf1416.cofre.model.SecretFileEntry;
import br.pucrio.inf1416.cofre.model.User;
import br.pucrio.inf1416.cofre.service.VaultService;
import br.pucrio.inf1416.cofre.ui.SecretFolderView;

public class VaultController {
	private final SecretFolderView secretFolderView;
	private final VaultService vaultService;
	private final User currentUser;

	public VaultController(SecretFolderView secretFolderView, VaultService vaultService, User currentUser) {
		super();
		this.secretFolderView = secretFolderView;
		this.vaultService = vaultService;
		this.currentUser = currentUser;

		configureActions();
	}

	private void configureActions() {
		secretFolderView.setListFilesAction(this::handleListFiles);
		secretFolderView.setDecryptSelectedAction(this::handleDecryptSelectedFile);
		secretFolderView.setBackAction(this::handleBack);
	}

	private void handleListFiles() {
		try {
			Path folderPath = Path.of(secretFolderView.getFolderPath());
			String secretPhrase = secretFolderView.getSecretPhrase();

			List<SecretFileEntry> files = vaultService.listVisibleFiles(currentUser, folderPath, secretPhrase);

			secretFolderView.showFiles(files);

			if (files.isEmpty()) {
				secretFolderView.showMessage("Nenhum arquivo disponívle para este usuário");
			}
		} catch (Exception e) {
			secretFolderView.showMessage("Erro ao listar arquivos: " + e.getMessage());
		}
	}

	private void handleDecryptSelectedFile() {
		try {
			SecretFileEntry selectedFile = secretFolderView.getSelectedFile();

			if (selectedFile == null) {
				secretFolderView.showMessage("Selecione um arquivo");
				return;
			}

			Path folderPath = Path.of(secretFolderView.getFolderPath());
			String secretPhrase = secretFolderView.getSecretPhrase();

			vaultService.decryptSelectedFile(currentUser, folderPath, selectedFile, secretPhrase);

			secretFolderView.showMessage("Arquivo decriptado com sucesso");
		} catch (Exception e) {
			secretFolderView.showMessage("Erro ao decriptar arquivo: " + e.getMessage());
		}
	}

	private void handleBack() {
		secretFolderView.dispose();
	}
}
