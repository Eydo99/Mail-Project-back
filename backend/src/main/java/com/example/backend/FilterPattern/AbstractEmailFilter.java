// backend/src/main/java/com/example/backend/FilterPattern/AbstractEmailFilter.java

package com.example.backend.FilterPattern;

import com.example.backend.model.mail;
import java.util.List;

/**
 * avoid duplication in concrete classes
 */
public abstract class AbstractEmailFilter implements EmailFilter {
    protected EmailFilter next;

    //set the next filter
    @Override
    public void setNext(EmailFilter next) {
        this.next = next;
    }

    //check if there is another filter in the chain then apply this filter on current list of mails if not return the current list
    protected List<mail> passToNext(List<mail> emails) {
        if (next != null) {
            return next.apply(emails);
        }
        return emails;
    }
}