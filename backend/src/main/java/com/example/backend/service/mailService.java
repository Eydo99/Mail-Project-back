package com.example.backend.service;

import java.util.PriorityQueue;
import java.util.Queue;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.example.backend.Util.EmailPriorityComparator;
import com.example.backend.Util.JsonFileManager;
import com.example.backend.model.mail;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.backend.DTOS.mailContentDTO;

import com.example.backend.Exceptions.UserNotFoundException;
import com.example.backend.Factory.mailFactory;
import com.google.gson.reflect.TypeToken;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@Getter
@Setter
public class mailService {

    // REMOVED: private String senderEmail = (String)
    // session.getAttribute("currentUser");

    private String senderEmail; // Keep this for backward compatibility with setSenderEmail()

    private final String BasePath = "data/users/";
    private final JsonFileManager jsonFileManager;

    // Type token for List<mail> - needed for generic FileService methods
    private static final Type MAIL_LIST_TYPE = new TypeToken<List<mail>>() {
    }.getType();

    public mailService(JsonFileManager jsonFileManager) {
        this.jsonFileManager = jsonFileManager;
    }

    /**
     * Get the currently logged-in user's email from session
     */
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

    /**
     * Get all emails from inbox
     */

    public List<mail> getInboxEmailsByPriority() {
        String inboxPath = BasePath + getLoggedInUser() + "/inbox.json";
        List<mail> emails = jsonFileManager.readListFromFile(inboxPath, MAIL_LIST_TYPE);

        PriorityQueue<mail> priorityQueue = new PriorityQueue<>(new EmailPriorityComparator());
        priorityQueue.addAll(emails);

        // Extract emails from priority queue in sorted order
        List<mail> sortedEmails = new ArrayList<>();
        while (!priorityQueue.isEmpty()) {
            sortedEmails.add(priorityQueue.poll());
        }

        return sortedEmails;
    }

    /**
     * Compose and send a new email
     */
    public void composeMail(mailContentDTO mailContent) throws UserNotFoundException {
        String currentUser = getLoggedInUser();
        String sentPath = BasePath + currentUser + "/sent.json";
        mail mail = mailFactory.createNewMail(mailContent);
        mail.setFrom(currentUser);

        // Add to sent folder
        Queue<String> recipientsQueue = mailContent.getRecipients();
        String receiver = recipientsQueue.peek();
        
        if (!(jsonFileManager.userExists(receiver))|| receiver.equals(currentUser)) {
            System.out.println("this email{ " + receiver + " } isn't found in our system");
            throw new UserNotFoundException("Email address " + receiver + " is not registered in our system");
        }

        List<mail> sentMails = jsonFileManager.readListFromFile(sentPath, MAIL_LIST_TYPE);
        sentMails.add(mail);
        jsonFileManager.writeListToFile(sentPath, sentMails);

        // Send to all receivers
        mail.setTo(null);
        String path = BasePath + receiver + "/inbox.json";
        List<mail> inboxMails = jsonFileManager.readListFromFile(path, MAIL_LIST_TYPE);
        inboxMails.add(mail);
        jsonFileManager.writeListToFile(path, inboxMails);
    }

    public void saveDraft(mailContentDTO mailContent) {
        String draftPath = BasePath + getLoggedInUser() + "/draft.json";
        List<mail> draftMails = jsonFileManager.readListFromFile(draftPath, MAIL_LIST_TYPE);
        mail mail = mailFactory.createNewMail(mailContent);
        draftMails.add(mail);
        jsonFileManager.writeListToFile(draftPath, draftMails);
    }

    private final Object trashLock = new Object();
    private final Object folderLock = new Object();

    /**
     * Move an email from one folder to another
     */
    public boolean moveEmail(int id, String fromFolder, String toFolder) {
        System.out.println("=== MOVE EMAIL START ===");
        System.out.println("Email ID: " + id);
        System.out.println("From Folder: " + fromFolder);
        System.out.println("To Folder: " + toFolder);

        try {
            String currentUser = getLoggedInUser();
            String fromFolderPath = BasePath + currentUser + "/" + fromFolder + ".json";
            String toFolderPath = BasePath + currentUser + "/" + toFolder + ".json";

            // Synchronize to prevent concurrent modification
            synchronized (folderLock) {
                // Read from source folder
                List<mail> fromEmails = jsonFileManager.readListFromFile(fromFolderPath, MAIL_LIST_TYPE);

                if (fromEmails == null) {
                    fromEmails = new ArrayList<>();
                }

                // Find the email to move
                mail emailToMove = null;
                for (mail email : fromEmails) {
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
                List<mail> toEmails = jsonFileManager.readListFromFile(toFolderPath, MAIL_LIST_TYPE);

                if (toEmails == null) {
                    toEmails = new ArrayList<>();
                }

                toEmails.add(emailToMove);
                boolean addSuccess = jsonFileManager.writeListToFile(toFolderPath, toEmails);

                if (!addSuccess) {
                    System.err.println("Failed to add to folder: " + toFolderPath);
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
    public List<mail> getCustomFolderEmails(String folderId) {
        String folderPath = BasePath + getLoggedInUser() + "/folder_" + folderId + ".json";
        return jsonFileManager.readListFromFile(folderPath, MAIL_LIST_TYPE);
    }
}