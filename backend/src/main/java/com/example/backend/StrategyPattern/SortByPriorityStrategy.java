package com.example.backend.StrategyPattern;

import com.example.backend.DTOS.mailDTO;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Sort emails by priority (1=Urgent, 2=High, 3=Medium, 4=Low)
 */
public class SortByPriorityStrategy implements EmailSortStrategy {
    private final boolean ascending;

    public SortByPriorityStrategy(boolean ascending) {
        this.ascending = ascending;
    }

    @Override
    public void sort(List<mailDTO> emails) {
        if (emails == null || emails.isEmpty()) {
            return;
        }

        Comparator<mailDTO> comparator = Comparator.comparingInt(mailDTO::getPriority);

        if (!ascending) {
            comparator = comparator.reversed();
        }

        Collections.sort(emails, comparator);
    }

    @Override
    public String getStrategyName() {
        return ascending ? "priority-asc" : "priority-desc";
    }
}