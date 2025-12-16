// backend/src/main/java/com/example/backend/service/EmailFilterService.java

package com.example.backend.service;

import com.example.backend.FilterPattern.*;
import com.example.backend.DTOS.FilterCriteriaDTO;
import com.example.backend.model.mail;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service to build and apply filter chain
 */
@Service
public class EmailFilterService {

    /**
     * Apply all filters to emails based on criteria
     */
    public List<mail> applyFilters(List<mail> emails, FilterCriteriaDTO criteria) {
        if (criteria == null || emails == null || emails.isEmpty()) {
            return emails;
        }

        // Build the filter chain
        EmailFilter chain = buildFilterChain(criteria);

        // Apply the chain
        return chain.apply(emails);
    }

    /**
     * Build the chain of responsibility for filters
     */
    private EmailFilter buildFilterChain(FilterCriteriaDTO criteria) {
        // Create all filters
        SearchFilter searchFilter = new SearchFilter(criteria.getSearchTerm());
        DateRangeFilter dateFilter = new DateRangeFilter(criteria.getDateFrom(), criteria.getDateTo());
        SenderFilter senderFilter = new SenderFilter(criteria.getSender());
        PriorityFilter priorityFilter = new PriorityFilter(criteria.getPriority());
        AttachmentFilter attachmentFilter = new AttachmentFilter(criteria.getHasAttachment());
        StarredFilter starredFilter = new StarredFilter(criteria.getIsStarred());
        SubjectFilter subjectFilter = new SubjectFilter(criteria.getSubjectContains());
        BodyFilter bodyFilter = new BodyFilter(criteria.getBodyContains());

        // Build the chain
        searchFilter.setNext(dateFilter);
        dateFilter.setNext(senderFilter);
        senderFilter.setNext(priorityFilter);
        priorityFilter.setNext(attachmentFilter);
        attachmentFilter.setNext(starredFilter);
        starredFilter.setNext(subjectFilter);
        subjectFilter.setNext(bodyFilter);

        return searchFilter;
    }

    /**
     * Check if any filters are active
     */
    public boolean hasActiveFilters(FilterCriteriaDTO criteria) {
        if (criteria == null) {
            return false;
        }

        return (criteria.getSearchTerm() != null && !criteria.getSearchTerm().trim().isEmpty()) ||
                criteria.getDateFrom() != null ||
                criteria.getDateTo() != null ||
                (criteria.getSender() != null && !criteria.getSender().trim().isEmpty()) ||
                (criteria.getPriority() != null && !criteria.getPriority().isEmpty()) ||
                criteria.getHasAttachment() != null ||
                criteria.getIsStarred() != null ||
                (criteria.getSubjectContains() != null && !criteria.getSubjectContains().trim().isEmpty()) ||
                (criteria.getBodyContains() != null && !criteria.getBodyContains().trim().isEmpty());
    }
}