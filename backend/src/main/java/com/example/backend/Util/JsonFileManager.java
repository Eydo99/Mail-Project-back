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
 * gateway for any component to access file manager (middle man)proxy design pattern is implemented (smart proxy)
 * not a proxy class by taking instances and objects, but it takes the filesystem (single entry point,controls access to resource,hides complexity)
 */
@Service
public class JsonFileManager {

    // Formatter for LocalDateTime serialization
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    /**
     * Gson instance for JSON serialization/deserialization with LocalDateTime support
     * java object <-> JSON text
     * GSON builder uses Builder design pattern
     **/
    private static final Gson gson = new GsonBuilder()
            //for indentation between json attributes
            .setPrettyPrinting()
            /*
             All of this made because json doesn't support localDataTime conversion
             registerTypeAdapter:add custom serializer and deserializer
             JsonSerializer(T):teaches json how to convert custom objects
             */
            .registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
                //serialization of localDateTime to String
                @Override
                public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
                    return new JsonPrimitive(src.format(dateTimeFormatter));
                }
            })
            //JSON deserializer(T):teaches json how to convert strings back to custom objects
            .registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
                //deserialization of localDateTime from String back to its type
                @Override
                public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                        throws JsonParseException {
                    return LocalDateTime.parse(json.getAsString(), dateTimeFormatter);
                }
            })
            //build the json Object
            .create();

    // Base directory for storing user data
    private static final String basePath = "data/users/";

    /**
     * Reads objects from a JSON file and converts them to List<T>
     * T> Type of objects to read
     * @param filePath: Path to the JSON file
     * @param type: Type token for deserialization
     * @return List of objects, or empty list if file doesn't exist or error occurs
     */
    public <T> List<T> readListFromFile(String filePath, Type type) {
        //create file object
        File file = new File(filePath);

        // If file doesn't exist, return empty list
        if (!file.exists()) {
            System.out.println("File not found: " + filePath);
            return new ArrayList<>();
        }

        //read data inside file and close it after reading to avoid memory leak
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
     *  <T> : Type of objects to write
     * @param filePath: Path to the JSON file
     * @param items: List of objects to write
     * @return true: if successful, false otherwise
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
     * Creates a user folder  with default JSON files
     * Creates:inbox.json, sent.json, trash.json, draft.json,folders.json,contacts.json
     * @param email: User's email address
     * @return true if successful, false otherwise
     */
    public boolean createUserFolder(String email) {
        try {
            // Create user directory at the given path
            Path userPath = Paths.get(basePath + email);
            Files.createDirectories(userPath);
            System.out.println("Created user folder: " + userPath);

            // Create default folder files (inbox, sent, trash, draft,contacts,folders)
            String[] folders = { "inbox.json", "sent.json", "trash.json", "draft.json","contacts.json","folders.json"};
            for (String folder : folders) {
                //get file path
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
     * Build the full path to a user's folder file
     * @param email:  User's email
     * @param folder: Folder name (inbox, sent, trash, draft)
     *@ return Full file path
     */
    public String getUserFolderPath(String email, String folder) {
        return basePath + email + "/" + folder + ".json";
    }


    /**
     * Deletes a file
     * @param filePath: Path to the file to delete
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
     * @param email: User's email
     * @return true if folder exists, false otherwise
     */
    public boolean userExists(String email) {
        File userFolder = new File(basePath + email);
        return userFolder.exists() && userFolder.isDirectory();
    }
}