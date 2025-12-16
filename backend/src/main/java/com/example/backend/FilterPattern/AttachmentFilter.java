// backend/src/main/java/com/example/backend/FilterPattern/AttachmentFilter.java

package com.example.backend.FilterPattern;

import com.example.backend.model.mail;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Filter emails by attachment presence
 */
public class AttachmentFilter extends AbstractEmailFilter {
    private final Boolean hasAttachment;

    public AttachmentFilter(Boolean hasAttachment) {
        this.hasAttachment = hasAttachment;
    }

    @Override
    public List<mail> apply(List<mail> emails) {
        if (hasAttachment == null) {
            return passToNext(emails);
        }

        List<mail> filtered = emails.stream()
                .filter(email -> email.isHasAttachment() == hasAttachment)
                .collect(Collectors.toList());

        return passToNext(filtered);
    }
}