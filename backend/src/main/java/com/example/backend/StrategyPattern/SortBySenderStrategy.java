package com.example.backend.StrategyPattern;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.example.backend.model.mail;

/**
 * Sort emails by sender email address
 */
public class SortBySenderStrategy implements EmailSortStrategy {
    private final boolean ascending;

    public SortBySenderStrategy(boolean ascending) {
        this.ascending = ascending;
    }

    @Override
    public void sort(List<mail> emails) {
        if (emails == null || emails.isEmpty()) {
            return;
        }

        Comparator<mail> comparator = Comparator.comparing(
                mail::getFrom,
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
        );

        if (!ascending) {
            comparator = comparator.reversed();
        }

        Collections.sort(emails, comparator);
    }

    @Override
    public String getStrategyName() {
        return ascending ? "sender-asc" : "sender-desc";
    }
}