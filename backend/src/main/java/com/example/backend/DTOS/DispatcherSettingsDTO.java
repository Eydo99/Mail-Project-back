package com.example.backend.DTOS;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for Dispatcher Mode settings
 * Used for API communication between frontend and backend
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DispatcherSettingsDTO {
    private boolean dispatcherModeEnabled;
    private boolean showDispatcherTutorial;
    private boolean dispatcherAutoOpen;
    private String dispatcherPosition;
    private boolean autoSummarizeUrgent;
    private boolean smartReply;
    private boolean priorityScoring;
    private boolean showTimeline;
    private boolean showMetrics;
}