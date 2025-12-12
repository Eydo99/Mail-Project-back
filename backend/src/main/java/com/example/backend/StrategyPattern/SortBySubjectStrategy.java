package com.example.backend.StrategyPattern;


import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.example.backend.model.mail;

/**
 * Sort emails by subject
 */
public class SortBySubjectStrategy implements EmailSortStrategy {
    private final boolean ascending;

    public SortBySubjectStrategy(boolean ascending) {
        this.ascending = ascending;
    }

    @Override
    public void sort(List<mail> emails) {
        if (emails == null || emails.isEmpty()) {
            return;
        }

        Comparator<mail> comparator = Comparator.comparing(
                mail::getSubject,
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
        );

        if (!ascending) {
            comparator = comparator.reversed();
        }

        Collections.sort(emails, comparator);
    }

    @Override
    public String getStrategyName() {
        return ascending ? "subject-asc" : "subject-desc";
    }
}