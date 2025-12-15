package com.example.backend.facade;

import com.example.backend.DTOS.ProfileUpdateRequest;
import com.example.backend.DTOS.PasswordChangeRequest;
import com.example.backend.DTOS.DispatcherSettingsDTO;
import com.example.backend.model.InfoPlus;
import com.example.backend.model.UserInfo;
import com.example.backend.service.ProfileCommandManager;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Component
public class UserProfileFacade {
    
    @Autowired
    private ProfileCommandManager commandManager;
    
    private final Gson gson = new Gson();
    
    private String getInfoPath(String email) {
        return "data/users/" + email + "/info.json";
    }
    
    private String getInfoPlusPath(String email) {
        return "data/users/" + email + "/infoplus.json";
    }
    
    public Map<String, Object> getProfile(String email) throws Exception {
        System.out.println("📋 Getting profile for: " + email);
        
        String infoJson = Files.readString(Paths.get(getInfoPath(email)));
        UserInfo userInfo = gson.fromJson(infoJson, UserInfo.class);
        
        InfoPlus infoPlus;
        String infoPlusPath = getInfoPlusPath(email);
        if (Files.exists(Paths.get(infoPlusPath))) {
            String infoPlusJson = Files.readString(Paths.get(infoPlusPath));
            infoPlus = gson.fromJson(infoPlusJson, InfoPlus.class);
        } else {
            System.out.println("⚠️  infoplus.json not found, creating default");
            infoPlus = new InfoPlus();
            try (FileWriter fw = new FileWriter(infoPlusPath)) {
                gson.toJson(infoPlus, fw);
            }
        }
        
        // Ensure dispatcher settings exist
        if (infoPlus.dispatcherSettings == null) {
            infoPlus.dispatcherSettings = new InfoPlus.DispatcherSettings();
            try (FileWriter fw = new FileWriter(infoPlusPath)) {
                gson.toJson(infoPlus, fw);
            }
        }
        
        Map<String, Object> profile = new HashMap<>();
        profile.put("firstName", userInfo.firstName != null ? userInfo.firstName : "");
        profile.put("lastName", userInfo.lastName != null ? userInfo.lastName : "");
        profile.put("fullName", (userInfo.firstName + " " + userInfo.lastName).trim());
        profile.put("email", userInfo.email);
        profile.put("phoneNumber", userInfo.phoneNumber);
        profile.put("birthDate", userInfo.birthDate);
        profile.put("jobTitle", infoPlus.jobTitle != null ? infoPlus.jobTitle : "");
        profile.put("phone", infoPlus.phone != null ? infoPlus.phone : "");
        profile.put("bio", infoPlus.bio != null ? infoPlus.bio : "");
        profile.put("profilePhoto", infoPlus.profilePhoto);
        
        System.out.println("✅ Profile loaded successfully");
        System.out.println("   Photo: " + (infoPlus.profilePhoto != null ? "Present" : "None"));
        
        return profile;
    }
    
