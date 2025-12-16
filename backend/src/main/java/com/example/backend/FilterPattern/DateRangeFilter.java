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
        if (dateFrom == null && dateTo == null) {
            return passToNext(emails);
        }

        List<mail> filtered = emails.stream()
                .filter(email -> {
                    LocalDateTime emailDate = email.getTimestamp();

                    if (emailDate == null) {
                        return false;
                    }

                    if (dateFrom != null && emailDate.isBefore(dateFrom)) {
                        return false;
                    }

                    if (dateTo != null) {
                        // Set to end of day
                        LocalDateTime endOfDay = dateTo.withHour(23).withMinute(59).withSecond(59);
                        if (emailDate.isAfter(endOfDay)) {
                            return false;
                        }
                    }

                    return true;
                })
                .collect(Collectors.toList());

        return passToNext(filtered);
    }
}