package com.example.backend.controller;

import com.example.backend.DTOS.contactRequestDTO;
import com.example.backend.DTOS.contactResponseDTO;
import com.example.backend.DTOS.PaginatedContactResponse;
import com.example.backend.service.ContactService;
import jakarta.servlet.http.HttpServletRequest;
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
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class ContactController {

    @Autowired
    private ContactService service;

    // REMOVE THIS:
    // @Autowired
    // private HttpSession session;

    private String getLoggedInUser(HttpServletRequest request) {
        String email = (String) request.getSession().getAttribute("currentUser");
        System.out.println("ContactController - Getting logged in user: " + email);
        System.out.println("Session ID: " + request.getSession().getId());
        return email;
    }

    @GetMapping
    public PaginatedContactResponse getContacts(@RequestParam int page,
                                                @RequestParam int size,
                                                @RequestParam(required = false) String search,
                                                @RequestParam(defaultValue = "name") String sortBy,
                                                HttpServletRequest request) {
        return service.getContacts(getLoggedInUser(request), page, size, search, sortBy);
    }

    @PostMapping
    public ResponseEntity<contactResponseDTO> addContact(@Valid @RequestBody contactRequestDTO dto,
                                                         HttpServletRequest request) {
        contactResponseDTO response = service.addContact(getLoggedInUser(request), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateContact(@PathVariable String id,
                                              @Valid @RequestBody contactRequestDTO dto,
                                              HttpServletRequest request) {
        service.updateContact(getLoggedInUser(request), id, dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable String id,
                                              HttpServletRequest request) {
        service.deleteContact(getLoggedInUser(request), id);
        return ResponseEntity.noContent().build();
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