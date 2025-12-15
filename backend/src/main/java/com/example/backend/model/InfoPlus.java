package com.example.backend.model;

/**
 * Extended user profile information
 * Stores additional user preferences beyond basic account info
 */
public class InfoPlus {
    public String jobTitle;
    public String phone;
    public String bio;
    public String profilePhoto;
    
    // ✨ NEW: Dispatcher Mode Settings
    public DispatcherSettings dispatcherSettings;
    
    public InfoPlus() {
        this.jobTitle = "";
        this.phone = "";
        this.bio = "";
        this.profilePhoto = null;
        this.dispatcherSettings = new DispatcherSettings(); // Default settings
    }
    
    public InfoPlus(String jobTitle, String phone, String bio, String profilePhoto) {
        this.jobTitle = jobTitle;
        this.phone = phone;
        this.bio = bio;
        this.profilePhoto = profilePhoto;
        this.dispatcherSettings = new DispatcherSettings();
    }
    
    /**
     * Inner class for Dispatcher Mode configuration
     * All settings related to the Dispatcher feature
     */
    public static class DispatcherSettings {
        public boolean dispatcherModeEnabled;      // Is Dispatcher Mode turned on?
        public boolean showDispatcherTutorial;     // Show tutorial on first launch?
        public boolean dispatcherAutoOpen;         // Auto-open Dispatcher on login?
        public String dispatcherPosition;          // "right" | "bottom" | "overlay"
        
        // AI Feature toggles (for Phase 3)
        public boolean autoSummarizeUrgent;        // Auto-summarize urgent emails?
        public boolean smartReply;                 // Enable smart reply suggestions?
        public boolean priorityScoring;            // Enable AI priority scoring?
        
        // Display toggles (for Phase 4)
        public boolean showTimeline;               // Show activity timeline?
        public boolean showMetrics;                // Show metrics dashboard?
        
        /**
         * Default constructor with safe defaults
         * Everything OFF initially for opt-in experience
         */
        public DispatcherSettings() {
            this.dispatcherModeEnabled = false;
            this.showDispatcherTutorial = true;
            this.dispatcherAutoOpen = false;
            this.dispatcherPosition = "right";
            this.autoSummarizeUrgent = false;
            this.smartReply = false;
            this.priorityScoring = false;
            this.showTimeline = true;
            this.showMetrics = true;
        }
    }
}