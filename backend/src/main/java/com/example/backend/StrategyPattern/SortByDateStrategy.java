package com.example.backend.StrategyPattern;

import com.example.backend.DTOS.mailDTO;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Sort emails by date (timestamp)
 */
public class SortByDateStrategy implements EmailSortStrategy {
    private final boolean ascending;

    public SortByDateStrategy(boolean ascending) {
        this.ascending = ascending;
    }

    @Override
    public void sort(List<mailDTO> emails) {
        if (emails == null || emails.isEmpty()) {
            return;
        }

        Comparator<mailDTO> comparator = Comparator.comparing(
                mailDTO::getTimestamp,
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