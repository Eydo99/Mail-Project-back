package com.example.backend.service;

import com.example.backend.DTOS.FolderRequestDTO;
import com.example.backend.DTOS.FolderResponseDTO;
import com.example.backend.Repo.FolderRepo;
import com.example.backend.Factory.FolderFactory;
import com.example.backend.model.Folder;
import com.example.backend.Util.JsonFileManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing custom folders
 * uses singleton design pattern using @service
 * handles all folders logic
 */
@Service
public class FolderService {

    @Autowired
    private FolderRepo folderRepo;

    @Autowired
    private JsonFileManager jsonFileManager;

    @Autowired
    private FolderFactory folderFactory;
    /**
     * Get all folders for a user
     * @param userEmail : needed to find all folders for current user
     * @return list of folders
     */
    public List<FolderResponseDTO> getAllFolders(String userEmail) {
        // Auto-create user folder if it doesn't exist
        if (!jsonFileManager.userExists(userEmail)) {
            jsonFileManager.createUserFolder(userEmail);
        }
        //get all folders for the user
        List<Folder> folders = folderRepo.findAll(userEmail);
        //convert folder Model to DTO using the map to dto method
        return folders.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get folder by ID
     * @param userEmail : needed to find all folders for current user
     * @param folderId :the id of the folder
     * @return wanted folder
     */
    public Optional<FolderResponseDTO> getFolderById(String userEmail, String folderId) {
        return folderRepo.findById(userEmail, folderId)
                .map(this::mapToDTO);
    }

    /**
     * Create new folder
     * @param userEmail :needed to find all folders for current user
     * @param dto : the info of the created folder
     */
    public FolderResponseDTO createFolder(String userEmail, FolderRequestDTO dto) {
        // Use factory to create folder
        Folder folder = folderFactory.createFolder(dto);
        //save folder to list of folders in folder.json
        Folder saved = folderRepo.save(userEmail, folder);

        // Create empty JSON file for this folder
        String folderPath = jsonFileManager.getUserFolderPath(userEmail, "folder_" + saved.getId());
        jsonFileManager.writeListToFile(folderPath, List.of());

        return mapToDTO(saved);
    }

    /**
     * Update folder
     * @param userEmail : needed to find all folders for current user
     * @param dto : the info of the updated folder
     * @param folderId : the id of the folder
     * @return updated folder or null
     */
    public Optional<FolderResponseDTO> updateFolder(String userEmail, String folderId, FolderRequestDTO dto) {
        //get folder from folder.json
        Optional<Folder> existing = folderRepo.findById(userEmail, folderId);

        if (existing.isPresent()) {
            //if folder really exists save it in a folder variable
            Folder folder = existing.get();

            // Use factory to update folder
            folderFactory.updateFolder(folder, dto);
            //save the new updated folder instead of existing one
            Folder updated = folderRepo.save(userEmail, folder);
            //convert the folder to dto and return it
            return Optional.of(mapToDTO(updated));
        }

        return Optional.empty();
    }

    /**
     * Delete folder and move emails to inbox
     * @param userEmail  : needed to find all folders for current user
     * @param folderId : the id of the folder to be deleted
     */
    public boolean deleteFolder(String userEmail, String folderId) {
        String folderPath = jsonFileManager.getUserFolderPath(userEmail, "folder_" + folderId);
        jsonFileManager.deleteFile(folderPath);

        // Delete the folder metadata
        return folderRepo.delete(userEmail, folderId);
    }

    /**
     * Update folder email count
     * @param userEmail : needed to find all folders for current user
     * @param folderId :the id of the folder to be updated
     * @param count :the new number of mails inside the folder
     */
    public void updateFolderCount(String userEmail, String folderId, int count) {
        folderRepo.updateEmailCount(userEmail, folderId, count);
    }

    /**
     * Increment folder email count
     * @param userEmail : needed to find all folders for current user
     * @param folderId :the id of the folder to be updated
     * @param increment :the number of mails to be added
     */
    public void incrementFolderCount(String userEmail, String folderId, int increment) {
        folderRepo.incrementEmailCount(userEmail, folderId, increment);
    }

    /**
     * Map Folder entity to DTO
     * @param folder :folder to be converted to DTO response
     * @return Folder response dto
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