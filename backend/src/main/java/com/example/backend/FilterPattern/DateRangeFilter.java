// backend/src/main/java/com/example/backend/FilterPattern/DateRangeFilter.java

package com.example.backend.FilterPattern;

import com.example.backend.model.mail;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Filter emails by date range
 */
public class DateRangeFilter extends AbstractEmailFilter {
    private final LocalDateTime dateFrom;
    private final LocalDateTime dateTo;

    public DateRangeFilter(LocalDateTime dateFrom, LocalDateTime dateTo) {
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
    }

    @Override
    public List<mail> apply(List<mail> emails) {
        //if no date range exists then move to the next filter
        if (dateFrom == null && dateTo == null) {
            return passToNext(emails);
        }

        List<mail> filtered = emails.stream()
                .filter(email -> {
                    //get the date of the mail
                    LocalDateTime emailDate = email.getTimestamp();
                    //if the mail has no date remove it
                    if (emailDate == null) {
                        return false;
                    }
                    //if we have date from and date of the mail is before that date remove it
                    if (dateFrom != null && emailDate.isBefore(dateFrom)) {
                        return false;
                    }
                    //if we have a date To
                    if (dateTo != null) {
                        // Set to end of day
                        LocalDateTime endOfDay = dateTo.withHour(23).withMinute(59).withSecond(59);
                        //if email date is after the do date remove it
                        if (emailDate.isAfter(endOfDay)) {
                            return false;
                        }
                    }
                    //add it to the filtered list if the email date is within the date range
                    return true;
                })
                .collect(Collectors.toList());
        //after filtering pass it to the next filter
        return passToNext(filtered);
    }
}