// backend/src/main/java/com/example/backend/FilterPattern/SearchFilter.java

package com.example.backend.FilterPattern;

import com.example.backend.model.mail;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Filter emails by search term (searches subject, body, sender, senderEmail)
 */
public class SearchFilter extends AbstractEmailFilter {
    private final String searchTerm;

    public SearchFilter(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    @Override
    public List<mail> apply(List<mail> emails) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return passToNext(emails);
        }

        String term = searchTerm.toLowerCase().trim();

        List<mail> filtered = emails.stream()
                .filter(email ->
                        (email.getSubject() != null && email.getSubject().toLowerCase().contains(term)) ||
                                (email.getBody() != null && email.getBody().toLowerCase().contains(term)) ||
                                (email.getFrom() != null && email.getFrom().toLowerCase().contains(term))
                              //  (email.getSenderEmail() != null && email.getSenderEmail().toLowerCase().contains(term))
                )
                .collect(Collectors.toList());

        return passToNext(filtered);
    }
}