    public void updateProfile(String email, ProfileUpdateRequest request) throws Exception {
        System.out.println("💾 Updating profile for: " + email);
        
        String infoPlusPath = getInfoPlusPath(email);
        InfoPlus currentInfoPlus;
        
        if (Files.exists(Paths.get(infoPlusPath))) {
            String json = Files.readString(Paths.get(infoPlusPath));
            currentInfoPlus = gson.fromJson(json, InfoPlus.class);
        } else {
            currentInfoPlus = new InfoPlus();
        }
        
        // Ensure dispatcher settings exist
        if (currentInfoPlus.dispatcherSettings == null) {
            currentInfoPlus.dispatcherSettings = new InfoPlus.DispatcherSettings();
        }
        
        boolean hasChanges = false;
        
        if (request.jobTitle != null) {
            String oldValue = currentInfoPlus.jobTitle != null ? currentInfoPlus.jobTitle : "";
            if (!request.jobTitle.equals(oldValue)) {
                commandManager.recordChange(email, "jobTitle", oldValue, request.jobTitle);
                currentInfoPlus.jobTitle = request.jobTitle;
                hasChanges = true;
            }
        }

        if (request.phone != null) {
            String oldValue = currentInfoPlus.phone != null ? currentInfoPlus.phone : "";
            if (!request.phone.equals(oldValue)) {
                commandManager.recordChange(email, "phone", oldValue, request.phone);
                currentInfoPlus.phone = request.phone;
                hasChanges = true;
            }
        }

        if (request.bio != null) {
            String oldValue = currentInfoPlus.bio != null ? currentInfoPlus.bio : "";
            if (!request.bio.equals(oldValue)) {
                commandManager.recordChange(email, "bio", oldValue, request.bio);
                currentInfoPlus.bio = request.bio;
                hasChanges = true;
            }
        }

        if (request.profilePhoto != null) {
            String photoToSave;
            
            if (request.profilePhoto.startsWith("data:image")) {
                photoToSave = request.profilePhoto;
                System.out.println("📸 Valid base64 photo detected, length: " + request.profilePhoto.length());
            }
            else if (request.profilePhoto.equals("assets/default-avatar.png") || 
                     request.profilePhoto.trim().isEmpty()) {
                photoToSave = null;
                System.out.println("📸 Default avatar detected, saving as null");
            }
            else {
                System.out.println("⚠️  Invalid photo format, saving as null: " + 
                    request.profilePhoto.substring(0, Math.min(50, request.profilePhoto.length())));
                photoToSave = null;
            }
            
            String oldValue = currentInfoPlus.profilePhoto;
            boolean photoChanged = (oldValue == null && photoToSave != null) ||
                                   (oldValue != null && !oldValue.equals(photoToSave)) ||
                                   (oldValue != null && photoToSave == null);
            
            if (photoChanged) {
                commandManager.recordChange(email, "profilePhoto", oldValue, photoToSave);
                currentInfoPlus.profilePhoto = photoToSave;
                hasChanges = true;
                
                if (photoToSave != null) {
                    System.out.println("✅ Profile photo WILL BE SAVED (base64, " + photoToSave.length() + " chars)");
                } else {
                    System.out.println("✅ Profile photo removed (set to null)");
                }
            } else {
                System.out.println("ℹ️  Photo unchanged");
            }
        }
        
        if (request.fullName != null && !request.fullName.trim().isEmpty()) {
            String infoPath = getInfoPath(email);
            String infoJson = Files.readString(Paths.get(infoPath));
            UserInfo userInfo = gson.fromJson(infoJson, UserInfo.class);
            
            String[] nameParts = request.fullName.trim().split("\\s+", 2);
            String newFirstName = nameParts[0];
            String newLastName = nameParts.length > 1 ? nameParts[1] : "";
            
            if (!newFirstName.equals(userInfo.firstName) || !newLastName.equals(userInfo.lastName)) {
                userInfo.firstName = newFirstName;
                userInfo.lastName = newLastName;
                
                try (FileWriter fw = new FileWriter(infoPath)) {
                    gson.toJson(userInfo, fw);
                }
                hasChanges = true;
                System.out.println("✅ Updated name in info.json");
            }
        }
        
        if (hasChanges) {
            try (FileWriter fw = new FileWriter(infoPlusPath)) {
                gson.toJson(currentInfoPlus, fw);
                fw.flush();
            }
            System.out.println("✅ Profile updated and WRITTEN to: " + infoPlusPath);
        } else {
            System.out.println("ℹ️  No changes to save");
        }
    }
    
    public void changePassword(String email, PasswordChangeRequest request) throws Exception {
        System.out.println("🔐 Changing password for: " + email);
        
        String infoPath = getInfoPath(email);
        String infoJson = Files.readString(Paths.get(infoPath));
        UserInfo userInfo = gson.fromJson(infoJson, UserInfo.class);
        
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder = 
            new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        
        if (!encoder.matches(request.currentPassword, userInfo.password)) {
            throw new Exception("Current password is incorrect");
        }
        
        userInfo.password = encoder.encode(request.newPassword);
        
        try (FileWriter fw = new FileWriter(infoPath)) {
            gson.toJson(userInfo, fw);
        }
        
        System.out.println("✅ Password changed successfully");
    }
    
    // ============================================================================
    // 🎮 DISPATCHER SETTINGS METHODS (NEW)
    // ============================================================================
    
