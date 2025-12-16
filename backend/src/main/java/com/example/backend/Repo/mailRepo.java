package com.example.backend.Repo;

import java.io.File;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;


import org.springframework.stereotype.Repository;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


import com.example.backend.Util.EmailPriorityComparator;
import com.example.backend.Util.JsonFileManager;
import com.example.backend.model.mail;
import com.google.gson.reflect.TypeToken;

import jakarta.servlet.http.HttpServletRequest;

@Repository
public class mailRepo {
    private String senderEmail; // Keep this for backward compatibility with setSenderEmail()

    private final String BasePath = "data/users/";
    private final JsonFileManager jsonFileManager;
    private static final Type MAIL_LIST_TYPE = new TypeToken<List<mail>>(){}.getType();

    public mailRepo(JsonFileManager jsonFileManager) {
        this.jsonFileManager = jsonFileManager;
    }
     private String getLoggedInUser() {
        if (senderEmail != null) {
            return senderEmail;
        }

        // Get current request and session
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String email = (String) request.getSession().getAttribute("currentUser");
            System.out.println("mailService - Getting logged in user: " + email);
            return email;
        }

        System.err.println("mailService - WARNING: No request context available!");
        return null;
    }

    public List<mail> getInboxEmails() {
        String inboxPath = BasePath + getLoggedInUser() + "/inbox.json";
        return jsonFileManager.readListFromFile(inboxPath, MAIL_LIST_TYPE);
    }
    public List<mail> getSentEmails() {
        String sentPath = BasePath + getLoggedInUser() + "/sent.json";
        return jsonFileManager.readListFromFile(sentPath, MAIL_LIST_TYPE);
    }
    /**
     * Get all trash emails
     */
    public List<mail> getTrashEmails() {
        String trashPath = BasePath + getLoggedInUser() + "/trash.json";
        return jsonFileManager.readListFromFile(trashPath, MAIL_LIST_TYPE);
    }
    public List<mail> getDraftEmails() {
        String draftPath = BasePath + getLoggedInUser() + "/draft.json";
        return jsonFileManager.readListFromFile(draftPath, MAIL_LIST_TYPE);
    }
    

    /**
     * Get specific email by ID from a folder
     */
    public mail getEmailById(int id, String folder) {
        String folderPath = BasePath + getLoggedInUser() + "/" + folder + ".json";
        List<mail> emails = jsonFileManager.readListFromFile(folderPath, MAIL_LIST_TYPE);

        return emails.stream()
                .filter(email -> email.getId() == id)
                .findFirst()
                .orElse(null);
    }
    private final Object trashLock = new Object();
    private final Object folderLock = new Object();

    public boolean deleteEmail(int id, String folder) {
        System.out.println("=== DELETE EMAIL START ===");
        System.out.println("Email ID: " + id);
        System.out.println("Folder: " + folder);

        try {
            if (folder.equals("trash")) {
                // Permanently delete from trash
                return permanentlyDeleteEmail(id);
            }

            String currentUser = getLoggedInUser();
            String folderPath = BasePath + currentUser + "/" + folder + ".json";
            String trashPath = BasePath + currentUser + "/trash.json";

            // Synchronize to prevent concurrent modification
            synchronized (folderLock) {
                List<mail> emails = jsonFileManager.readListFromFile(folderPath, MAIL_LIST_TYPE);

                // Handle null or empty list
                if (emails == null) {
                    emails = new ArrayList<>();
                }

                // Find and remove the email
                mail emailToDelete = null;
                for (mail email : emails) {
                    if (email.getId() == id) {
                        emailToDelete = email;
                        break;
                    }
                }

                if (emailToDelete == null) {
                    System.out.println("Email not found with ID: " + id);
                    return false;
                }

                // Remove from source folder
                emails.remove(emailToDelete);
                boolean writeSuccess = jsonFileManager.writeListToFile(folderPath, emails);

                if (!writeSuccess) {
                    System.err.println("Failed to write to folder: " + folderPath);
                    return false;
                }

                // Add to trash (synchronized separately)
                synchronized (trashLock) {
                    List<mail> trashEmails = jsonFileManager.readListFromFile(trashPath, MAIL_LIST_TYPE);

                    // Initialize trash list if null
                    if (trashEmails == null) {
                        trashEmails = new ArrayList<>();
                    }

                    emailToDelete.setTrashedAt(LocalDateTime.now());
                    trashEmails.add(emailToDelete);
                    boolean trashWriteSuccess = jsonFileManager.writeListToFile(trashPath, trashEmails);

                    if (!trashWriteSuccess) {
                        System.err.println("Failed to write to trash: " + trashPath);
                        return false;
                    }
                }

                System.out.println("=== DELETE EMAIL SUCCESS ===");
                return true;
            }

        } catch (Exception e) {
            System.err.println("=== DELETE EMAIL ERROR ===");
            System.err.println("Error deleting email: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete email", e);
        }
    }

    private boolean permanentlyDeleteEmail(int id) {
        String trashPath = BasePath + getLoggedInUser() + "/trash.json";
        System.out.println("Attempting to delete email " + id + " from: " + trashPath);

        try {
            synchronized (trashLock) {
                List<mail> emails = jsonFileManager.readListFromFile(trashPath, MAIL_LIST_TYPE);

                // Handle case where trash file doesn't exist or is null
                if (emails == null) {
                    emails = new ArrayList<>();
                    return false; // Email not found
                }

                List<mail> filteredEmails = emails.stream()
                        .filter(email -> email.getId() != id)
                        .collect(Collectors.toList());

                if (filteredEmails.size() < emails.size()) {
                    jsonFileManager.writeListToFile(trashPath, filteredEmails);
                    return true;
                }

                return false;
            }
        } catch (Exception e) {
            System.err.println("Error permanently deleting email: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to permanently delete email", e);
        }
    }

    // ADD this method to mailRepo.java after getDraftEmails()

    /**
     * Get all starred emails from inbox and sent folders
     */
    // In your mailRepo class, update the getStarredEmails() method to include folder information

