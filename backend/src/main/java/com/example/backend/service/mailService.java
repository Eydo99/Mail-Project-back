package com.example.backend.service;
import java.util.PriorityQueue;
import java.util.Queue;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.example.backend.Util.EmailPriorityComparator;
import com.example.backend.Util.JsonFileManager;
import org.springframework.stereotype.Service;

import com.example.backend.DTOS.mailContentDTO;
import com.example.backend.DTOS.mailDTO;
import com.example.backend.Exceptions.UserNotFoundException;
import com.example.backend.Factory.mailFactory;
import com.google.gson.reflect.TypeToken;

import lombok.Getter;
import lombok.Setter;

@Service
@Getter
@Setter
public class mailService {
    private String senderEmail = "belal@gmail.com";
    private final String BasePath = "data/users/";
    private final JsonFileManager jsonFileManager;
    
    // Type token for List<mailDTO> - needed for generic FileService methods
    private static final Type MAIL_LIST_TYPE = new TypeToken<List<mailDTO>>(){}.getType();

    public mailService(JsonFileManager jsonFileManager) {
        this.jsonFileManager = jsonFileManager;
    }

    /**
     * Get all emails from inbox
     */
    public List<mailDTO> getInboxEmails() {
        String inboxPath = BasePath + senderEmail + "/inbox.json";
        return jsonFileManager.readListFromFile(inboxPath, MAIL_LIST_TYPE);
    }

    public List<mailDTO> getInboxEmailsByPriority() {
        String inboxPath = BasePath + senderEmail + "/inbox.json";
        List<mailDTO> emails = jsonFileManager.readListFromFile(inboxPath, MAIL_LIST_TYPE);


        PriorityQueue<mailDTO> priorityQueue = new PriorityQueue<>(new EmailPriorityComparator());

        priorityQueue.addAll(emails);

        // Extract emails from priority queue in sorted order
        List<mailDTO> sortedEmails = new ArrayList<>();
        while (!priorityQueue.isEmpty()) {
            sortedEmails.add(priorityQueue.poll());
        }

        return sortedEmails;
    }




    /**
     * Get all sent emails
     */
    public List<mailDTO> getSentEmails() {
        String sentPath = BasePath + senderEmail + "/sent.json";
        return jsonFileManager.readListFromFile(sentPath, MAIL_LIST_TYPE);
    }

    /**
     * Get all draft emails
     */
    public List<mailDTO> getDraftEmails() {
        String draftPath = BasePath + senderEmail + "/draft.json";
        return jsonFileManager.readListFromFile(draftPath, MAIL_LIST_TYPE);
    }

    /**
     * Get all trash emails
     */
    public List<mailDTO> getTrashEmails() {
        String trashPath = BasePath + senderEmail + "/trash.json";
        return jsonFileManager.readListFromFile(trashPath, MAIL_LIST_TYPE);
    }

    /**
     * Get specific email by ID from a folder
     */
    public mailDTO getEmailById(int id, String folder) {
        String folderPath = BasePath + senderEmail + "/" + folder + ".json";
        List<mailDTO> emails = jsonFileManager.readListFromFile(folderPath, MAIL_LIST_TYPE);

        return emails.stream()
                .filter(email -> email.getId() == id)
                .findFirst()
                .orElse(null);
    }

    /**
     * Compose and send a new email
     */


    public void composeMail(mailContentDTO mailContent) throws UserNotFoundException {
        String sentPath = BasePath + senderEmail + "/sent.json";
        mailDTO mail= mailFactory.createNewMail(mailContent) ;
        // Add to sent folder
        Queue<String> recipientsQueue = mail.getTo();
        String receiver =recipientsQueue.peek() ;
        
        if (!(jsonFileManager.userExists(receiver))) {
            System.out.println("this email{ "+receiver+" } isn't found in our system");
            throw new UserNotFoundException("Email address " + receiver + " is not registered in our system");
        }
        List<mailDTO> sentMails = jsonFileManager.readListFromFile(sentPath, MAIL_LIST_TYPE);
        sentMails.add(mail);
        jsonFileManager.writeListToFile(sentPath, sentMails);
        mail.setFrom(senderEmail);
        // Send to all receivers
        mail.setTo(null);

            String path = BasePath + receiver + "/inbox.json";
            List<mailDTO> inboxMails = jsonFileManager.readListFromFile(path, MAIL_LIST_TYPE);
            inboxMails.add(mail);
            jsonFileManager.writeListToFile(path, inboxMails);

        
    }
    public void saveDraft(mailContentDTO mailContent){
        String draftPath = BasePath + senderEmail + "/draft.json";
        List<mailDTO> draftMails = jsonFileManager.readListFromFile(draftPath, MAIL_LIST_TYPE);
        mailDTO mail =mailFactory.createNewMail(mailContent) ;
        draftMails.add(mail);
        jsonFileManager.writeListToFile(draftPath, draftMails);

    }

