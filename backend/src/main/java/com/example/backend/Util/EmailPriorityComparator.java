package com.example.backend.Util;

import com.example.backend.model.mail;

import java.util.Comparator;

/**
 * Comparator for sorting emails by priority
 * Lower priority number = Higher importance (1 is most urgent, 4 is least)
 */
public class EmailPriorityComparator implements Comparator<mail> {

    @Override
    public int compare(mail email1, mail email2) {
        // Compare by priority first (ascending: 1, 2, 3, 4)
        int priorityComparison = Integer.compare(email1.getPriority(), email2.getPriority());

        if (priorityComparison != 0) {
            return priorityComparison;
        }

        // If priorities are equal, sort by timestamp (newest first)
        return email2.getTimestamp().compareTo(email1.getTimestamp());
    }
}