    /**
     * Get Dispatcher settings for a user
     */
    public DispatcherSettingsDTO getDispatcherSettings(String email) throws Exception {
        System.out.println("🎮 Getting dispatcher settings for: " + email);
        
        String infoPlusPath = getInfoPlusPath(email);
        InfoPlus infoPlus;
        
        if (Files.exists(Paths.get(infoPlusPath))) {
            String json = Files.readString(Paths.get(infoPlusPath));
            infoPlus = gson.fromJson(json, InfoPlus.class);
            
            // Ensure dispatcher settings exist
            if (infoPlus.dispatcherSettings == null) {
                infoPlus.dispatcherSettings = new InfoPlus.DispatcherSettings();
                // Save the default settings
                try (FileWriter fw = new FileWriter(infoPlusPath)) {
                    gson.toJson(infoPlus, fw);
                }
            }
        } else {
            // Create new infoplus with default settings
            infoPlus = new InfoPlus();
            try (FileWriter fw = new FileWriter(infoPlusPath)) {
                gson.toJson(infoPlus, fw);
            }
        }
        
        // Convert to DTO
        InfoPlus.DispatcherSettings ds = infoPlus.dispatcherSettings;
        DispatcherSettingsDTO dto = new DispatcherSettingsDTO(
            ds.dispatcherModeEnabled,
            ds.showDispatcherTutorial,
            ds.dispatcherAutoOpen,
            ds.dispatcherPosition,
            ds.autoSummarizeUrgent,
            ds.smartReply,
            ds.priorityScoring,
            ds.showTimeline,
            ds.showMetrics
        );
        
        System.out.println("✅ Dispatcher settings loaded: enabled=" + ds.dispatcherModeEnabled);
        return dto;
    }
    
    /**
     * Update Dispatcher settings
     */
    public void updateDispatcherSettings(String email, DispatcherSettingsDTO dto) throws Exception {
        System.out.println("💾 Updating dispatcher settings for: " + email);
        
        String infoPlusPath = getInfoPlusPath(email);
        InfoPlus infoPlus;
        
        if (Files.exists(Paths.get(infoPlusPath))) {
            String json = Files.readString(Paths.get(infoPlusPath));
            infoPlus = gson.fromJson(json, InfoPlus.class);
            
            if (infoPlus.dispatcherSettings == null) {
                infoPlus.dispatcherSettings = new InfoPlus.DispatcherSettings();
            }
        } else {
            infoPlus = new InfoPlus();
        }
        
        // Update dispatcher settings
        InfoPlus.DispatcherSettings ds = infoPlus.dispatcherSettings;
        ds.dispatcherModeEnabled = dto.isDispatcherModeEnabled();
        ds.showDispatcherTutorial = dto.isShowDispatcherTutorial();
        ds.dispatcherAutoOpen = dto.isDispatcherAutoOpen();
        ds.dispatcherPosition = dto.getDispatcherPosition();
        ds.autoSummarizeUrgent = dto.isAutoSummarizeUrgent();
        ds.smartReply = dto.isSmartReply();
        ds.priorityScoring = dto.isPriorityScoring();
        ds.showTimeline = dto.isShowTimeline();
        ds.showMetrics = dto.isShowMetrics();
        
        // Write to file
        try (FileWriter fw = new FileWriter(infoPlusPath)) {
            gson.toJson(infoPlus, fw);
            fw.flush();
        }
        
        System.out.println("✅ Dispatcher settings saved: enabled=" + ds.dispatcherModeEnabled);
    }
    
    /**
     * Quick toggle for Dispatcher mode (most common operation)
     */
    public void toggleDispatcher(String email, boolean enabled) throws Exception {
        System.out.println("🔄 Toggling dispatcher to " + enabled + " for: " + email);
        
        DispatcherSettingsDTO currentSettings = getDispatcherSettings(email);
        currentSettings.setDispatcherModeEnabled(enabled);
        updateDispatcherSettings(email, currentSettings);
        
        System.out.println("✅ Dispatcher toggled successfully");
    }
    
    // ============================================================================
    // UNDO/REDO METHODS (existing)
    // ============================================================================
    
    public boolean undo(String email) {
        return commandManager.undo(email);
    }
    
    public boolean redo(String email) {
        return commandManager.redo(email);
    }
    
    public boolean canUndo(String email) {
        return commandManager.canUndo(email);
    }
    
    public boolean canRedo(String email) {
        return commandManager.canRedo(email);
    }
    
    public void clearHistory(String email) {
        commandManager.clearHistory(email);
    }
    
    public boolean hasChanges(String email) {
        return commandManager.hasChanges(email);
    }
}