package com.example.backend.Factory;

import com.example.backend.DTOS.contactRequestDTO;
import com.example.backend.model.Contact;
import com.example.backend.model.Email;
import com.example.backend.model.Phone;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Factory for creating Contact entities
 * Encapsulates contact creation logic and ensures consistent initialization
 */
@Component
public class ContactFactory {

    private static final String[] AVATAR_COLORS = {
            "#3b82f6", "#8b5cf6", "#ec4899", "#10b981", "#f59e0b", "#ef4444"
    };

    private final Random random = new Random();

    /**
     * Creates a new Contact from request DTO
     *
     * @param dto The contact request data
     * @return A fully initialized Contact entity
     */
    public Contact createContact(contactRequestDTO dto) {
        Contact contact = new Contact();
        contact.setId(UUID.randomUUID().toString());
        contact.setName(dto.getName());
        contact.setEmail(dto.getEmails() != null ? dto.getEmails() : new ArrayList<>());
        contact.setPhone(dto.getPhones() != null ? dto.getPhones() : new ArrayList<>());
        contact.setInitials(generateInitials(dto.getName()));
        contact.setColour(generateRandomColor());

        // Ensure at least one primary if items exist
        ensurePrimaryEmail(contact.getEmail());
        ensurePrimaryPhone(contact.getPhone());

        return contact;
    }

    /**
     * Updates an existing Contact with new data
     * Note: Keeps the existing ID and color
     * Preserves primary flags from incoming DTO
     *
     * @param contact The contact to update
     * @param dto The new contact data
     */
    public void updateContact(Contact contact, contactRequestDTO dto) {
        contact.setName(dto.getName());

        // Update emails - preserve primary flags from DTO
        if (dto.getEmails() != null) {
            contact.setEmail(new ArrayList<>(dto.getEmails()));
            ensurePrimaryEmail(contact.getEmail());
        } else {
            contact.setEmail(new ArrayList<>());
        }

        // Update phones - preserve primary flags from DTO
        if (dto.getPhones() != null) {
            contact.setPhone(new ArrayList<>(dto.getPhones()));
            ensurePrimaryPhone(contact.getPhone());
        } else {
            contact.setPhone(new ArrayList<>());
        }

        contact.setInitials(generateInitials(dto.getName()));
        // Note: Keep existing color when updating
    }

    /**
     * Creates a contact with specific ID (useful for testing or migration)
     *
     * @param id The specific ID to use
     * @param dto The contact request data
     * @return A fully initialized Contact entity with specified ID
     */
    public Contact createContactWithId(String id, contactRequestDTO dto) {
        Contact contact = createContact(dto);
        contact.setId(id);
        return contact;
    }

    /**
     * Ensures at least one email is marked as primary
     * If no primary exists and list is not empty, marks first as primary
     */
    private void ensurePrimaryEmail(List<Email> emails) {
        if (emails == null || emails.isEmpty()) {
            return;
        }

        boolean hasPrimary = emails.stream().anyMatch(Email::isPrimary);
        if (!hasPrimary) {
            emails.get(0).setPrimary(true);
        }
    }

    /**
     * Ensures at least one phone is marked as primary
     * If no primary exists and list is not empty, marks first as primary
     */
    private void ensurePrimaryPhone(List<Phone> phones) {
        if (phones == null || phones.isEmpty()) {
            return;
        }

        boolean hasPrimary = phones.stream().anyMatch(Phone::isPrimary);
        if (!hasPrimary) {
            phones.get(0).setPrimary(true);
        }
    }

    /**
     * Generates initials from name (e.g., "John Doe" -> "JD")
     *
     * @param name The full name
     * @return Uppercase initials
     */
    private String generateInitials(String name) {
        return Arrays.stream(name.split(" "))
                .map(part -> part.substring(0, 1).toUpperCase())
                .reduce("", String::concat);
    }

    /**
     * Generates a random avatar color from predefined palette
     *
     * @return Hex color code
     */
    private String generateRandomColor() {
        return AVATAR_COLORS[random.nextInt(AVATAR_COLORS.length)];
    }
}