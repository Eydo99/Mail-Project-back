package com.example.backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.backend.DTOS.mailDTO;

import lombok.Getter;
import lombok.Setter;

@Service
@Getter
@Setter
public class mailService {
    private String senderEmail = "dummy@gmail.com";
    private final String BasePath = "data/users/";
    private final FileService fileService;

    public mailService(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * Get all emails from inbox
     */
    public List<mailDTO> getInboxEmails() {
        String inboxPath = BasePath + senderEmail + "/inbox.json";
        return fileService.readMailsFromFile(inboxPath);
    }

    /**
     * Get all sent emails
     */
    public List<mailDTO> getSentEmails() {
        String sentPath = BasePath + senderEmail + "/sent.json";
        return fileService.readMailsFromFile(sentPath);
    }

    /**
     * Get all draft emails
     */
    public List<mailDTO> getDraftEmails() {
        String draftPath = BasePath + senderEmail + "/draft.json";
        return fileService.readMailsFromFile(draftPath);
    }

    /**
     * Get all trash emails
     */
    public List<mailDTO> getTrashEmails() {
        String trashPath = BasePath + senderEmail + "/trash.json";
        return fileService.readMailsFromFile(trashPath);
    }

    /**
     * Get specific email by ID from a folder
     */
    public mailDTO getEmailById(int id, String folder) {
        String folderPath = BasePath + senderEmail + "/" + folder + ".json";
        List<mailDTO> emails = fileService.readMailsFromFile(folderPath);

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
            List<mailDTO> inboxMails = fileService.readMailsFromFile(path);
            inboxMails.add(mail);
            fileService.writeMailsToFile(path, inboxMails);
        }

        // Add to sent folder
        List<mailDTO> sentMails = fileService.readMailsFromFile(sentPath);
        sentMails.add(mail);
        fileService.writeMailsToFile(sentPath, sentMails);
    }

    /**
     * Toggle star status of an email
     */
    public boolean toggleStar(int id, String folder) {
        String folderPath = BasePath + senderEmail + "/" + folder + ".json";
        List<mailDTO> emails = fileService.readMailsFromFile(folderPath);

        boolean found = false;
        for (mailDTO email : emails) {
            if (email.getId() == id) {
                email.setStarred(!email.isStarred());
                found = true;
                break;
            }
        }

        if (found) {
            fileService.writeMailsToFile(folderPath, emails);
        }

        return found;
    }

    /**
     * Delete an email (move to trash)
     */
    public boolean deleteEmail(int id, String folder) {
        if (folder.equals("trash")) {
            // Permanently delete from trash
            return permanentlyDeleteEmail(id);
        }

        String folderPath = BasePath + senderEmail + "/" + folder + ".json";
        List<mailDTO> emails = fileService.readMailsFromFile(folderPath);

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
            List<mailDTO> trashEmails = fileService.readMailsFromFile(trashPath);
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
        List<mailDTO> emails = fileService.readMailsFromFile(trashPath);

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