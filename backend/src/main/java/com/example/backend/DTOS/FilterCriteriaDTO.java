// backend/src/main/java/com/example/backend/dto/FilterCriteriaDTO.java

package com.example.backend.DTOS;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for filter criteria
 */
@Getter
@Setter
public class FilterCriteriaDTO {
    private String searchTerm;
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;
    private String sender;
    private List<Integer> priority;
    private Boolean hasAttachment;
    private Boolean isStarred;
    private String subjectContains;
    private String bodyContains;

    // Constructors
    public FilterCriteriaDTO() {}

}