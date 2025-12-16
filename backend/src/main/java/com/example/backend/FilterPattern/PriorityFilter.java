// backend/src/main/java/com/example/backend/FilterPattern/PriorityFilter.java

package com.example.backend.FilterPattern;

import com.example.backend.model.mail;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Filter emails by priority levels
 */
public class PriorityFilter extends AbstractEmailFilter {
    private final List<Integer> priorities;

    public PriorityFilter(List<Integer> priorities) {
        this.priorities = priorities;
    }

    @Override
    public List<mail> apply(List<mail> emails) {
        if (priorities == null || priorities.isEmpty()) {
            return passToNext(emails);
        }

        List<mail> filtered = emails.stream()
                .filter(email -> priorities.contains(email.getPriority()))
                .collect(Collectors.toList());

        return passToNext(filtered);
    }
}