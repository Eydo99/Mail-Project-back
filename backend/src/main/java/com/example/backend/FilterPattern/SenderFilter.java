

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
        //if no sender exists then move to the next filter
        if (sender == null || sender.trim().isEmpty()) {
            return passToNext(emails);
        }

        String senderLower = sender.toLowerCase().trim();

        //filter the list of mails based on the sender field in the filter modal
        List<mail> filtered = emails.stream()
                .filter(email -> (email.getFrom() != null && email.getFrom().toLowerCase().contains(senderLower)))
                .collect(Collectors.toList());
        //after filtering pass it to the next filter
        return passToNext(filtered);
    }
}