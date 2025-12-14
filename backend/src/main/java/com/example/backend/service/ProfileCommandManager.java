package com.example.backend.service;

import com.example.backend.model.InfoPlus;
import com.google.gson.Gson;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-Safe Command Manager for Profile Changes
 * Manages undo/redo stacks per user with proper synchronization
 */
@Service
public class ProfileCommandManager {
    
    private static final int MAX_HISTORY_SIZE = 50;
    
    private final Gson gson = new Gson();
    
    // FIXED: Use ConcurrentHashMap for thread safety
    private final Map<String, Stack<ProfileChange>> undoStacks = new ConcurrentHashMap<>();
    private final Map<String, Stack<ProfileChange>> redoStacks = new ConcurrentHashMap<>();
    
    // FIXED: Track last access time for cleanup
    private final Map<String, Long> lastAccessTime = new ConcurrentHashMap<>();
    private static final long HISTORY_TTL_MS = 30 * 60 * 1000; // 30 minutes
    
    /**
     * Inner class representing a single field change
     */
    public static class ProfileChange {
        public String email;
        public String fieldName;
        public String oldValue;
        public String newValue;
        
        public ProfileChange(String email, String fieldName, String oldValue, String newValue) {
            this.email = email;
            this.fieldName = fieldName;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }
    }
    
    /**
     * Record a change (add to undo stack)
     * FIXED: Added synchronization for thread safety
     */
    public void recordChange(String email, String fieldName, String oldValue, String newValue) {
        synchronized (this) {
            Stack<ProfileChange> undoStack = undoStacks.computeIfAbsent(email, k -> new Stack<>());
            Stack<ProfileChange> redoStack = redoStacks.computeIfAbsent(email, k -> new Stack<>());
            
            // Clear redo stack when new change is made
            redoStack.clear();
            
            // Add to undo stack
            ProfileChange change = new ProfileChange(email, fieldName, oldValue, newValue);
            undoStack.push(change);
            
            // FIXED: Limit stack size properly
            if (undoStack.size() > MAX_HISTORY_SIZE) {
                undoStack.remove(0);
            }
            
            // Update last access time
            lastAccessTime.put(email, System.currentTimeMillis());
            
            System.out.println("📝 Recorded change: " + fieldName + " for " + email);
        }
    }
    
    /**
     * Undo last change
     * FIXED: Added synchronization
     */
    public boolean undo(String email) {
        synchronized (this) {
            // FIXED: Clean up expired history before operation
            cleanupExpiredHistory(email);
            
            Stack<ProfileChange> undoStack = undoStacks.get(email);
            Stack<ProfileChange> redoStack = redoStacks.computeIfAbsent(email, k -> new Stack<>());
            
            if (undoStack == null || undoStack.isEmpty()) {
                System.out.println("⚠️ Nothing to undo for: " + email);
                return false;
            }
            
            ProfileChange change = undoStack.pop();
            
            try {
                // Apply the old value
                applyChange(change.email, change.fieldName, change.oldValue);
                
                // Move to redo stack
                redoStack.push(change);
                
                // Update last access time
                lastAccessTime.put(email, System.currentTimeMillis());
                
                System.out.println("⏪ Undid: " + change.fieldName + " to " + change.oldValue);
                return true;
                
            } catch (Exception e) {
                System.err.println("❌ Undo failed: " + e.getMessage());
                // FIXED: Push back to undo stack on failure
                undoStack.push(change);
                return false;
            }
        }
    }
    
    /**
     * Redo last undone change
     * FIXED: Added synchronization
     */
    public boolean redo(String email) {
        synchronized (this) {
            // FIXED: Clean up expired history before operation
            cleanupExpiredHistory(email);
            
            Stack<ProfileChange> undoStack = undoStacks.computeIfAbsent(email, k -> new Stack<>());
            Stack<ProfileChange> redoStack = redoStacks.get(email);
            
            if (redoStack == null || redoStack.isEmpty()) {
                System.out.println("⚠️ Nothing to redo for: " + email);
                return false;
            }
            
            ProfileChange change = redoStack.pop();
            
            try {
                // Apply the new value
                applyChange(change.email, change.fieldName, change.newValue);
                
                // Move back to undo stack
                undoStack.push(change);
                
                // Update last access time
                lastAccessTime.put(email, System.currentTimeMillis());
                
                System.out.println("⏩ Redid: " + change.fieldName + " to " + change.newValue);
                return true;
                
            } catch (Exception e) {
                System.err.println("❌ Redo failed: " + e.getMessage());
                // FIXED: Push back to redo stack on failure
                redoStack.push(change);
                return false;
            }
        }
    }
    
