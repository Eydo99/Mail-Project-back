package com.example.backend.StrategyPattern;

import com.example.backend.model.Contact;

import java.util.List;

/**
 * interface for contact sort strategies
 * implements the strategy pattern
 */
public interface contactSortStrategy {
    void sort(List<Contact> contacts);
}