    // /**
    //  * Toggle star status of an email
    //  */
    // public boolean toggleStar(int id, String folder) {
    //     String folderPath = BasePath + senderEmail + "/" + folder + ".json";
    //     List<mailDTO> emails = fileService.readListFromFile()(folderPath, MAIL_LIST_TYPE);

    //     boolean found = false;
    //     for (mailDTO email : emails) {
    //         if (email.getId() == id) {
    //             email.setStared(!email.isStared());
    //             found = true;
    //             break;
    //         }
    //     }

    //     if (found) {
    //         fileService.writeListToFile()(folderPath, emails);
    //     }

    //     return found;
    // }

    /**
     * Delete an email (move to trash)
     */
// Add this at the class level
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

            String folderPath = BasePath + senderEmail + "/" + folder + ".json";
            String trashPath = BasePath + senderEmail + "/trash.json";

            // Synchronize to prevent concurrent modification
            synchronized (folderLock) {
                List<mailDTO> emails = jsonFileManager.readListFromFile(folderPath, MAIL_LIST_TYPE);

                // Handle null or empty list
                if (emails == null) {
                    emails = new ArrayList<>();
                }

                // Find and remove the email
                mailDTO emailToDelete = null;
                for (mailDTO email : emails) {
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
                    List<mailDTO> trashEmails = jsonFileManager.readListFromFile(trashPath, MAIL_LIST_TYPE);

                    // Initialize trash list if null
                    if (trashEmails == null) {
                        trashEmails = new ArrayList<>();
                    }

                    trashEmails.add(emailToDelete);
                    boolean trashWriteSuccess = jsonFileManager.writeListToFile(trashPath, trashEmails);

                    if (!trashWriteSuccess) {
                        System.err.println("Failed to write to trash: " + trashPath);
                        // TODO: Consider rolling back the folder deletion
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
        String trashPath = BasePath + senderEmail + "/trash.json";
        System.out.println("Attempting to delete email " + id + " from: " + trashPath);

        try {
            synchronized (trashLock) {
                List<mailDTO> emails = jsonFileManager.readListFromFile(trashPath, MAIL_LIST_TYPE);

                // Handle case where trash file doesn't exist or is null
                if (emails == null) {
                    emails = new ArrayList<>();
                    return false; // Email not found
                }

                List<mailDTO> filteredEmails = emails.stream()
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

    //move shefoo
    /**
     * Move an email from one folder to another
     */
    public boolean moveEmail(int id, String fromFolder, String toFolder) {
        System.out.println("=== MOVE EMAIL START ===");
        System.out.println("Email ID: " + id);
        System.out.println("From Folder: " + fromFolder);
        System.out.println("To Folder: " + toFolder);

        try {
            String fromFolderPath = BasePath + senderEmail + "/" + fromFolder + ".json";
            String toFolderPath = BasePath + senderEmail + "/" + toFolder + ".json";

            // Synchronize to prevent concurrent modification
            synchronized (folderLock) {
                // Read from source folder
                List<mailDTO> fromEmails = jsonFileManager.readListFromFile(fromFolderPath, MAIL_LIST_TYPE);

                if (fromEmails == null) {
                    fromEmails = new ArrayList<>();
                }

                // Find the email to move
                mailDTO emailToMove = null;
                for (mailDTO email : fromEmails) {
                    if (email.getId() == id) {
                        emailToMove = email;
                        break;
                    }
                }

                if (emailToMove == null) {
                    System.out.println("Email not found with ID: " + id);
                    return false;
                }

                // Remove from source folder
                fromEmails.remove(emailToMove);
                boolean removeSuccess = jsonFileManager.writeListToFile(fromFolderPath, fromEmails);

                if (!removeSuccess) {
                    System.err.println("Failed to remove from folder: " + fromFolderPath);
                    return false;
                }

                // Add to destination folder
                List<mailDTO> toEmails = jsonFileManager.readListFromFile(toFolderPath, MAIL_LIST_TYPE);

                if (toEmails == null) {
                    toEmails = new ArrayList<>();
                }

                toEmails.add(emailToMove);
                boolean addSuccess = jsonFileManager.writeListToFile(toFolderPath, toEmails);

                if (!addSuccess) {
                    System.err.println("Failed to add to folder: " + toFolderPath);
                    // TODO: Consider rolling back the removal
                    return false;
                }

                System.out.println("=== MOVE EMAIL SUCCESS ===");
                return true;
            }

        } catch (Exception e) {
            System.err.println("=== MOVE EMAIL ERROR ===");
            System.err.println("Error moving email: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to move email", e);
        }
    }


    /**
     * Get emails from custom folder
     */
    public List<mailDTO> getCustomFolderEmails(String folderId) {
        String folderPath = BasePath + senderEmail + "/folder_" + folderId + ".json";
        return jsonFileManager.readListFromFile(folderPath, MAIL_LIST_TYPE);
    }
}