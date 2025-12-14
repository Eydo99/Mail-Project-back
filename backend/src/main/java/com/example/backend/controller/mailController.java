package com.example.backend.controller;

import com.example.backend.DTOS.attachementDTO;
import com.example.backend.DTOS.mailContentDTO;
import com.example.backend.DTOS.mailDTO;
import com.example.backend.Exceptions.UserNotFoundException;
import com.example.backend.service.mailService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

@RestController
@RequestMapping("/api/mail")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:59007", "http://localhost:65183", "http://localhost:53596"}, allowCredentials = "true")
public class mailController {

    private final mailService mailService;

    @Autowired
    public mailController(mailService mailService) {
        this.mailService = mailService;
    }

    /**
     * Get logged-in user from session - CRITICAL FIX
     */
    private String getLoggedInUser(HttpServletRequest request) {
        String email = (String) request.getSession().getAttribute("currentUser");
        System.out.println("📧 mailController - Getting logged in user: " + email);
        System.out.println("📧 Session ID: " + request.getSession().getId());
        return email;
    }

    /**
     * Get all emails from inbox for current user
     */
    @GetMapping("/inbox")
    public ResponseEntity<List<mailDTO>> getInboxEmails(HttpServletRequest request) {
        try {
            String loggedInUser = getLoggedInUser(request);
            mailService.setSenderEmail(loggedInUser);
            
            System.out.println("📥 Getting inbox for: " + loggedInUser);
            List<mailDTO> emails = mailService.getInboxEmails();
            System.out.println("📥 Found " + emails.size() + " emails in inbox");
            
            return ResponseEntity.ok(emails);
        } catch (Exception e) {
            System.err.println("❌ Error getting inbox: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/inbox/priority")
    public ResponseEntity<List<mailDTO>> getInboxEmailsByPriority(HttpServletRequest request) {
        try {
            String loggedInUser = getLoggedInUser(request);
            mailService.setSenderEmail(loggedInUser);
            
            List<mailDTO> emails = mailService.getInboxEmailsByPriority();
            return ResponseEntity.ok(emails);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all sent emails for current user
     */
    @GetMapping("/sent")
    public ResponseEntity<List<mailDTO>> getSentEmails(HttpServletRequest request) {
        try {
            String loggedInUser = getLoggedInUser(request);
            mailService.setSenderEmail(loggedInUser);
            
            System.out.println("📤 Getting sent emails for: " + loggedInUser);
            List<mailDTO> emails = mailService.getSentEmails();
            System.out.println("📤 Found " + emails.size() + " sent emails");
            
            return ResponseEntity.ok(emails);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all draft emails for current user
     */
    @GetMapping("/draft")
    public ResponseEntity<List<mailDTO>> getDraftEmails(HttpServletRequest request) {
        try {
            String loggedInUser = getLoggedInUser(request);
            mailService.setSenderEmail(loggedInUser);
            
            List<mailDTO> emails = mailService.getDraftEmails();
            return ResponseEntity.ok(emails);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all trash emails for current user
     */
    @GetMapping("/trash")
    public ResponseEntity<List<mailDTO>> getTrashEmails(HttpServletRequest request) {
        try {
            String loggedInUser = getLoggedInUser(request);
            mailService.setSenderEmail(loggedInUser);
            
            List<mailDTO> emails = mailService.getTrashEmails();
            return ResponseEntity.ok(emails);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get a specific email by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<mailDTO> getEmailById(@PathVariable int id, 
                                                @RequestParam String folder,
                                                HttpServletRequest request) {
        try {
            String loggedInUser = getLoggedInUser(request);
            mailService.setSenderEmail(loggedInUser);
            
            mailDTO email = mailService.getEmailById(id, folder);
            if (email != null) {
                return ResponseEntity.ok(email);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Send/compose a new email
     */
    @PostMapping("/compose")
    public ResponseEntity<String> composeMail(@RequestBody mailContentDTO mailContent,
                                              HttpServletRequest request) {
        try {
            String loggedInUser = getLoggedInUser(request);
            mailService.setSenderEmail(loggedInUser);
            
            System.out.println("📨 Composing email from: " + loggedInUser);
            
            // Process attachments - decode base64 and save files
            if (mailContent.getAttachements() != null && !mailContent.getAttachements().isEmpty()) {
                for (attachementDTO attachment : mailContent.getAttachements()) {
                    try {
                        // Decode base64 from filePath
                        byte[] fileBytes = Base64.getDecoder().decode(attachment.getFilePath());

                        // Generate unique filename
                        String savedFilename = UUID.randomUUID().toString() + "_" + attachment.getFilename();

                        // Save to disk
                        String uploadDir = "data/uploads/";
                        File directory = new File(uploadDir);
                        if (!directory.exists()) {
                            directory.mkdirs();
                        }

                        java.nio.file.Path filePath = Paths.get(uploadDir + savedFilename);
                        Files.write(filePath, fileBytes);

                        // Update filePath to the actual server path
                        attachment.setFilePath(uploadDir + savedFilename);

                    } catch (Exception e) {
                        System.err.println("Error saving attachment: " + e.getMessage());
                    }
                }
            }

            Queue<String> recipientsQueue = mailContent.getRecipients();
            List<String> failedRecipients = new ArrayList<>();

            while (!recipientsQueue.isEmpty()) {
                Queue<String> temp = new LinkedList<>();
                String currentRecipient = recipientsQueue.poll();
                temp.add(currentRecipient);
                mailContent.setRecipients(temp);

                try {
                    mailService.composeMail(mailContent);
                    System.out.println("✅ Email sent to: " + currentRecipient);
                } catch (UserNotFoundException e) {
                    failedRecipients.add(currentRecipient);
                    System.out.println("❌ Failed to send to: " + currentRecipient);
                }
            }

            if (!failedRecipients.isEmpty()) {
                String failedEmails = String.join(", ", failedRecipients);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("The following email(s) are not registered in our system: " + failedEmails);
            }

            return ResponseEntity.ok("Email sent successfully");

        } catch (Exception e) {
            System.err.println("❌ Error sending email: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error sending email: " + e.getMessage());
        }
    }

    @PostMapping("/draft/save")
    public ResponseEntity<String> saveDraft(@RequestBody mailContentDTO mail,
                                           HttpServletRequest request) {
        try {
            String loggedInUser = getLoggedInUser(request);
            mailService.setSenderEmail(loggedInUser);
            
            mailService.saveDraft(mail);
            return ResponseEntity.ok("Email saved to draft successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to save email");
        }
    }

    /**
     * Delete an email (move to trash)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmail(@PathVariable int id, 
                                             @RequestParam String folder,
                                             HttpServletRequest request) {
        try {
            String loggedInUser = getLoggedInUser(request);
            mailService.setSenderEmail(loggedInUser);
            
            System.out.println("🗑️ Deleting email " + id + " from " + folder + " for user: " + loggedInUser);
            
            boolean success = mailService.deleteEmail(id, folder);
            if (success) {
                return ResponseEntity.ok("Email moved to trash");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("❌ Error deleting email: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete email");
        }
    }

    /**
     * Move an email from one folder to another
     */
    @PutMapping("/{id}/move")
    public ResponseEntity<String> moveEmail(
            @PathVariable int id,
            @RequestParam String fromFolder,
            @RequestParam String toFolder,
            HttpServletRequest request) {
        try {
            String loggedInUser = getLoggedInUser(request);
            mailService.setSenderEmail(loggedInUser);
            
            System.out.println("📦 Moving email " + id + " from " + fromFolder + " to " + toFolder + " for user: " + loggedInUser);
            
            boolean success = mailService.moveEmail(id, fromFolder, toFolder);
            if (success) {
                return ResponseEntity.ok("Email moved successfully");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("❌ Error moving email: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to move email: " + e.getMessage());
        }
    }
}