    /**
     * Apply a field change to infoplus.json
     * FIXED: Better error handling and null safety
     */
    private void applyChange(String email, String fieldName, String value) throws Exception {
        String path = "data/users/" + email + "/infoplus.json";
        
        // Read current infoplus
        InfoPlus infoPlus;
        if (Files.exists(Paths.get(path))) {
            String json = Files.readString(Paths.get(path));
            infoPlus = gson.fromJson(json, InfoPlus.class);
            if (infoPlus == null) {
                infoPlus = new InfoPlus();
            }
        } else {
            infoPlus = new InfoPlus();
        }
        
        // FIXED: Handle null values properly - update instance fields
        switch (fieldName) {
            case "jobTitle":
                infoPlus.jobTitle = value != null ? value : "";
                break;
            case "phone":
                infoPlus.phone = value != null ? value : "";
                break;
            case "bio":
                infoPlus.bio = value != null ? value : "";
                break;
            case "profilePhoto":
                infoPlus.profilePhoto = value; // Can be null for photo
                break;
            default:
                throw new IllegalArgumentException("Unknown field: " + fieldName);
        }
        
        // Write back
        try (FileWriter fw = new FileWriter(path)) {
            gson.toJson(infoPlus, fw);
        }
    }
    
    /**
     * Check if user can undo
     * FIXED: Added synchronization
     */
    public boolean canUndo(String email) {
        synchronized (this) {
            cleanupExpiredHistory(email);
            Stack<ProfileChange> stack = undoStacks.get(email);
            return stack != null && !stack.isEmpty();
        }
    }
    
    /**
     * Check if user can redo
     * FIXED: Added synchronization
     */
    public boolean canRedo(String email) {
        synchronized (this) {
            cleanupExpiredHistory(email);
            Stack<ProfileChange> stack = redoStacks.get(email);
            return stack != null && !stack.isEmpty();
        }
    }
    
    /**
     * Clear history (called after save)
     * FIXED: Also clear last access time
     */
    public void clearHistory(String email) {
        synchronized (this) {
            undoStacks.remove(email);
            redoStacks.remove(email);
            lastAccessTime.remove(email);
            System.out.println("🗑️ Cleared history for: " + email);
        }
    }
    
    /**
     * Check if user has unsaved changes
     */
    public boolean hasChanges(String email) {
        synchronized (this) {
            cleanupExpiredHistory(email);
            Stack<ProfileChange> stack = undoStacks.get(email);
            return stack != null && !stack.isEmpty();
        }
    }
    
    /**
     * FIXED: Clean up expired history to prevent memory leaks
     * Called before each operation
     */
    private void cleanupExpiredHistory(String email) {
        Long lastAccess = lastAccessTime.get(email);
        if (lastAccess != null) {
            long elapsed = System.currentTimeMillis() - lastAccess;
            if (elapsed > HISTORY_TTL_MS) {
                System.out.println("🧹 Cleaning up expired history for: " + email);
                undoStacks.remove(email);
                redoStacks.remove(email);
                lastAccessTime.remove(email);
            }
        }
    }
    
    /**
     * FIXED: Add method to cleanup all expired histories
     * Can be called by a scheduled task
     */
    public void cleanupAllExpiredHistories() {
        synchronized (this) {
            long currentTime = System.currentTimeMillis();
            List<String> expiredEmails = new ArrayList<>();
            
            lastAccessTime.forEach((email, lastAccess) -> {
                if (currentTime - lastAccess > HISTORY_TTL_MS) {
                    expiredEmails.add(email);
                }
            });
            
            expiredEmails.forEach(email -> {
                undoStacks.remove(email);
                redoStacks.remove(email);
                lastAccessTime.remove(email);
                System.out.println("🧹 Cleaned up expired history for: " + email);
            });
            
            if (!expiredEmails.isEmpty()) {
                System.out.println("✅ Cleaned up " + expiredEmails.size() + " expired user histories");
            }
        }
    }
}