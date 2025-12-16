package com.example.backend.Repo;

import com.example.backend.model.Contact;
import com.example.backend.Util.JsonFileManager;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 *Repo for managing contacts in JSON files
 *middleman between the contact service and contact.json using json file manager
 */
@Repository
public class contactRepo {

    @Autowired
    private JsonFileManager jsonFileManager;

    private static final String contactsFile = "contacts";

    /**
     * get all contacts from contact.json
     * @param userEmail : folder of user to get its contacts
     * @return list of contacts
     */
    public List<Contact> findAll(String userEmail) {
        //determine type of list for json file manager
        Type listType = new TypeToken<List<Contact>>() {}.getType();
        //construct path of contact.json
        String path = jsonFileManager.getUserFolderPath(userEmail, contactsFile);
        //get list from contact.json
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

    /**
     * save list of emails to contact.json
     * @param userEmail : the email that the list will be saved into
     * @param contacts :list of contacts to be saved
     */
    public void saveAll(String userEmail, List<Contact> contacts) {
        String path = jsonFileManager.getUserFolderPath(userEmail, contactsFile);
        jsonFileManager.writeListToFile(path, contacts);
    }
}