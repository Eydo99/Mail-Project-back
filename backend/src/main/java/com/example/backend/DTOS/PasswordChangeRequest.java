package com.example.backend.DTOS;

public class PasswordChangeRequest {
    public String currentPassword;
    public String newPassword;
    public String confirmPassword;
    
    // Optional: Add validation methods
    public boolean isValid() {
        if (currentPassword == null || currentPassword.trim().isEmpty()) {
            return false;
        }
        if (newPassword == null || newPassword.length() < 8) {
            return false;
        }
        if (!newPassword.equals(confirmPassword)) {
            return false;
        }
        return true;
    }
    
    public String getValidationError() {
        if (currentPassword == null || currentPassword.trim().isEmpty()) {
            return "Current password is required";
        }
        if (newPassword == null || newPassword.length() < 8) {
            return "New password must be at least 8 characters";
        }
        if (!newPassword.equals(confirmPassword)) {
            return "Passwords do not match";
        }
        return null;
    }
}