package com.example.backend.Repo;

import com.example.backend.model.Folder;
import com.example.backend.Util.JsonFileManager;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing custom folders in JSON files
 * Similar pattern to contactRepo
 */
@Repository
public class FolderRepo {

    @Autowired
    private JsonFileManager jsonFileManager;

    private static final String FOLDERS_FILE = "folders";

    /**
     * Get all folders for a user
     */
    public List<Folder> findAll(String userEmail) {
        Type listType = new TypeToken<List<Folder>>() {}.getType();
        String path = jsonFileManager.getUserFolderPath(userEmail, FOLDERS_FILE);
        List<Folder> folders = jsonFileManager.readListFromFile(path, listType);

        return folders != null ? folders : new ArrayList<>();
    }

    /**
     * Find folder by ID
     */
    public Optional<Folder> findById(String userEmail, String folderId) {
        List<Folder> folders = findAll(userEmail);
        return folders.stream()
                .filter(f -> f.getId().equals(folderId))
                .findFirst();
    }

    /**
     * Save all folders
     */
    public void saveAll(String userEmail, List<Folder> folders) {
        String path = jsonFileManager.getUserFolderPath(userEmail, FOLDERS_FILE);
        jsonFileManager.writeListToFile(path, folders);
    }

    /**
     * Add a new folder
     */
    public Folder save(String userEmail, Folder folder) {
        List<Folder> folders = findAll(userEmail);

        // Check if folder with this ID already exists
        Optional<Folder> existing = folders.stream()
                .filter(f -> f.getId().equals(folder.getId()))
                .findFirst();

        if (existing.isPresent()) {
            // Update existing folder
            folders.removeIf(f -> f.getId().equals(folder.getId()));
        }

        folders.add(folder);
        saveAll(userEmail, folders);
        return folder;
    }

    /**
     * Delete folder by ID
     */
    public boolean delete(String userEmail, String folderId) {
        List<Folder> folders = findAll(userEmail);
        boolean removed = folders.removeIf(f -> f.getId().equals(folderId));

        if (removed) {
            saveAll(userEmail, folders);
        }

        return removed;
    }

    /**
     * Update folder email count
     */
    public void updateEmailCount(String userEmail, String folderId, int count) {
        List<Folder> folders = findAll(userEmail);

        folders.stream()
                .filter(f -> f.getId().equals(folderId))
                .findFirst()
                .ifPresent(folder -> {
                    folder.setEmailCount(count);
                    saveAll(userEmail, folders);
                });
    }

    /**
     * Increment folder email count
     */
    public void incrementEmailCount(String userEmail, String folderId, int increment) {
        List<Folder> folders = findAll(userEmail);

        folders.stream()
                .filter(f -> f.getId().equals(folderId))
                .findFirst()
                .ifPresent(folder -> {
                    folder.setEmailCount(folder.getEmailCount() + increment);
                    saveAll(userEmail, folders);
                });
    }
}