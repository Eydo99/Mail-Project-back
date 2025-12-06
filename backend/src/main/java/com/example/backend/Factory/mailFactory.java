package com.example.backend.Factory;

import java.time.LocalDateTime;

import com.example.backend.DTOS.mailContentDTO;
import com.example.backend.DTOS.mailDTO;

public class mailFactory {
    private static int idCounter=0 ;
       public static mailDTO createNewMail(mailContentDTO mailContent) {
        mailDTO mail = new mailDTO();
        mail.setId(++idCounter);
        mail.setSubject(mailContent.getSubject());
        mail.setBody(mailContent.getBody());
        mail.setTo(mailContent.getRecipients());
        mail.setTimestamp(LocalDateTime.now().toString());
        mail.setStarred(false);
        mail.setHasAttachment(!(mailContent.getAttachements().isEmpty()));
        mail.setPriority(mailContent.getPiriority());
        mail.setPreview(mailContent.getBody().substring(0, Math.min(100, mailContent.getBody().length())));
        mail.setAttachments(mailContent.getAttachements());
        return mail;
    }
}
