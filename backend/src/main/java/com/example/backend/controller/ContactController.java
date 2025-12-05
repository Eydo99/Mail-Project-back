package com.example.backend.controller;

import com.example.backend.DTOS.contactRequestDTO;
import com.example.backend.DTOS.contactResponseDTO;
import com.example.backend.DTOS.PaginatedContactResponse;
import com.example.backend.service.ContactService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/contacts")
@CrossOrigin
public class ContactController {

    @Autowired
    private ContactService service;

    private final String loggedInUser = "dummy@mail.com"; // Temporary mocked user
    private final String loggedInUser = "dummy@gmail.com"; // Temporary mocked user

    @GetMapping
    public PaginatedContactResponse getContacts(@RequestParam int page,
                                                @RequestParam int size,
                                                @RequestParam(required = false) String search,
                                                @RequestParam(defaultValue = "name") String sortBy) {
        return service.getContacts(loggedInUser, page, size, search, sortBy);
    }

    @PostMapping
    public ResponseEntity<contactResponseDTO> addContact(@Valid @RequestBody contactRequestDTO dto) {
        contactResponseDTO response = service.addContact(loggedInUser, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateContact(@PathVariable String id,
                                              @Valid @RequestBody contactRequestDTO dto) {
        service.updateContact(loggedInUser, id, dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable String id) {
        service.deleteContact(loggedInUser, id);
        return ResponseEntity.noContent().build();
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