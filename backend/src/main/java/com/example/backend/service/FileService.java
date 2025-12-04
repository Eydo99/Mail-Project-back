package com.example.backend.service;

import com.example.backend.DTOS.mailDTO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.springframework.stereotype.Service;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * FileService handles all file I/O operations for the mail system
 * Responsibilities: Read/Write JSON files, Create folders, Manage file paths
 */
@Service
public class FileService {

    // Gson instance for JSON serialization/deserialization
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // Base directory for storing user data
    private static final String BASE_PATH = "data/users/";

    /**
     * Reads mails from a JSON file and converts them to List<Mail>
     *
     * @param filePath Path to the JSON file (e.g.,
     *                 "data/users/omar@mail.com/inbox.json")
     * @return List of Mail objects, or empty list if file doesn't exist or error
     *         occurs
     */
    public List<mailDTO> readMailsFromFile(String filePath) {
        File file = new File(filePath);

        // If file doesn't exist, return empty list
        if (!file.exists()) {
            System.out.println("File not found: " + filePath);
            return new ArrayList<>();
        }

        try (Reader reader = new FileReader(file)) {
            // Define the type for Gson to deserialize (List<Mail>)
            Type mailListType = new TypeToken<List<mailDTO>>() {
            }.getType();

            // Convert JSON to List<Mail>
            System.out.println("trying to read file: " + filePath);
            List<mailDTO> mails = gson.fromJson(reader, mailListType);

            // Return the list or empty list if null
            return mails != null ? mails : new ArrayList<>();

        } catch (IOException e) {
            System.err.println("Error reading file: " + filePath);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Writes a list of mails to a JSON file
     *
     * @param filePath Path to the JSON file
     * @param mails    List of Mail objects to write
     * @return true if successful, false otherwise
     */
    public boolean writeMailsToFile(String filePath, List<mailDTO> mails) {
        try {
            // Create parent directories if they don't exist
            File file = new File(filePath);
            file.getParentFile().mkdirs();

            // Write the list to JSON file
            try (Writer writer = new FileWriter(file)) {
                gson.toJson(mails, writer);
                System.out.println("Successfully wrote to: " + filePath);
                return true;
            }

        } catch (IOException e) {
            System.err.println("Error writing to file: " + filePath);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks if a file exists
     *
     * @param filePath Path to check
     * @return true if file exists, false otherwise
     */
    public boolean fileExists(String filePath) {
        return new File(filePath).exists();
    }

    /**
     * Creates a user folder structure with default JSON files
     * Creates: inbox.json, sent.json, trash.json, draft.json
     *
     * @param email User's email address
     * @return true if successful, false otherwise
     */
    public boolean createUserFolder(String email) {
        try {
            // Create user directory
            Path userPath = Paths.get(BASE_PATH + email);
            Files.createDirectories(userPath);
            System.out.println("Created user folder: " + userPath);

            // Create default folder files (inbox, sent, trash, draft)
            String[] folders = { "inbox.json", "sent.json", "trash.json", "draft.json" };

            for (String folder : folders) {
                File file = new File(userPath.toString(), folder);

                if (!file.exists()) {
                    // Create empty JSON file (empty array)
                    writeMailsToFile(file.getPath(), new ArrayList<>());
                    System.out.println("Created file: " + file.getName());
                }
            }

            return true;

        } catch (IOException e) {
            System.err.println("Error creating user folder: " + email);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Builds the full path to a user's folder file
     *
     * @param email  User's email
     * @param folder Folder name (inbox, sent, trash, draft)
     * @return Full file path (e.g., "data/users/omar@mail.com/inbox.json")
     */
    public String getUserFolderPath(String email, String folder) {
        return BASE_PATH + email + "/" + folder + ".json";
    }

    /**
     * Gets the base directory path for a user
     *
     * @param email User's email
     * @return Base directory path (e.g., "data/users/omar@mail.com/")
     */
    public String getUserBasePath(String email) {
        return BASE_PATH + email + "/";
    }

    /**
     * Deletes a file
     *
     * @param filePath Path to the file to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteFile(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                return file.delete();
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error deleting file: " + filePath);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks if a user folder exists
     *
     * @param email User's email
     * @return true if folder exists, false otherwise
     */
    public boolean userExists(String email) {
        File userFolder = new File(BASE_PATH + email);
        return userFolder.exists() && userFolder.isDirectory();
    }
}