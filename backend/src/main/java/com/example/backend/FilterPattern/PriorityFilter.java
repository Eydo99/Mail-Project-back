package com.example.backend.FilterPattern;

import com.example.backend.model.mail;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Filter emails by priority levels using a PriorityQueue
 */
public class PriorityFilter extends AbstractEmailFilter {
    private final List<Integer> priorities;

    public PriorityFilter(List<Integer> priorities) {
        this.priorities = priorities;
    }

    @Override
    public List<mail> apply(List<mail> emails) {
        //if no priorities exist then move to the next filter
        if (priorities == null || priorities.isEmpty()) {
            return passToNext(emails);
        }

        // Create a PriorityQueue that sorts emails by priority (lowest number = highest priority)
        PriorityQueue<mail> pq = new PriorityQueue<>(Comparator.comparingInt(mail::getPriority));

        // Add only emails that match the specified priorities
        for (mail email : emails) {
            if (priorities.contains(email.getPriority())) {
                pq.offer(email);
            }
        }

        // Extract emails from priority queue in sorted order
        List<mail> filtered = new ArrayList<>();
        while (!pq.isEmpty()) {
            filtered.add(pq.poll());
        }
        //after filtering pass it to the next filter
        return passToNext(filtered);
    }
}