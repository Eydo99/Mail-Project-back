// backend/src/main/java/com/example/backend/FilterPattern/SubjectFilter.java

package com.example.backend.FilterPattern;

import com.example.backend.model.mail;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Filter emails by subject contains
 */
public class SubjectFilter extends AbstractEmailFilter {
    private final String subjectContains;

    public SubjectFilter(String subjectContains) {
        this.subjectContains = subjectContains;
    }

    @Override
    public List<mail> apply(List<mail> emails) {
        if (subjectContains == null || subjectContains.trim().isEmpty()) {
            return passToNext(emails);
        }

        String term = subjectContains.toLowerCase().trim();

        List<mail> filtered = emails.stream()
                .filter(email ->
                        email.getSubject() != null &&
                                email.getSubject().toLowerCase().contains(term)
                )
                .collect(Collectors.toList());

        return passToNext(filtered);
    }
}