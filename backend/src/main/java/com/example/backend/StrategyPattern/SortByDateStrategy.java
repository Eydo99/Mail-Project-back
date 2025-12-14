package com.example.backend.StrategyPattern;


import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.example.backend.model.mail;

/**
 * Sort emails by date (timestamp)
 */
public class SortByDateStrategy implements EmailSortStrategy {
    private final boolean ascending;

    public SortByDateStrategy(boolean ascending) {
        this.ascending = ascending;
    }

    @Override
    public void sort(List<mail> emails) {
        if (emails == null || emails.isEmpty()) {
            return;
        }

        Comparator<mail> comparator = Comparator.comparing(
                mail::getTimestamp,
                Comparator.nullsLast(Comparator.naturalOrder())
        );

        if (!ascending) {
            comparator = comparator.reversed();
        }

        Collections.sort(emails, comparator);
    }

    @Override
    public String getStrategyName() {
        return ascending ? "date-asc" : "date-desc";
    }
}