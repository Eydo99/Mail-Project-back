// backend/src/main/java/com/example/backend/FilterPattern/AbstractEmailFilter.java

package com.example.backend.FilterPattern;

import com.example.backend.model.mail;
import java.util.List;

public abstract class AbstractEmailFilter implements EmailFilter {
    protected EmailFilter next;

    @Override
    public void setNext(EmailFilter next) {
        this.next = next;
    }

    protected List<mail> passToNext(List<mail> emails) {
        if (next != null) {
            return next.apply(emails);
        }
        return emails;
    }
}