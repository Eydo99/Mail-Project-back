package com.example.backend.Factory;

import com.example.backend.DTOS.FolderRequestDTO;
import com.example.backend.model.Folder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Factory for creating Folder entities
 * Encapsulates folder creation logic and allow extensibility in the future
 */
@Component
public class FolderFactory {

    /**
     * Creates a new Folder from request DTO
     * @param dto The folder data
     * @return A fully initialized Folder with unique id and email count
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
     * @param folder The folder to update
     * @param dto The new folder data
     */
    public void updateFolder(Folder folder, FolderRequestDTO dto) {
        folder.setName(dto.getName());
        folder.setDescription(dto.getDescription() != null ? dto.getDescription() : "");
        folder.setColor(dto.getColor());
    }

}