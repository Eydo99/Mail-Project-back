package com.example.backend.controller;

import com.example.backend.DTOS.mailDTO;
import com.example.backend.service.mailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mail")
@CrossOrigin(origins = "http://localhost:4200")
public class mailController {

    private final mailService mailService;

    @Autowired
    public mailController(mailService mailService) {
        this.mailService = mailService;
    }

    /**
     * Get all emails from inbox for current user
     * GET /api/mail/inbox
     */
    @GetMapping("/inbox")
    public ResponseEntity<List<mailDTO>> getInboxEmails() {
        try {
            List<mailDTO> emails = mailService.getInboxEmails();
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
    public ResponseEntity<List<mailDTO>> getSentEmails() {
        try {
            List<mailDTO> emails = mailService.getSentEmails();
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
    public ResponseEntity<List<mailDTO>> getDraftEmails() {
        try {
            List<mailDTO> emails = mailService.getDraftEmails();
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
    public ResponseEntity<List<mailDTO>> getTrashEmails() {
        try {
            List<mailDTO> emails = mailService.getTrashEmails();
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
    public ResponseEntity<mailDTO> getEmailById(@PathVariable int id, @RequestParam String folder) {
        try {
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
     * POST /api/mail/compose
     */
    @PostMapping("/compose")
    public ResponseEntity<String> composeMail(@RequestBody mailDTO mail) {
        try {
            mailService.composeMail(mail);
            return ResponseEntity.ok("Email sent successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send email");
        }
    
    }
    
    @PostMapping("/draft/save")
    public ResponseEntity<String> saveDraft(@RequestBody mailDTO mail) {
        try {
            mailService.saveDraft(mail);
            return ResponseEntity.ok("Email saved successfully");
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
    // public ResponseEntity<String> toggleStar(@PathVariable int id, @RequestParam String folder) {
    //     try {
    //         boolean success = mailService.toggleStar(id, folder);
    //         if (success) {
    //             return ResponseEntity.ok("Star toggled successfully");
    //         }
    //         return ResponseEntity.notFound().build();
    //     } catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    //                 .body("Failed to toggle star");
    //     }
    // }

    /**
     * Delete an email (move to trash)
     * DELETE /api/mail/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmail(@PathVariable int id, @RequestParam String folder) {
        try {
            boolean success = mailService.deleteEmail(id, folder);
            if (success) {
                return ResponseEntity.ok("Email moved to trash");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete email");
        }
    }
}