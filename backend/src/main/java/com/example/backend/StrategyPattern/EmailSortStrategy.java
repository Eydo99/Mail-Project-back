package com.example.backend.StrategyPattern;

import com.example.backend.model.mail;

import java.util.List;

/**
 * interface for emails sort strategies
 * implements the strategy pattern
 */
public interface EmailSortStrategy {
    /**
     * Sort emails based on specific criteria
     * @param emails List of emails to sort
     */
    void sort(List<mail> emails);

    /**
     * Get the name of the sorting strategy
     * @return Strategy name
     */
    String getStrategyName();
}