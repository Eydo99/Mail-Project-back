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
  *Repo for managing custom folders in JSON files
  *middleman between the service and folder.json using json file manager
  */
@Repository
public class FolderRepo {

    @Autowired
    private JsonFileManager jsonFileManager;
    private static final String foldersFile = "folders";

    /**
     * Get all folders for a user

     * @param userEmail:email of the user to get all his folders
     * @return list of folders if not null,empty list otherwise
     */
    public List<Folder> findAll(String userEmail) {
        //determine type of list for deserialization of json
        Type listType = new TypeToken<List<Folder>>() {}.getType();
        //get path exactly
        String path = jsonFileManager.getUserFolderPath(userEmail, foldersFile);
        //get all folders in folders.json
        List<Folder> folders = jsonFileManager.readListFromFile(path, listType);
        return folders != null ? folders : new ArrayList<>();
    }

    /**
     * Find folder by ID
     * @param userEmail:the email of user to get his folders
     * @param folderId:to get the exact folder from folders.json
     * @return folder by its id
     */
    public Optional<Folder> findById(String userEmail, String folderId) {
        //get all folders on folders.json
        List<Folder> folders = findAll(userEmail);
        return folders.stream()
                .filter(f -> f.getId().equals(folderId))
                .findFirst();
    }

    /**
     * Save all folders
     * @param userEmail:the email of user to get his folders
     * @param folders:List of folders to be saved
     */
    public void saveAll(String userEmail, List<Folder> folders) {
        //get path exactly
        String path = jsonFileManager.getUserFolderPath(userEmail, foldersFile);
        //save list to folders.json
        jsonFileManager.writeListToFile(path, folders);
    }

    /**
     * Add a new folder
     * update existing one
     * @param userEmail:the email of user to get his folders
     * @param folder:new folder or updated one
     * @return the new or the updated folder
     */
    public Folder save(String userEmail, Folder folder) {
        //get all folders
        List<Folder> folders = findAll(userEmail);

        // Check if folder with this ID already exists
        //optional is used for null exception
        Optional<Folder> existing = folders.stream()
                .filter(f -> f.getId().equals(folder.getId()))
                .findFirst();

        if (existing.isPresent()) {
            // remove existing folder
            folders.removeIf(f -> f.getId().equals(folder.getId()));
        }
        //add the new or the updated folder
        folders.add(folder);
        //save the new or updated list to folders.json
        saveAll(userEmail, folders);
        return folder;
    }

    /**
     * Delete folder by ID
     * @param userEmail:the email of user to get his folders
     * @param folderId:to get the exact folder from folders.json
     * @return removed folder
     */
    public boolean delete(String userEmail, String folderId) {
        //get all folders
        List<Folder> folders = findAll(userEmail);
        //find the folder to be removed and remove it if exists
        boolean removed = folders.removeIf(f -> f.getId().equals(folderId));
        //save the new list to folders.json
        if (removed) {
            saveAll(userEmail, folders);
        }
        return removed;
    }

    /**
     * Update folder email count
     * @param userEmail:the email of user to get his folders
     * @param folderId:to get the exact folder from folders.json
     * @param count:the new count of mails inside folder
     */
    public void updateEmailCount(String userEmail, String folderId, int count) {
        //get all folders in folders.json
        List<Folder> folders = findAll(userEmail);
        //find folder to be updated by id,if found then update the emailCount and save the list
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
     * @param userEmail:the email of user to get his folders
     * @param folderId:to get the exact folder from folders.json
     * @param increment:value to be incremented or decremented
     */
    public void incrementEmailCount(String userEmail, String folderId, int increment) {
        //get all folders in folders.json
        List<Folder> folders = findAll(userEmail);
        //find folder to be updated by id,if found then increment the emailCount and save the list
        folders.stream()
                .filter(f -> f.getId().equals(folderId))
                .findFirst()
                .ifPresent(folder -> {
                    folder.setEmailCount(folder.getEmailCount() + increment);
                    saveAll(userEmail, folders);
                });
    }
}