package com.example.backend.StrategyPattern;

import com.example.backend.model.Contact;

import java.util.List;

public class sortByEmail implements contactSortStrategy {

    @Override
    public void sort(List<Contact> contacts)
    {
        contacts.sort((a,b)->
        {
            String emailA = a.getEmail().get(0).getAddress().toLowerCase();
            String emailB = b.getEmail().get(0).getAddress().toLowerCase();
            return emailA.compareTo(emailB);
        });
    }
}
