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
        //if isStarred not defined  then move to the next filter
        if (isStarred == null) {
            return passToNext(emails);
        }

        //filter the list of mails based on the sender field in the filter modal
        List<mail> filtered = emails.stream()
                .filter(email -> email.isStarred() == isStarred)
                .collect(Collectors.toList());
        //after filtering pass it to the next filter
        return passToNext(filtered);
    }
}