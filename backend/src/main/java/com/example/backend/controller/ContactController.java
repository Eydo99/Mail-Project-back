package com.example.backend.controller;

import com.example.backend.DTOS.contactRequestDTO;
import com.example.backend.DTOS.contactResponseDTO;
import com.example.backend.DTOS.PaginatedContactResponse;
import com.example.backend.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
@CrossOrigin
public class ContactController {

    @Autowired
    private ContactService service;

    private final String loggedInUser = "dummy@gmail.com"; // Temporary mocked user

    @GetMapping
    public PaginatedContactResponse getContacts(@RequestParam int page,
                                                @RequestParam int size,
                                                @RequestParam(required = false) String search,
                                                @RequestParam(defaultValue = "name") String sortBy) {
        return service.getContacts(loggedInUser, page, size, search, sortBy);
    }

    @PostMapping
    public contactResponseDTO addContact(@RequestBody contactRequestDTO dto) {
        return service.addContact(loggedInUser, dto);
    }

    @PutMapping("/{id}")
    public void updateContact(@PathVariable String id, @RequestBody contactRequestDTO dto) {
        service.updateContact(loggedInUser, id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteContact(@PathVariable String id) {
        service.deleteContact(loggedInUser, id);
    }
}