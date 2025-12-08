package com.example.backend.controller;

import com.example.backend.DTOS.FolderRequestDTO;
import com.example.backend.DTOS.FolderResponseDTO;
import com.example.backend.DTOS.mailDTO;
import com.example.backend.service.FolderService;
import com.example.backend.service.mailService;
import jakarta.servlet.http.HttpServletRequest;
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
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class FolderController {

    @Autowired
    private FolderService folderService;

    @Autowired
    private mailService mailService;

    // REMOVE THIS:
    // @Autowired
    // private HttpSession session;

    private String getLoggedInUser(HttpServletRequest request) {
        String email = (String) request.getSession().getAttribute("currentUser");
        System.out.println("FolderController - Getting logged in user: " + email);
        return email;
    }

    @GetMapping
    public ResponseEntity<List<FolderResponseDTO>> getAllFolders(HttpServletRequest request) {
        List<FolderResponseDTO> folders = folderService.getAllFolders(getLoggedInUser(request));
        return ResponseEntity.ok(folders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FolderResponseDTO> getFolderById(@PathVariable String id,
                                                           HttpServletRequest request) {
        Optional<FolderResponseDTO> folder = folderService.getFolderById(getLoggedInUser(request), id);
        return folder.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<FolderResponseDTO> createFolder(@Valid @RequestBody FolderRequestDTO dto,
                                                          HttpServletRequest request) {
        String loggedInUser = getLoggedInUser(request);
        mailService.setSenderEmail(loggedInUser);
        FolderResponseDTO created = folderService.createFolder(loggedInUser, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateFolder(@PathVariable String id,
                                             @Valid @RequestBody FolderRequestDTO dto,
                                             HttpServletRequest request) {
        Optional<FolderResponseDTO> updated = folderService.updateFolder(getLoggedInUser(request), id, dto);
        if (updated.isPresent()) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFolder(@PathVariable String id,
                                             HttpServletRequest request) {
        String loggedInUser = getLoggedInUser(request);
        mailService.setSenderEmail(loggedInUser);
        boolean deleted = folderService.deleteFolder(loggedInUser, id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/emails")
    public ResponseEntity<List<mailDTO>> getEmailsByFolder(@PathVariable String id,
                                                           HttpServletRequest request) {
        String loggedInUser = getLoggedInUser(request);
        mailService.setSenderEmail(loggedInUser);
        List<mailDTO> emails = mailService.getCustomFolderEmails(id);
        return ResponseEntity.ok(emails);
    }

    @PatchMapping("/{id}/count")
    public ResponseEntity<Void> updateFolderCount(@PathVariable String id,
                                                  @RequestBody Map<String, Integer> request,
                                                  HttpServletRequest httpRequest) {
        folderService.updateFolderCount(getLoggedInUser(httpRequest), id, request.get("count"));
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/increment")
    public ResponseEntity<Void> incrementFolderCount(@PathVariable String id,
                                                     @RequestBody Map<String, Integer> request,
                                                     HttpServletRequest httpRequest) {
        folderService.incrementFolderCount(getLoggedInUser(httpRequest), id, request.get("increment"));
        return ResponseEntity.ok().build();
    }

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