// backend/src/main/java/com/example/backend/FilterPattern/SenderFilter.java

package com.example.backend.FilterPattern;

import com.example.backend.model.mail;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Filter emails by sender
 */
public class SenderFilter extends AbstractEmailFilter {
    private final String sender;

    public SenderFilter(String sender) {
        this.sender = sender;
    }

    @Override
    public List<mail> apply(List<mail> emails) {
        if (sender == null || sender.trim().isEmpty()) {
            return passToNext(emails);
        }

        String senderLower = sender.toLowerCase().trim();

        List<mail> filtered = emails.stream()
                .filter(email ->
                        (email.getFrom() != null && email.getFrom().toLowerCase().contains(senderLower))
                )
                .collect(Collectors.toList());

        return passToNext(filtered);
    }
}