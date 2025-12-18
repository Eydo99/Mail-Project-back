package com.example.backend.StrategyPattern;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import com.example.backend.model.mail;

/**
 * Sort emails by priority (1=Urgent, 2=High, 3=Medium, 4=Low) using PriorityQueue
 */
public class SortByPriorityStrategy implements EmailSortStrategy {
    private final boolean ascending;

    public SortByPriorityStrategy(boolean ascending) {
        this.ascending = ascending;
    }

    @Override
    public void sort(List<mail> emails) {
        //check if list is empty or null
        if (emails == null || emails.isEmpty()) {
            return;
        }

        //define a comparator and its rules
        Comparator<mail> comparator = Comparator.comparingInt(mail::getPriority);
        //if sort in descending reverse the list
        if (!ascending) {
            comparator = comparator.reversed();
        }

        // Create PriorityQueue with the comparator
        PriorityQueue<mail> pq = new PriorityQueue<>(comparator);

        // Add all emails to the priority queue
        pq.addAll(emails);

        // Clear the original list
        emails.clear();

        // Extract emails from priority queue in sorted order
        while (!pq.isEmpty()) {
            emails.add(pq.poll());
        }
    }

    @Override
    public String getStrategyName() {
        return ascending ? "priority-asc" : "priority-desc";
    }
}