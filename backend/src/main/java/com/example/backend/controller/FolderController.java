package com.example.backend.controller;

import com.example.backend.DTOS.FolderRequestDTO;
import com.example.backend.DTOS.FolderResponseDTO;
import com.example.backend.model.mail;
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

    /**
     * helper method to get current loggedIn user from local storage
     * @param request : the HTTP servlet request
     * @return loggedIn email in local storage
     */
    private String getLoggedInUser(HttpServletRequest request) {
        String email = (String) request.getSession().getAttribute("currentUser");
        System.out.println("FolderController - Getting logged in user: " + email);
        return email;
    }

    /**
     * method to get all folders
     * @param request : needed to know the current loggedIn user
     * @return list of folders
     */
    @GetMapping
    public ResponseEntity<List<FolderResponseDTO>> getAllFolders(HttpServletRequest request) {
        List<FolderResponseDTO> folders = folderService.getAllFolders(getLoggedInUser(request));
        return ResponseEntity.ok(folders);
    }

    /**
     * get wanted folder
     * @param id :the id of the wanted folder
     * @param request :needed to know the current loggedIn user
     * @return wanted folder
     */
    @GetMapping("/{id}")
    public ResponseEntity<FolderResponseDTO> getFolderById(@PathVariable String id,
                                                           HttpServletRequest request) {
        //optional is used because folder can be null
        Optional<FolderResponseDTO> folder = folderService.getFolderById(getLoggedInUser(request), id);
        return folder.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * create new custom folder
     * @param dto : the info of the created folder
     * @param request : needed to know the current loggedIn user
     * @return new created folder
     */
    @PostMapping
    public ResponseEntity<FolderResponseDTO> createFolder(@Valid @RequestBody FolderRequestDTO dto,
                                                          HttpServletRequest request) {
        String loggedInUser = getLoggedInUser(request);
        mailService.setSenderEmail(loggedInUser);
        FolderResponseDTO created = folderService.createFolder(loggedInUser, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * update an existing folder
     * @param id : the id of the wanted folder
     * @param dto : the new info to be updated
     * @param request :needed to know the current loggedIn user
     */
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

    /**
     * delete a folder
     * @param id : the id of the deleted folder
     * @param request : needed to know the current loggedIn user
     */
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

    /**
     * get emails in a folder
     * @param id : id of folder
     * @param request :needed to know the current loggedIn user
     * @return list of mails
     */
    @GetMapping("/{id}/emails")
    public ResponseEntity<List<mail>> getEmailsByFolder(@PathVariable String id,
                                                        HttpServletRequest request) {
        try {
            String loggedInUser = getLoggedInUser(request);
            System.out.println("📁 FolderController: Getting emails for folder: " + id + " for user: " + loggedInUser);

            mailService.setSenderEmail(loggedInUser);

            // Call the simple version (no filters, default sort)
            List<mail> emails = mailService.getCustomFolderEmails(id);

            System.out.println("📁 FolderController: Found " + emails.size() + " emails");
            return ResponseEntity.ok(emails);
        } catch (Exception e) {
            System.err.println("❌ Error getting folder emails: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     *
     * @param id : the id of the updated folder
     * @param request : new no of mails
     * @param httpRequest: needed to know the current loggedIn user
     */
    @PatchMapping("/{id}/count")
    public ResponseEntity<Void> updateFolderCount(@PathVariable String id,
                                                  @RequestBody Map<String, Integer> request,
                                                  HttpServletRequest httpRequest) {
        folderService.updateFolderCount(getLoggedInUser(httpRequest), id, request.get("count"));
        return ResponseEntity.ok().build();
    }

    /**
     *
     * @param id : the id of the updated folder
     * @param request : new no of mails to be added
     * @param httpRequest: needed to know the current loggedIn user
     */
    @PatchMapping("/{id}/increment")
    public ResponseEntity<Void> incrementFolderCount(@PathVariable String id,
                                                     @RequestBody Map<String, Integer> request,
                                                     HttpServletRequest httpRequest) {
        folderService.incrementFolderCount(getLoggedInUser(httpRequest), id, request.get("increment"));
        return ResponseEntity.ok().build();
    }

    /**
     * handles @valid when the argument is not valid
     * @param ex : the exception to be  handled
     * @return map ot the exceptions that need to be handled
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        return getStringMap(ex);
    }

    static Map<String, String> getStringMap(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }


}