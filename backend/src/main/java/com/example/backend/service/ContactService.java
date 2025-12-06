package com.example.backend.service;

import com.example.backend.DTOS.contactRequestDTO;
import com.example.backend.DTOS.PaginatedContactResponse;
import com.example.backend.Repo.contactRepo;
import com.example.backend.StrategyPattern.contactSortStrategy;
import com.example.backend.StrategyPattern.sortByEmail;
import com.example.backend.StrategyPattern.sortByName;
import com.example.backend.DTOS.contactResponseDTO;
import com.example.backend.Util.JsonFileManager;
import com.example.backend.model.Contact;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ContactService {

    @Autowired
    private contactRepo repository;

    @Autowired
    private JsonFileManager jsonFileManager;

    private contactSortStrategy getStrategy(String sortBy) {
        if ("email".equalsIgnoreCase(sortBy)) {
            return new sortByEmail();
        }
        return new sortByName();
    }

    public PaginatedContactResponse getContacts(String user, int page, int size,
                                                String search, String sortBy) {

        // Auto-create user folder if it doesn't exist
        if (!jsonFileManager.userExists(user)) {
            jsonFileManager.createUserFolder(user);
        }

        List<Contact> contacts = repository.findAll(user);

        if (search != null && !search.isBlank()) {
            String term = search.toLowerCase();
            contacts.removeIf(c -> {
                boolean nameMatch = c.getName() != null && c.getName().toLowerCase().contains(term);
                boolean emailMatch = c.getEmail() != null &&
                        c.getEmail().stream().anyMatch(e -> e.getAddress() != null &&
                                e.getAddress().toLowerCase().contains(term));
                return !nameMatch && !emailMatch;
            });
        }

        contactSortStrategy strategy = getStrategy(sortBy);
        strategy.sort(contacts);

        // Store total count before pagination
        int totalItems = contacts.size();

        int start = page * size;
        int end = Math.min(start + size, contacts.size());
        List<Contact> subList = (start < contacts.size()) ? contacts.subList(start, end) : new ArrayList<>();

        List<contactResponseDTO> dtos = new ArrayList<>();
        subList.forEach(contact -> dtos.add(mapToDTO(contact)));

        return new PaginatedContactResponse(dtos, totalItems);
    }

    public contactResponseDTO addContact(String user, contactRequestDTO dto) {
        List<Contact> contacts = repository.findAll(user);

        Contact newContact = mapToEntity(dto);
        contacts.add(newContact);
        repository.saveAll(user, contacts);

        return mapToDTO(newContact);
    }

    public void updateContact(String user, String id, contactRequestDTO dto) {
        List<Contact> contacts = repository.findAll(user);

        for (Contact c : contacts) {
            if (c.getId().equals(id)) {
                c.setName(dto.getName());
                c.setEmail(dto.getEmails() != null ? dto.getEmails() : new ArrayList<>());
                c.setPhone(dto.getPhones() != null ? dto.getPhones() : new ArrayList<>());
                c.setInitials(generateInitials(dto.getName()));
                repository.saveAll(user, contacts);
                return;
            }
        }
    }

    public void deleteContact(String user, String id) {
        List<Contact> contacts = repository.findAll(user);
        contacts.removeIf(c -> c.getId().equals(id));
        repository.saveAll(user, contacts);
    }

    private Contact mapToEntity(contactRequestDTO dto) {
        Contact c = new Contact();
        c.setId(UUID.randomUUID().toString());
        c.setName(dto.getName());
        c.setEmail(dto.getEmails() != null ? dto.getEmails() : new ArrayList<>());
        c.setPhone(dto.getPhones() != null ? dto.getPhones() : new ArrayList<>());
        c.setInitials(generateInitials(dto.getName()));
        c.setColour(generateRandomColor());
        return c;
    }

    private contactResponseDTO mapToDTO(Contact c) {
        contactResponseDTO dto = new contactResponseDTO();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setEmails(c.getEmail() != null ? c.getEmail() : new ArrayList<>());
        dto.setPhones(c.getPhone() != null ? c.getPhone() : new ArrayList<>());
        dto.setInitials(c.getInitials());
        dto.setAvatarColor(c.getColour());
        return dto;
    }

    private String generateInitials(String name) {
        return Arrays.stream(name.split(" "))
                .map(p -> p.substring(0, 1).toUpperCase())
                .reduce("", String::concat);
    }

    private String generateRandomColor() {
        String[] colors = {"#3b82f6","#8b5cf6","#ec4899","#10b981","#f59e0b","#ef4444"};
        return colors[new Random().nextInt(colors.length)];
    }
}