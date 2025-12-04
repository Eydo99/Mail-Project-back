package com.example.backend.service;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.backend.DTOS.mailDTO;
import com.google.gson.reflect.TypeToken;

import lombok.Getter;
import lombok.Setter;

@Service
@Getter
@Setter
public class mailService {
    private String senderEmail = "dummy@gmail.com";
    private final String BasePath = "data/users/";
    private final FileService fileService;
    
    // Type token for List<mailDTO> - needed for generic FileService methods
    private static final Type MAIL_LIST_TYPE = new TypeToken<List<mailDTO>>(){}.getType();

    public mailService(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * Get all emails from inbox
     */
    public List<mailDTO> getInboxEmails() {
        String inboxPath = BasePath + senderEmail + "/inbox.json";
        return fileService.readMailsFromFile(inboxPath, MAIL_LIST_TYPE);
    }

    /**
     * Get all sent emails
     */
    public List<mailDTO> getSentEmails() {
        String sentPath = BasePath + senderEmail + "/sent.json";
        return fileService.readMailsFromFile(sentPath, MAIL_LIST_TYPE);
    }

    /**
     * Get all draft emails
     */
    public List<mailDTO> getDraftEmails() {
        String draftPath = BasePath + senderEmail + "/draft.json";
        return fileService.readMailsFromFile(draftPath, MAIL_LIST_TYPE);
    }

    /**
     * Get all trash emails
     */
    public List<mailDTO> getTrashEmails() {
        String trashPath = BasePath + senderEmail + "/trash.json";
        return fileService.readMailsFromFile(trashPath, MAIL_LIST_TYPE);
    }

    /**
     * Get specific email by ID from a folder
     */
    public mailDTO getEmailById(int id, String folder) {
        String folderPath = BasePath + senderEmail + "/" + folder + ".json";
        List<mailDTO> emails = fileService.readMailsFromFile(folderPath, MAIL_LIST_TYPE);

        return emails.stream()
                .filter(email -> email.getId() == id)
                .findFirst()
                .orElse(null);
    }

    /**
     * Compose and send a new email
     */


    //factory is needed
    public void composeMail(mailDTO mail) {
        String sentPath = BasePath + senderEmail + "/sent.json";
        List<String> receiverList = mail.getTo();

        // Send to all receivers
        for (String receiver : receiverList) {
            String path = BasePath + receiver + "/inbox.json";
            List<mailDTO> inboxMails = fileService.readMailsFromFile(path, MAIL_LIST_TYPE);
            inboxMails.add(mail);
            fileService.writeMailsToFile(path, inboxMails);
        }

        // Add to sent folder
        List<mailDTO> sentMails = fileService.readMailsFromFile(sentPath, MAIL_LIST_TYPE);
        sentMails.add(mail);
        fileService.writeMailsToFile(sentPath, sentMails);
    }

    // /**
    //  * Toggle star status of an email
    //  */
    // public boolean toggleStar(int id, String folder) {
    //     String folderPath = BasePath + senderEmail + "/" + folder + ".json";
    //     List<mailDTO> emails = fileService.readMailsFromFile(folderPath, MAIL_LIST_TYPE);

    //     boolean found = false;
    //     for (mailDTO email : emails) {
    //         if (email.getId() == id) {
    //             email.setStared(!email.isStared());
    //             found = true;
    //             break;
    //         }
    //     }

    //     if (found) {
    //         fileService.writeMailsToFile(folderPath, emails);
    //     }

    //     return found;
    // }

    /**
     * Delete an email (move to trash)
     */
    public boolean deleteEmail(int id, String folder) {
        if (folder.equals("trash")) {
            // Permanently delete from trash
            return permanentlyDeleteEmail(id);
        }

        String folderPath = BasePath + senderEmail + "/" + folder + ".json";
        List<mailDTO> emails = fileService.readMailsFromFile(folderPath, MAIL_LIST_TYPE);

        // Find and remove the email
        mailDTO emailToDelete = null;
        for (mailDTO email : emails) {
            if (email.getId() == id) {
                emailToDelete = email;
                break;
            }
        }

        if (emailToDelete != null) {
            emails.remove(emailToDelete);
            fileService.writeMailsToFile(folderPath, emails);

            // Add to trash
            String trashPath = BasePath + senderEmail + "/trash.json";
            List<mailDTO> trashEmails = fileService.readMailsFromFile(trashPath, MAIL_LIST_TYPE);
            trashEmails.add(emailToDelete);
            fileService.writeMailsToFile(trashPath, trashEmails);

            return true;
        }

        return false;
    }

    /**
     * Permanently delete an email from trash
     */
    private boolean permanentlyDeleteEmail(int id) {
        String trashPath = BasePath + senderEmail + "/trash.json";
        List<mailDTO> emails = fileService.readMailsFromFile(trashPath, MAIL_LIST_TYPE);

        List<mailDTO> filteredEmails = emails.stream()
                .filter(email -> email.getId() != id)
                .collect(Collectors.toList());

        if (filteredEmails.size() < emails.size()) {
            fileService.writeMailsToFile(trashPath, filteredEmails);
            return true;
        }

        return false;
    }
}