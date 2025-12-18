package com.example.backend.StrategyPattern;

import com.example.backend.model.Contact;

import java.util.List;

/**
 * interface for contact sort strategies
 * implements the strategy pattern
 */
public interface contactSortStrategy {

    /**
     * Sort contacts based on specific criteria
     * @param contacts List of emails to sort
     */
    void sort(List<Contact> contacts);
}
