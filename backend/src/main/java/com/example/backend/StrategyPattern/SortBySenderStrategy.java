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
        //check if list is empty or null
        if (emails == null || emails.isEmpty()) {
            return;
        }
        //define a comparator and its rules
        Comparator<mail> comparator = Comparator.comparing(
                mail::getFrom,
                //if mail.getFrom()=null put last
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
        );
        //if sort in descending reverse the list
        if (!ascending) {
            comparator = comparator.reversed();
        }
        //apply the comparator on the emails list
        Collections.sort(emails, comparator);
    }

    @Override
    public String getStrategyName() {
        return ascending ? "sender-asc" : "sender-desc";
    }
}