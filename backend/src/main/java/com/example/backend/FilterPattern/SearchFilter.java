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
        //if nothing in search term exists then move to the next filter
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return passToNext(emails);
        }

        String term = searchTerm.toLowerCase().trim();
        //filter the list of mails based on the subject, body and sender field in the filter modal
        List<mail> filtered = emails.stream()
                .filter(email -> (email.getSubject() != null && email.getSubject().toLowerCase().contains(term)) ||
                        (email.getBody() != null && email.getBody().toLowerCase().contains(term)) ||
                        (email.getFrom() != null && email.getFrom().toLowerCase().contains(term)))
                .collect(Collectors.toList());
        //after filtering pass it to the next filter
        return passToNext(filtered);
    }
}