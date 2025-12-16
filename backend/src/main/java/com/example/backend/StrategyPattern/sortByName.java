package com.example.backend.StrategyPattern;

import com.example.backend.model.Contact;

import java.util.List;

public class sortByName  implements contactSortStrategy {

    @Override
    public void sort(List<Contact> contacts)
    {
        //sort contacts by na,e
        contacts.sort((a,b)->a.getName().compareToIgnoreCase(b.getName()));
    }
}
