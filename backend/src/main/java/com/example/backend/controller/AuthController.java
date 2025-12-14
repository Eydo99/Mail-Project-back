package com.example.backend.controller;

import com.example.backend.DTOS.SignupRequest;
import com.example.backend.DTOS.LoginRequest;
import com.example.backend.facade.AuthFacade;
import com.example.backend.model.UserInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class AuthController {

    private final AuthFacade facade;

    public AuthController(AuthFacade facade) {
        this.facade = facade;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest req) {
        try {
            facade.signup(req);
            return ResponseEntity.ok("{\"message\":\"Signup successful\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"message\":\"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpServletRequest request) {
        try {
            // Invalidate old session if exists
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                System.out.println("🗑️ LOGIN - Invalidating old session: " + oldSession.getId());
                oldSession.invalidate();
            }

            // Create new session
            HttpSession session = request.getSession(true);
            UserInfo user = facade.login(req);
            session.setAttribute("currentUser", user.email);
            
            // Set session timeout (30 minutes)
            session.setMaxInactiveInterval(1800);
            
            System.out.println("✅ LOGIN - Session created for: " + user.email);
            System.out.println("✅ LOGIN - Session ID: " + session.getId());
            
            return ResponseEntity.ok("{\"message\":\"Login successful\", \"email\":\"" + user.email + "\"}");
        } catch (Exception e) {
            System.out.println("❌ LOGIN - Failed: " + e.getMessage());
            return ResponseEntity.badRequest().body("{\"message\":\"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validate(HttpServletRequest request) {
        System.out.println("🔍 VALIDATE - Endpoint called");
        
        HttpSession session = request.getSession(false);
        
        if (session == null) {
            System.out.println("❌ VALIDATE - No session found");
            return ResponseEntity.status(401).body("{\"message\":\"No session\"}");
        }
        
        System.out.println("✅ VALIDATE - Session ID: " + session.getId());
        
        String email = (String) session.getAttribute("currentUser");
        
        if (email == null) {
            System.out.println("❌ VALIDATE - Session exists but no currentUser attribute");
            return ResponseEntity.status(401).body("{\"message\":\"Not authenticated\"}");
        }
        
        System.out.println("✅ VALIDATE - Valid session for: " + email);
        return ResponseEntity.ok("{\"message\":\"Valid session\", \"email\":\"" + email + "\"}");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        System.out.println("🚪 LOGOUT - Endpoint called");
        
        HttpSession session = request.getSession(false);
        
        if (session != null) {
            String email = (String) session.getAttribute("currentUser");
            System.out.println("✅ LOGOUT - Invalidating session for: " + email);
            System.out.println("✅ LOGOUT - Session ID: " + session.getId());
            session.invalidate();
            System.out.println("✅ LOGOUT - Session invalidated successfully");
        } else {
            System.out.println("⚠️ LOGOUT - No active session found");
        }
        
        return ResponseEntity.ok("{\"message\":\"Logged out successfully\"}");
    }
}