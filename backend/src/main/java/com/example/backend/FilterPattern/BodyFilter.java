// backend/src/main/java/com/example/backend/FilterPattern/BodyFilter.java

package com.example.backend.FilterPattern;

import com.example.backend.model.mail;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Filter emails by body contains
 */
public class BodyFilter extends AbstractEmailFilter {
    private final String bodyContains;

    public BodyFilter(String bodyContains) {
        this.bodyContains = bodyContains;
    }

    @Override
    public List<mail> apply(List<mail> emails) {
        if (bodyContains == null || bodyContains.trim().isEmpty()) {
            return passToNext(emails);
        }

        String term = bodyContains.toLowerCase().trim();

        List<mail> filtered = emails.stream()
                .filter(email ->
                        email.getBody() != null &&
                                email.getBody().toLowerCase().contains(term)
                )
                .collect(Collectors.toList());

        return passToNext(filtered);
    }
}