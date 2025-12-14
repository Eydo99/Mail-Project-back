package com.example.backend.StrategyPattern;

import com.example.backend.model.mail;

import java.util.List;

/**
 * Strategy Pattern for Email Sorting
 * Define a family of sorting algorithms
 */
public interface EmailSortStrategy {
    /**
     * Sort emails based on specific criteria
     * @param emails List of emails to sort
     */
    void sort(List<mail> emails);

    /**
     * Get the name of this sorting strategy
     * @return Strategy name (e.g., "date", "sender", "subject")
     */
    String getStrategyName();
}