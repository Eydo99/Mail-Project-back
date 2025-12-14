package com.example.backend.facade;

import com.example.backend.DTOS.ProfileUpdateRequest;
import com.example.backend.DTOS.PasswordChangeRequest;
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

        // 🔧 CRITICAL FIX: Handle profilePhoto properly
        if (request.profilePhoto != null) {
            String photoToSave;
            
            // If it's a base64 data URL, save it
            if (request.profilePhoto.startsWith("data:image")) {
                photoToSave = request.profilePhoto;
                System.out.println("📸 Valid base64 photo detected, length: " + request.profilePhoto.length());
            }
            // If it's the default avatar placeholder, save as null
            else if (request.profilePhoto.equals("assets/default-avatar.png") || 
                     request.profilePhoto.trim().isEmpty()) {
                photoToSave = null;
                System.out.println("📸 Default avatar detected, saving as null");
            }
            // Otherwise, invalid format - save as null
            else {
                System.out.println("⚠️  Invalid photo format, saving as null: " + 
                    request.profilePhoto.substring(0, Math.min(50, request.profilePhoto.length())));
                photoToSave = null;
            }
            
            // Check if photo actually changed
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
        
        // Handle fullName update
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
        
        // 🔧 CRITICAL: Write to infoplus.json
        if (hasChanges) {
            try (FileWriter fw = new FileWriter(infoPlusPath)) {
                gson.toJson(currentInfoPlus, fw);
                fw.flush(); // Force write to disk
            }
            System.out.println("✅ Profile updated and WRITTEN to: " + infoPlusPath);
            System.out.println("📄 Content: jobTitle=" + currentInfoPlus.jobTitle + 
                             ", phone=" + currentInfoPlus.phone + 
                             ", bio=" + currentInfoPlus.bio + 
                             ", hasPhoto=" + (currentInfoPlus.profilePhoto != null));
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