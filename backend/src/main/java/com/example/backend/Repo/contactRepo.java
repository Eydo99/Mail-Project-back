package com.example.backend.Repo;

import com.example.backend.model.Contact;
import com.example.backend.Util.JsonFileManager;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Repository
public class contactRepo {

    @Autowired
    private JsonFileManager jsonFileManager; // JSON manager

    private static final String CONTACTS_FILE = "contacts";

    public List<Contact> findAll(String userEmail) {
        Type listType = new TypeToken<List<Contact>>() {}.getType();
        String path = jsonFileManager.getUserFolderPath(userEmail, CONTACTS_FILE);
        List<Contact> contacts = jsonFileManager.readListFromFile(path, listType);

        // Ensure all contacts have non-null email and phone lists
        contacts.forEach(contact -> {
            if (contact.getEmail() == null) {
                contact.setEmail(new ArrayList<>());
            }
            if (contact.getPhone() == null) {
                contact.setPhone(new ArrayList<>());
            }
        });

        return contacts;
    }

    public void saveAll(String userEmail, List<Contact> contacts) {
        String path = jsonFileManager.getUserFolderPath(userEmail, CONTACTS_FILE);
        jsonFileManager.writeListToFile(path, contacts);
    }
}