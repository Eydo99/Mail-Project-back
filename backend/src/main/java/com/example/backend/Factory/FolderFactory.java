package com.example.backend.Factory;

import com.example.backend.DTOS.FolderRequestDTO;
import com.example.backend.model.Folder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Factory for creating Folder entities
 * Encapsulates folder creation logic and ensures consistent initialization
 */
@Component
public class FolderFactory {

    /**
     * Creates a new Folder from request DTO
     *
     * @param dto The folder request data
     * @return A fully initialized Folder entity
     */
    public Folder createFolder(FolderRequestDTO dto) {
        Folder folder = new Folder();
        folder.setId(UUID.randomUUID().toString());
        folder.setName(dto.getName());
        folder.setDescription(dto.getDescription() != null ? dto.getDescription() : "");
        folder.setColor(dto.getColor());
        folder.setEmailCount(0);
        return folder;
    }

    /**
     * Updates an existing Folder with new data
     * Note: Keeps the existing ID and emailCount
     *
     * @param folder The folder to update
     * @param dto The new folder data
     */
    public void updateFolder(Folder folder, FolderRequestDTO dto) {
        folder.setName(dto.getName());
        folder.setDescription(dto.getDescription() != null ? dto.getDescription() : "");
        folder.setColor(dto.getColor());
        // Note: emailCount is not updated here, managed separately
    }

    /**
     * Creates a folder with specific ID (useful for testing or migration)
     *
     * @param id The specific ID to use
     * @param dto The folder request data
     * @return A fully initialized Folder entity with specified ID
     */
    public Folder createFolderWithId(String id, FolderRequestDTO dto) {
        Folder folder = createFolder(dto);
        folder.setId(id);
        return folder;
    }

    /**
     * Creates a default system folder with preset values
     *
     * @param name The folder name
     * @param color The folder color
     * @return A folder with default system settings
     */
    public Folder createSystemFolder(String name, String color) {
        Folder folder = new Folder();
        folder.setId(UUID.randomUUID().toString());
        folder.setName(name);
        folder.setDescription("System folder");
        folder.setColor(color);
        folder.setEmailCount(0);
        return folder;
    }
}