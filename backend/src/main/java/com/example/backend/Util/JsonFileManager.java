package com.example.backend.Util;

import com.google.gson.*;
import org.springframework.stereotype.Service;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * FileService handles all file I/O operations for the system
 * Generic JSON manager that works with any DTO type
 * Responsibilities: Read/Write JSON files, Create folders, Manage file paths
 * no business layer touches the file system only proxy does
 * it is the gateway for any component to acess file manager (middle man)proxy design pattern is implmented (smart proxy)
 * it is not a proxy class by takingg instances and objects but it takes the filesystem (single entry point,controls access to resource,hides complexity)
 */
@Service
public class JsonFileManager {

    // Formatter for LocalDateTime serialization
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // Gson instance for JSON serialization/deserialization with LocalDateTime support
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
                @Override
                public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
                    return new JsonPrimitive(src.format(DATE_TIME_FORMATTER));
                }
            })
            .registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
                @Override
                public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                        throws JsonParseException {
                    return LocalDateTime.parse(json.getAsString(), DATE_TIME_FORMATTER);
                }
            })
            .create();

    // Base directory for storing user data
    private static final String BASE_PATH = "data/users/";

    /**
     * Reads objects from a JSON file and converts them to List<T>
     * Generic method that works with any DTO type
     *
     * @param <T>      Type of objects to read
     * @param filePath Path to the JSON file (e.g., "data/users/omar@mail.com/inbox.json")
     * @param type     Type token for deserialization (e.g., new TypeToken<List<Mail>>(){}.getType())
     * @return List of objects, or empty list if file doesn't exist or error occurs
     */
    public <T> List<T> readListFromFile(String filePath, Type type) {
        File file = new File(filePath);

        // If file doesn't exist, return empty list
        if (!file.exists()) {
            System.out.println("File not found: " + filePath);
            return new ArrayList<>();
        }

        try (Reader reader = new FileReader(file)) {
            // Define the type for Gson to deserialize (List<T>)
            System.out.println("trying to read file: " + filePath);
            List<T> items = gson.fromJson(reader, type);

            // Return the list or empty list if null
            return items != null ? items : new ArrayList<>();

        } catch (IOException e) {
            System.err.println("Error reading file: " + filePath);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Writes a list of objects to a JSON file
     * Generic method that works with any DTO type
     *
     * @param <T>      Type of objects to write
     * @param filePath Path to the JSON file
     * @param items    List of objects to write
     * @return true if successful, false otherwise
     */
    public <T> boolean writeListToFile(String filePath, List<T> items) {
        try {
            // Create parent directories if they don't exist
            File file = new File(filePath);
            file.getParentFile().mkdirs();

            // Write the list to JSON file
            try (Writer writer = new FileWriter(file)) {
                gson.toJson(items, writer);
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
            String[] folders = { "inbox.json", "sent.json", "trash.json", "draft.json","contacts.json","folders.json" };

            for (String folder : folders) {
                File file = new File(userPath.toString(), folder);

                if (!file.exists()) {
                    // Create empty JSON file (empty array)
                    writeListToFile(file.getPath(), new ArrayList<>());
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