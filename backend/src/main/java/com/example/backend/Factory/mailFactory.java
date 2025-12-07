package com.example.backend.Factory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

import com.example.backend.DTOS.mailContentDTO;
import com.example.backend.DTOS.mailDTO;

public class mailFactory {
    private static int idCounter = 0;
    private static final String COUNTER_FILE = "data/mail_counter.txt";
    
    // Static block to initialize idCounter from file when class is loaded
    static {
        idCounter = loadCounterFromFile();
    }
    
    private static int loadCounterFromFile() {
        try {
            File file = new File(COUNTER_FILE);
            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line = reader.readLine();
                reader.close();
                return line != null ? Integer.parseInt(line.trim()) : 0;
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading counter from file: " + e.getMessage());
        }
        return 0; // Default to 0 if file doesn't exist or error occurs
    }
    
    private static void saveCounterToFile() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(COUNTER_FILE));
            writer.write(String.valueOf(idCounter));
            writer.close();
        } catch (IOException e) {
            System.err.println("Error saving counter to file: " + e.getMessage());
        }
    }
    
    public static mailDTO createNewMail(mailContentDTO mailContent) {
        mailDTO mail = new mailDTO();
        mail.setId(++idCounter);
        mail.setSubject(mailContent.getSubject());
        mail.setBody(mailContent.getBody());
        mail.setTo(mailContent.getRecipients());
        mail.setTimestamp(LocalDateTime.now());
        mail.setStarred(false);
        mail.setHasAttachment(!(mailContent.getAttachements().isEmpty()));
        mail.setPriority(mailContent.getPiriority());
        mail.setPreview(mailContent.getBody().substring(0, Math.min(100, mailContent.getBody().length())));
        mail.setAttachments(mailContent.getAttachements());
        
        // Save the updated counter after creating new mail
        saveCounterToFile();
        
        return mail;
    }
}