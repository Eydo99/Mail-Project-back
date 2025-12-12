package com.example.backend.controller;

import com.example.backend.DTOS.attachementDTO;
import com.example.backend.DTOS.mailContentDTO;
import com.example.backend.Exceptions.UserNotFoundException;
import com.example.backend.Repo.mailRepo;
import com.example.backend.model.mail;
import com.example.backend.service.mailService;

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
@CrossOrigin(origins = "http://localhost:4200")
public class mailController {

    private final mailService mailService;
    private final mailRepo mailRepo;

    @Autowired
    public mailController(mailService mailService, mailRepo mailRepo) {
        this.mailService = mailService;
        this.mailRepo = mailRepo;
    }

    /**
     * Get all emails from inbox for current user
     * GET /api/mail/inbox
     */
    @GetMapping("/inbox")
    public ResponseEntity<List<mail>> getInboxEmails() {
        try {
            List<mail> emails = mailRepo.getInboxEmails();
            return ResponseEntity.ok(emails);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/inbox/priority")
    public ResponseEntity<List<mail>> getInboxEmailsByPriority() {
        try {
            List<mail> emails = mailService.getInboxEmailsByPriority();
            return ResponseEntity.ok(emails);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all sent emails for current user
     * GET /api/mail/sent
     */
    @GetMapping("/sent")
    public ResponseEntity<List<mail>> getSentEmails() {
        try {
            List<mail> emails = mailRepo.getSentEmails();
            return ResponseEntity.ok(emails);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all draft emails for current user
     * GET /api/mail/draft
     */
    @GetMapping("/draft")
    public ResponseEntity<List<mail>> getDraftEmails() {
        try {
            List<mail> emails = mailRepo.getDraftEmails();
            return ResponseEntity.ok(emails);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all trash emails for current user
     * GET /api/mail/trash
     */
    @GetMapping("/trash")
    public ResponseEntity<List<mail>> getTrashEmails() {
        try {
            List<mail> emails = mailRepo.getTrashEmails();
            return ResponseEntity.ok(emails);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get a specific email by ID
     * GET /api/mail/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<mail> getEmailById(@PathVariable int id, @RequestParam String folder) {
        try {
            mail email = mailRepo.getEmailById(id, folder);
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
     * POST /api/mail/compose
     */
    @PostMapping("/compose")
    public ResponseEntity<String> composeMail(@RequestBody mailContentDTO mailContent) {

        try { // <-- ADD THIS TRY HERE
              // Process attachments - decode base64 and save files
            if (mailContent.getAttachements() != null && !mailContent.getAttachements().isEmpty()) {
                for (attachementDTO attachment : mailContent.getAttachements()) {
                    try {
                        // Decode base64 from filePath
                        byte[] fileBytes = Base64.getDecoder().decode(attachment.getFilePath());

                        // Generate unique filename
                        String savedFilename = UUID.randomUUID().toString() + "_" + attachment.getFilename();

                        // Save to disk (change path as needed)
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
                } catch (UserNotFoundException e) {
                    failedRecipients.add(currentRecipient);
                }
            }

            if (!failedRecipients.isEmpty()) {
                String failedEmails = String.join(", ", failedRecipients);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("The following email(s) are not registered in our system: " + failedEmails);
            }

            return ResponseEntity.ok("Email sent successfully");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error sending email: " + e.getMessage());
        }
    }

    @PostMapping("/draft/save")
    public ResponseEntity<String> saveDraft(@RequestBody mailContentDTO mail) {
        try {
            mailService.saveDraft(mail);
            return ResponseEntity.ok("Email saved to dtaft successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to save email");
        }
    }

    /**
     * Toggle star status of an email
     * PUT /api/mail/{id}/star
     */
    // @PutMapping("/{id}/star")
    // public ResponseEntity<String> toggleStar(@PathVariable int id, @RequestParam
    // String folder) {
    // try {
    // boolean success = mailService.toggleStar(id, folder);
    // if (success) {
    // return ResponseEntity.ok("Star toggled successfully");
    // }
    // return ResponseEntity.notFound().build();
    // } catch (Exception e) {
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    // .body("Failed to toggle star");
    // }
    // }

    /**
     * Delete an email (move to trash)
     * DELETE /api/mail/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmail(@PathVariable int id, @RequestParam String folder) {
        try {
            boolean success = mailRepo.deleteEmail(id, folder);
            if (success) {
                return ResponseEntity.ok("Email moved to trash");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete email");
        }
    }

    /**
     * Move an email from one folder to another
     * PUT /api/mail/{id}/move
     */
    @PutMapping("/{id}/move")
    public ResponseEntity<String> moveEmail(
            @PathVariable int id,
            @RequestParam String fromFolder,
            @RequestParam String toFolder) {
        try {
            boolean success = mailService.moveEmail(id, fromFolder, toFolder);
            if (success) {
                return ResponseEntity.ok("Email moved successfully");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to move email: " + e.getMessage());
        }
    }

}