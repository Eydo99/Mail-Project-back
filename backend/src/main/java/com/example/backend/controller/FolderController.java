package com.example.backend.controller;

import com.example.backend.DTOS.FolderRequestDTO;
import com.example.backend.DTOS.FolderResponseDTO;
import com.example.backend.DTOS.mailDTO;
import com.example.backend.service.FolderService;
import com.example.backend.service.mailService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/folders")
@CrossOrigin(origins = "http://localhost:4200")
public class FolderController {

    @Autowired
    private FolderService folderService;

    @Autowired
    private mailService mailService;

    private final String loggedInUser = "belal@gmail.com"; // Temporary mocked user

    /*
     Get all folders for a user
     GET /api/folders
     */
    @GetMapping
    public ResponseEntity<List<FolderResponseDTO>> getAllFolders() {
        List<FolderResponseDTO> folders = folderService.getAllFolders(loggedInUser);
        return ResponseEntity.ok(folders);
    }

    /*
      Get folder by ID
      GET /api/folders/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<FolderResponseDTO> getFolderById(@PathVariable String id) {
        Optional<FolderResponseDTO> folder = folderService.getFolderById(loggedInUser, id);
        return folder.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /*
      Create new folder
      POST /api/folders
     */
    @PostMapping
    public ResponseEntity<FolderResponseDTO> createFolder(@Valid @RequestBody FolderRequestDTO dto) {
        mailService.setSenderEmail(loggedInUser);
        FolderResponseDTO created = folderService.createFolder(loggedInUser, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /*
      Update folder
      PUT /api/folders/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateFolder(
            @PathVariable String id,
            @Valid @RequestBody FolderRequestDTO dto) {
        Optional<FolderResponseDTO> updated = folderService.updateFolder(loggedInUser, id, dto);
        if (updated.isPresent()) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    /*
      Delete folder
      DELETE /api/folders/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFolder(@PathVariable String id) {
        mailService.setSenderEmail(loggedInUser);
        boolean deleted = folderService.deleteFolder(loggedInUser, id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    /*
      Get all emails in a folder
      GET /api/folders/{id}/emails
     */
    @GetMapping("/{id}/emails")
    public ResponseEntity<List<mailDTO>> getEmailsByFolder(@PathVariable String id) {
        mailService.setSenderEmail(loggedInUser);
        List<mailDTO> emails = mailService.getCustomFolderEmails(id);
        return ResponseEntity.ok(emails);
    }

    /*
      Update folder email count
      PATCH /api/folders/{id}/count
     */
    @PatchMapping("/{id}/count")
    public ResponseEntity<Void> updateFolderCount(
            @PathVariable String id,
            @RequestBody Map<String, Integer> request) {
        folderService.updateFolderCount(loggedInUser, id, request.get("count"));
        return ResponseEntity.ok().build();
    }

    /*
      Increment folder email count
      PATCH /api/folders/{id}/increment
     */
    @PatchMapping("/{id}/increment")
    public ResponseEntity<Void> incrementFolderCount(
            @PathVariable String id,
            @RequestBody Map<String, Integer> request) {
        folderService.incrementFolderCount(loggedInUser, id, request.get("increment"));
        return ResponseEntity.ok().build();
    }


    // Exception handler for validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }
}