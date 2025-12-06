package com.example.backend.service;

import com.example.backend.DTOS.FolderRequestDTO;
import com.example.backend.DTOS.FolderResponseDTO;
import com.example.backend.DTOS.mailDTO;
import com.example.backend.Repo.FolderRepo;
import com.example.backend.model.Folder;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing custom folders - Works with your existing DTO structure
 */
@Service
public class FolderService {

    @Autowired
    private FolderRepo folderRepo;

    @Autowired
    private JsonFileManager jsonFileManager;

    private static final Type MAIL_LIST_TYPE = new TypeToken<List<mailDTO>>(){}.getType();

    /**
     * Get all folders for a user
     */
    public List<FolderResponseDTO> getAllFolders(String userEmail) {
        // Auto-create user folder if it doesn't exist
        if (!jsonFileManager.userExists(userEmail)) {
            jsonFileManager.createUserFolder(userEmail);
        }

        List<Folder> folders = folderRepo.findAll(userEmail);

        return folders.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get folder by ID
     */
    public Optional<FolderResponseDTO> getFolderById(String userEmail, String folderId) {
        return folderRepo.findById(userEmail, folderId)
                .map(this::mapToDTO);
    }

    /**
     * Create new folder
     */
    public FolderResponseDTO createFolder(String userEmail, FolderRequestDTO dto) {
        Folder folder = new Folder();
        folder.setId(UUID.randomUUID().toString());
        folder.setName(dto.getName());
        folder.setDescription(dto.getDescription());
        folder.setColor(dto.getColor());
        folder.setEmailCount(0);

        Folder saved = folderRepo.save(userEmail, folder);

        // Create empty JSON file for this folder
        String folderPath = jsonFileManager.getUserFolderPath(userEmail, "folder_" + saved.getId());
        jsonFileManager.writeListToFile(folderPath, List.of());

        return mapToDTO(saved);
    }

    /**
     * Update folder
     */
    public Optional<FolderResponseDTO> updateFolder(String userEmail, String folderId, FolderRequestDTO dto) {
        Optional<Folder> existing = folderRepo.findById(userEmail, folderId);

        if (existing.isPresent()) {
            Folder folder = existing.get();
            folder.setName(dto.getName());
            folder.setDescription(dto.getDescription());
            folder.setColor(dto.getColor());

            Folder updated = folderRepo.save(userEmail, folder);
            return Optional.of(mapToDTO(updated));
        }

        return Optional.empty();
    }

    /**
     * Delete folder and move emails to inbox
     */
    public boolean deleteFolder(String userEmail, String folderId) {
        // Move all emails from this folder to inbox
        moveAllEmailsToInbox(userEmail, folderId);

        // Delete the folder JSON file
        String folderPath = jsonFileManager.getUserFolderPath(userEmail, "folder_" + folderId);
        jsonFileManager.deleteFile(folderPath);

        // Delete the folder metadata
        return folderRepo.delete(userEmail, folderId);
    }

    /**
     * Get all emails in a custom folder
     */
    public List<mailDTO> getEmailsByFolder(String userEmail, String folderId) {
        String folderPath = jsonFileManager.getUserFolderPath(userEmail, "folder_" + folderId);
        return jsonFileManager.readListFromFile(folderPath, MAIL_LIST_TYPE);
    }

    /**
     * Update folder email count
     */
    public void updateFolderCount(String userEmail, String folderId, int count) {
        folderRepo.updateEmailCount(userEmail, folderId, count);
    }

    /**
     * Increment folder email count
     */
    public void incrementFolderCount(String userEmail, String folderId, int increment) {
        folderRepo.incrementEmailCount(userEmail, folderId, increment);
    }

    /**
     * Recalculate email count for a folder
     */
    public void recalculateFolderCount(String userEmail, String folderId) {
        List<mailDTO> emails = getEmailsByFolder(userEmail, folderId);
        updateFolderCount(userEmail, folderId, emails.size());
    }

    /**
     * Move all emails from a folder to inbox (when folder is deleted)
     */
    private void moveAllEmailsToInbox(String userEmail, String folderId) {
        String folderPath = jsonFileManager.getUserFolderPath(userEmail, "folder_" + folderId);
        String inboxPath = jsonFileManager.getUserFolderPath(userEmail, "inbox");

        List<mailDTO> folderEmails = jsonFileManager.readListFromFile(folderPath, MAIL_LIST_TYPE);

        if (!folderEmails.isEmpty()) {
            // Get inbox emails
            List<mailDTO> inboxEmails = jsonFileManager.readListFromFile(inboxPath, MAIL_LIST_TYPE);

            // Clear custom folder reference from emails
            folderEmails.forEach(email -> email.setCustomFolderId(null));

            // Add to inbox
            inboxEmails.addAll(folderEmails);

            // Save inbox
            jsonFileManager.writeListToFile(inboxPath, inboxEmails);
        }
    }

    /**
     * Map Folder entity to DTO
     */
    private FolderResponseDTO mapToDTO(Folder folder) {
        return new FolderResponseDTO(
                folder.getId(),
                folder.getName(),
                folder.getDescription(),
                folder.getColor(),
                folder.getEmailCount()
        );
    }
}