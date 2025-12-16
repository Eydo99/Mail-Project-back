// backend/src/main/java/com/example/backend/FilterPattern/StarredFilter.java

package com.example.backend.FilterPattern;

import com.example.backend.model.mail;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Filter emails by starred status
 */
public class StarredFilter extends AbstractEmailFilter {
    private final Boolean isStarred;

    public StarredFilter(Boolean isStarred) {
        this.isStarred = isStarred;
    }

    @Override
    public List<mail> apply(List<mail> emails) {
        if (isStarred == null) {
            return passToNext(emails);
        }

        List<mail> filtered = emails.stream()
                .filter(email -> email.isStarred() == isStarred)
                .collect(Collectors.toList());

        return passToNext(filtered);
    }
}