public List<mail> getStarredEmails() {
    String currentUser = getLoggedInUser();
    List<mail> starredEmails = new ArrayList<>();
    
    // Search through all folders for starred emails
    String[] folders = {"inbox", "sent", "draft"};
    
    for (String folder : folders) {
        String folderPath = BasePath + currentUser + "/" + folder + ".json";
        List<mail> emails = jsonFileManager.readListFromFile(folderPath, MAIL_LIST_TYPE);
        
        if (emails != null) {
            for (mail email : emails) {
                if (email.isStarred()) {
                    // IMPORTANT: Set the folder property so frontend knows where email actually lives
                    email.setFolder(folder);
                    starredEmails.add(email);
                }
            }
        }
    }
    
    // Also check custom folders
    File userDir = new File(BasePath + currentUser);
    if (userDir.exists() && userDir.isDirectory()) {
        File[] files = userDir.listFiles((dir, name) -> name.startsWith("folder_") && name.endsWith(".json"));
        
        if (files != null) {
            for (File file : files) {
                // Extract just the ID part from "folder_123.json"
                String fileName = file.getName().replace(".json", "");
                String folderPath = file.getAbsolutePath();
                List<mail> emails = jsonFileManager.readListFromFile(folderPath, MAIL_LIST_TYPE);
                
                if (emails != null) {
                    for (mail email : emails) {
                        if (email.isStarred()) {
                            // Set folder as "folder_X" to match file naming (e.g., "folder_123")
                            email.setFolder(fileName);  // CHANGED: Use fileName directly instead of concatenating
                            starredEmails.add(email);
                        }
                    }
                }
            }
        }
    }
    
    return starredEmails;
}
    /**
 * Toggle star status and manage starred folder
 */
public boolean toggleStar(int id, String folder) {
    try {
        String currentUser = getLoggedInUser();
        String folderPath = BasePath + currentUser + "/" + folder + ".json";
        String starredPath = BasePath + currentUser + "/starred.json";

        synchronized (folderLock) {
            // Get the email from the source folder
            List<mail> emails = jsonFileManager.readListFromFile(folderPath, MAIL_LIST_TYPE);
            
            if (emails == null) {
                emails = new ArrayList<>();
            }

            mail emailToToggle = null;
            for (mail email : emails) {
                if (email.getId() == id) {
                    emailToToggle = email;
                    break;
                }
            }

            if (emailToToggle == null) {
                System.out.println("Email not found with ID: " + id);
                return false;
            }

            // Toggle the starred status
            emailToToggle.setStarred(!emailToToggle.isStarred());
            
            // Update the original folder
            jsonFileManager.writeListToFile(folderPath, emails);

            // Manage starred folder
            List<mail> starredEmails = jsonFileManager.readListFromFile(starredPath, MAIL_LIST_TYPE);
            if (starredEmails == null) {
                starredEmails = new ArrayList<>();
            }

            if (emailToToggle.isStarred()) {
                // Add to starred folder (create a copy)
                mail starredCopy = createMailCopy(emailToToggle);
                starredEmails.add(starredCopy);
            } else {
                // Remove from starred folder
                starredEmails.removeIf(email -> email.getId() == id);
            }

            jsonFileManager.writeListToFile(starredPath, starredEmails);
            
            return true;
        }
    } catch (Exception e) {
        System.err.println("Error toggling star: " + e.getMessage());
        e.printStackTrace();
        return false;
    }
}

/**
 * Create a copy of a mail object
 */
private mail createMailCopy(mail original) {
    try {
        return (mail) original.clone();
    } catch (CloneNotSupportedException e) {
        System.err.println("Error cloning mail object: " + e.getMessage());
        e.printStackTrace();
        // Fallback to manual copy if clone fails
        return createMailCopy(original);
    }
}    
}
