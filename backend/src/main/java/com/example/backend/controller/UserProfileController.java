package com.example.backend.controller;

import com.example.backend.DTOS.ProfileUpdateRequest;
import com.example.backend.DTOS.PasswordChangeRequest;
import com.example.backend.facade.UserProfileFacade;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for user profile management
 * Handles settings panel operations including profile updates and password changes
 */
@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class UserProfileController {
    
    @Autowired
    private UserProfileFacade profileFacade;
    
    /**
     * Get user profile
     * GET /api/user/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        System.out.println("📋 GET /api/user/profile - Getting user profile");
        
        // Validate session
        HttpSession session = request.getSession(false);
        if (session == null) {
            System.out.println("❌ No session found");
            return ResponseEntity.status(401)
                .body(createErrorResponse("Not authenticated"));
        }
        
        String email = (String) session.getAttribute("currentUser");
        if (email == null) {
            System.out.println("❌ No currentUser in session");
            return ResponseEntity.status(401)
                .body(createErrorResponse("Not authenticated"));
        }
        
        try {
            Map<String, Object> profile = profileFacade.getProfile(email);
            System.out.println("✅ Profile retrieved for: " + email);
            return ResponseEntity.ok(profile);
            
        } catch (Exception e) {
            System.err.println("❌ Error getting profile: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                .body(createErrorResponse("Error loading profile: " + e.getMessage()));
        }
    }
    
    /**
     * Update user profile
     * PUT /api/user/profile
     */
    @PutMapping("/profile")
public ResponseEntity<?> updateProfile(
        @RequestBody ProfileUpdateRequest request,
        HttpServletRequest httpRequest) {
    
    System.out.println("💾 PUT /api/user/profile - Updating user profile");
    
    System.out.println("═══════════════════════════════════════");
    System.out.println("📥 REQUEST RECEIVED:");
    System.out.println("   fullName: " + request.fullName);
    System.out.println("   jobTitle: " + request.jobTitle);
    System.out.println("   phone: " + request.phone);
    System.out.println("   bio: " + request.bio);
    
    if (request.profilePhoto != null) {
        int length = request.profilePhoto.length();
        String preview = request.profilePhoto.substring(0, Math.min(50, length));
        System.out.println("   profilePhoto: " + preview + "... (total length: " + length + " chars)");
        System.out.println("   photo starts with 'data:image': " + request.profilePhoto.startsWith("data:image"));
    } else {
        System.out.println("   profilePhoto: NULL");
    }
    System.out.println("═══════════════════════════════════════");
    
    HttpSession session = httpRequest.getSession(false);
    if (session == null) {
        System.out.println("❌ No session found");
        return ResponseEntity.status(401)
            .body(createErrorResponse("Not authenticated"));
    }
    
    String email = (String) session.getAttribute("currentUser");
    if (email == null) {
        System.out.println("❌ No currentUser in session");
        return ResponseEntity.status(401)
            .body(createErrorResponse("Not authenticated"));
    }
    
    if (request == null) {
        System.out.println(" Invalid request body");
        
        return ResponseEntity.status(400)
            .body(createErrorResponse("Invalid request body"));
    }
    
    try {
        profileFacade.updateProfile(email, request);
        profileFacade.clearHistory(email);
        
        System.out.println("✅ Profile updated successfully for: " + email);
        return ResponseEntity.ok(createSuccessResponse("Profile updated successfully"));
        
    } catch (Exception e) {
        System.err.println("❌ Error updating profile: " + e.getMessage());
        e.printStackTrace();
        return ResponseEntity.status(500)
            .body(createErrorResponse("Error updating profile: " + e.getMessage()));
    }
}
    /**
     * Change user password
     * PUT /api/user/password
     */
    @PutMapping("/password")
    public ResponseEntity<?> changePassword(
            @RequestBody PasswordChangeRequest request,
            HttpServletRequest httpRequest) {
        
        System.out.println("🔐 PUT /api/user/password - Changing password");
        
        // Validate session
        HttpSession session = httpRequest.getSession(false);
        if (session == null) {
            System.out.println("❌ No session found");
            return ResponseEntity.status(401)
                .body(createErrorResponse("Not authenticated"));
        }
        
        String email = (String) session.getAttribute("currentUser");
        if (email == null) {
            System.out.println("❌ No currentUser in session");
            return ResponseEntity.status(401)
                .body(createErrorResponse("Not authenticated"));
        }
        
        // Validate request
        if (request == null || !request.isValid()) {
            String error = request != null ? request.getValidationError() : "Invalid request";
            System.out.println("❌ Invalid password change request: " + error);
            return ResponseEntity.status(400)
                .body(createErrorResponse(error));
        }
        
        try {
            profileFacade.changePassword(email, request);
            System.out.println("✅ Password changed successfully for: " + email);
            return ResponseEntity.ok(createSuccessResponse("Password changed successfully"));
            
        } catch (Exception e) {
            System.err.println("❌ Error changing password: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(400)
                .body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Undo last profile change
     * POST /api/user/profile/undo
     */
    @PostMapping("/profile/undo")
    public ResponseEntity<?> undo(HttpServletRequest request) {
        System.out.println("⏪ POST /api/user/profile/undo");
        
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResponseEntity.status(401)
                .body(createErrorResponse("Not authenticated"));
        }
        
        String email = (String) session.getAttribute("currentUser");
        if (email == null) {
            return ResponseEntity.status(401)
                .body(createErrorResponse("Not authenticated"));
        }
        
        try {
            boolean success = profileFacade.undo(email);
            
            if (success) {
                System.out.println("✅ Undo successful for: " + email);
                return ResponseEntity.ok(createSuccessResponse("Undo successful"));
            } else {
                System.out.println("⚠️ Nothing to undo for: " + email);
                return ResponseEntity.ok(createSuccessResponse("Nothing to undo"));
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error during undo: " + e.getMessage());
            return ResponseEntity.status(500)
                .body(createErrorResponse("Error during undo: " + e.getMessage()));
        }
    }
    
    /**
     * Redo last undone profile change
     * POST /api/user/profile/redo
     */
    @PostMapping("/profile/redo")
    public ResponseEntity<?> redo(HttpServletRequest request) {
        System.out.println("⏩ POST /api/user/profile/redo");
        
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResponseEntity.status(401)
                .body(createErrorResponse("Not authenticated"));
        }
        
        String email = (String) session.getAttribute("currentUser");
        if (email == null) {
            return ResponseEntity.status(401)
                .body(createErrorResponse("Not authenticated"));
        }
        
        try {
            boolean success = profileFacade.redo(email);
            
            if (success) {
                System.out.println("✅ Redo successful for: " + email);
                return ResponseEntity.ok(createSuccessResponse("Redo successful"));
            } else {
                System.out.println("⚠️ Nothing to redo for: " + email);
                return ResponseEntity.ok(createSuccessResponse("Nothing to redo"));
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error during redo: " + e.getMessage());
            return ResponseEntity.status(500)
                .body(createErrorResponse("Error during redo: " + e.getMessage()));
        }
    }
    
    /**
     * Check if user can undo
     * GET /api/user/profile/can-undo
     */
    @GetMapping("/profile/can-undo")
    public ResponseEntity<?> canUndo(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResponseEntity.status(401)
                .body(createErrorResponse("Not authenticated"));
        }
        
        String email = (String) session.getAttribute("currentUser");
        if (email == null) {
            return ResponseEntity.status(401)
                .body(createErrorResponse("Not authenticated"));
        }
        
        boolean canUndo = profileFacade.canUndo(email);
        Map<String, Boolean> response = new HashMap<>();
        response.put("canUndo", canUndo);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Check if user can redo
     * GET /api/user/profile/can-redo
     */
    @GetMapping("/profile/can-redo")
    public ResponseEntity<?> canRedo(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResponseEntity.status(401)
                .body(createErrorResponse("Not authenticated"));
        }
        
        String email = (String) session.getAttribute("currentUser");
        if (email == null) {
            return ResponseEntity.status(401)
                .body(createErrorResponse("Not authenticated"));
        }
        
        boolean canRedo = profileFacade.canRedo(email);
        Map<String, Boolean> response = new HashMap<>();
        response.put("canRedo", canRedo);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Check if user has unsaved changes
     * GET /api/user/profile/has-changes
     */
    @GetMapping("/profile/has-changes")
    public ResponseEntity<?> hasChanges(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResponseEntity.status(401)
                .body(createErrorResponse("Not authenticated"));
        }
        
        String email = (String) session.getAttribute("currentUser");
        if (email == null) {
            return ResponseEntity.status(401)
                .body(createErrorResponse("Not authenticated"));
        }
        
        boolean hasChanges = profileFacade.hasChanges(email);
        Map<String, Boolean> response = new HashMap<>();
        response.put("hasChanges", hasChanges);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Helper method to create error response
     */
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return response;
    }
    
    /**
     * Helper method to create success response
     */
    private Map<String, String> createSuccessResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return response;
    }
}