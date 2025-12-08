package com.example.backend.StrategyPattern;

import com.example.backend.DTOS.mailDTO;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Sort emails by subject
 */
public class SortBySubjectStrategy implements EmailSortStrategy {
    private final boolean ascending;

    public SortBySubjectStrategy(boolean ascending) {
        this.ascending = ascending;
    }

    @Override
    public void sort(List<mailDTO> emails) {
        if (emails == null || emails.isEmpty()) {
            return;
        }

        Comparator<mailDTO> comparator = Comparator.comparing(
                mailDTO::getSubject,
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