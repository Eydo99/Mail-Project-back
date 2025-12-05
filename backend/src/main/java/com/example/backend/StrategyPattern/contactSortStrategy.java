package com.example.backend.StrategyPattern;

import com.example.backend.model.Contact;

import java.util.List;

public interface contactSortStrategy {
    void sort(List<Contact> contacts);
}
