package com.example.backend.service;

import java.util.List;

import com.example.backend.DTOS.mailDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class mailService {
    private String senderEmail = "dummy@gmail.com";
    private final String BasePath = "data/users/";
    private final FileService fileService;

    public mailService(FileService fileService) {
        this.fileService = fileService;

    }

    // add factory to create files
    public void composeMail(mailDTO mail) {
        String sentPath = BasePath + senderEmail + "/sent.json";
        List<String> recieverList = mail.getTo();
        // Queue needed here 
        for (var reciever : recieverList) {
            String path = BasePath + reciever + "/inbox.json";
            List<mailDTO> inboxMails = fileService.readMailsFromFile(path);
            inboxMails.add(mail);
            fileService.writeMailsToFile(path, inboxMails);
        }
        List<mailDTO> sentMails = fileService.readMailsFromFile(sentPath);
        sentMails.add(mail);
        fileService.writeMailsToFile(sentPath, sentMails);
    }
}
