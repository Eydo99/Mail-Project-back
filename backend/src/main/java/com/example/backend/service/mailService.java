package com.example.backend.service;

import java.util.List;

import com.example.backend.DTOS.mailDTO;

import lombok.AllArgsConstructor;


@AllArgsConstructor
public class mailService {
    private String senderEmail = "dummy@gmailcom";
    private final String BasePath = "data/users";
    private final FileService fileService;

    public mailService(FileService fileService) {
        this.fileService = fileService;

    }

    public void composeMail(mailDTO mail) {
        String sentPath = BasePath + senderEmail + "/sent.json";
        List<String> recieverList =mail.getTo() ;
        for(var reciever : recieverList){
            String path = BasePath+reciever+"/inbox.json";
            List<mailDTO> mails = fileService.readMailsFromFile(sentPath) ;
            mails.add(mail) ;
            fileService.writeMailsToFile(path, mails) ;

        }
        List<mailDTO> sentMails = fileService.readMailsFromFile(sentPath);
        sentMails.add(mail);
        fileService.writeMailsToFile(sentPath, sentMails);

    }
}
