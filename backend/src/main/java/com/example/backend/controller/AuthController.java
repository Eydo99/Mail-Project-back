package com.example.backend.controller;

import com.example.backend.DTOS.SignupRequest;
import com.example.backend.DTOS.LoginRequest;
import com.example.backend.facade.AuthFacade;
import com.example.backend.model.UserInfo;
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
    public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpSession session) {
        try {
            UserInfo user = facade.login(req);
            session.setAttribute("userEmail", user.email);
            return ResponseEntity.ok("{\"message\":\"Login successful\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"message\":\"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("{\"message\":\"Logged out\"}");
    }
}
