package com.example.backend.service;

import java.lang.reflect.Type;
import java.util.ArrayList;
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
    private final JsonFileManager jsonFileManager;

    private static final Type MAIL_LIST_TYPE = new TypeToken<List<mailDTO>>(){}.getType();

    private final Object trashLock = new Object();
    private final Object folderLock = new Object();

    public mailService(JsonFileManager jsonFileManager) {
        this.jsonFileManager = jsonFileManager;
    }

    public List<mailDTO> getInboxEmails() {
        String inboxPath = BasePath + senderEmail + "/inbox.json";
        return jsonFileManager.readListFromFile(inboxPath, MAIL_LIST_TYPE);
    }

    public List<mailDTO> getSentEmails() {
        String sentPath = BasePath + senderEmail + "/sent.json";
        return jsonFileManager.readListFromFile(sentPath, MAIL_LIST_TYPE);
    }

    public List<mailDTO> getDraftEmails() {
        String draftPath = BasePath + senderEmail + "/draft.json";
        return jsonFileManager.readListFromFile(draftPath, MAIL_LIST_TYPE);
    }

    public List<mailDTO> getTrashEmails() {
        String trashPath = BasePath + senderEmail + "/trash.json";
        return jsonFileManager.readListFromFile(trashPath, MAIL_LIST_TYPE);
    }

    public mailDTO getEmailById(int id, String folder) {
        String folderPath = BasePath + senderEmail + "/" + folder + ".json";
        List<mailDTO> emails = jsonFileManager.readListFromFile(folderPath, MAIL_LIST_TYPE);

        if (emails == null) return null;

        return emails.stream()
                .filter(email -> email.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public void composeMail(mailDTO mail) {

        List<String> receiverList = mail.getTo();

        for (String receiver : receiverList) {
            String path = BasePath + receiver + "/inbox.json";
            List<mailDTO> inboxMails = jsonFileManager.readListFromFile(path, MAIL_LIST_TYPE);

            if (inboxMails == null) {
                inboxMails = new ArrayList<>();
            }

            inboxMails.add(mail);
            jsonFileManager.writeListToFile(path, inboxMails);
        }

        String sentPath = BasePath + senderEmail + "/sent.json";
        List<mailDTO> sentMails = jsonFileManager.readListFromFile(sentPath, MAIL_LIST_TYPE);

        if (sentMails == null) {
            sentMails = new ArrayList<>();
        }

        sentMails.add(mail);
        jsonFileManager.writeListToFile(sentPath, sentMails);
    }

    public void saveDraft(mailDTO mail) {

        String draftPath = BasePath + senderEmail + "/draft.json";

        List<mailDTO> draftMails = jsonFileManager.readListFromFile(draftPath, MAIL_LIST_TYPE);

        if (draftMails == null) {
            draftMails = new ArrayList<>();
        }

        draftMails.add(mail);
        jsonFileManager.writeListToFile(draftPath, draftMails);
    }

    // ================= DELETE EMAIL =================

    public boolean deleteEmail(int id, String folder) {

        try {
            if (folder.equals("trash")) {
                return permanentlyDeleteEmail(id);
            }

            String folderPath = BasePath + senderEmail + "/" + folder + ".json";
            String trashPath  = BasePath + senderEmail + "/trash.json";

            synchronized (folderLock) {

                List<mailDTO> emails = jsonFileManager.readListFromFile(folderPath, MAIL_LIST_TYPE);

                if (emails == null || emails.isEmpty()) {
                    return false;
                }

                mailDTO emailToDelete = null;

                for (mailDTO email : emails) {
                    if (email.getId() == id) {
                        emailToDelete = email;
                        break;
                    }
                }

                if (emailToDelete == null) {
                    return false;
                }

                // remove from folder
                emails.remove(emailToDelete);
                jsonFileManager.writeListToFile(folderPath, emails);

                // add to trash
                synchronized (trashLock) {
                    List<mailDTO> trashEmails = jsonFileManager.readListFromFile(trashPath, MAIL_LIST_TYPE);

                    if (trashEmails == null) {
                        trashEmails = new ArrayList<>();
                    }

                    trashEmails.add(emailToDelete);
                    jsonFileManager.writeListToFile(trashPath, trashEmails);
                }

                return true;
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to delete email", e);
        }
    }

    private boolean permanentlyDeleteEmail(int id) {

        String trashPath = BasePath + senderEmail + "/trash.json";

        try {
            synchronized (trashLock) {

                List<mailDTO> emails = jsonFileManager.readListFromFile(trashPath, MAIL_LIST_TYPE);

                if (emails == null || emails.isEmpty()) {
                    return false;
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
            throw new RuntimeException("Failed to permanently delete email", e);
        }
    }
}
