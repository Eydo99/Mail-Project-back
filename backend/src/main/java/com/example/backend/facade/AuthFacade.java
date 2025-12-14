package com.example.backend.facade;

import com.example.backend.DTOS.SignupRequest;
import com.example.backend.DTOS.LoginRequest;
import com.example.backend.model.UserInfo;
import com.example.backend.model.InfoPlus;
import com.example.backend.Util.JsonFileManager;
import com.google.gson.Gson;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class AuthFacade {
    
    private final JsonFileManager fileManager; // Proxy to filesystem
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final Gson gson = new Gson();
    
    public AuthFacade(JsonFileManager fileManager) {
        this.fileManager = fileManager;
    }
    
    private String getInfoPath(String email) {
        return "data/users/" + email + "/info.json";
    }
    
    private String getInfoPlusPath(String email) {
        return "data/users/" + email + "/infoplus.json";
    }
    
    public void signup(SignupRequest req) throws Exception {
        // Validate Gmail account
        if (!req.email.endsWith("@gmail.com"))
            throw new Exception("Only Gmail accounts allowed");
        
        // Check if user already exists
        if (fileManager.userExists(req.email))
            throw new Exception("Email already exists");
        
        // Create user folder with mail files (inbox, sent, draft, trash, contacts, folders)
        fileManager.createUserFolder(req.email);
        
        // Create info.json (basic account information)
        UserInfo user = new UserInfo();
        user.firstName = req.firstName;
        user.lastName = req.lastName;
        user.email = req.email;
        user.password = encoder.encode(req.password); // Hash password
        user.phoneNumber = req.phoneNumber;
        user.birthDate = req.birthDate;
        
        try (FileWriter fw = new FileWriter(getInfoPath(req.email))) {
            gson.toJson(user, fw);
            System.out.println("✅ Created info.json for: " + req.email);
        }
        
        // Create infoplus.json (extended profile - initially empty)
        InfoPlus infoPlus = new InfoPlus();
        // InfoPlus constructor sets default values: jobTitle="", phone="", bio="", profilePhoto=null
        
        try (FileWriter fw = new FileWriter(getInfoPlusPath(req.email))) {
            gson.toJson(infoPlus, fw);
            System.out.println("✅ Created infoplus.json for: " + req.email);
        }
    }
    
    public UserInfo login(LoginRequest req) throws Exception {
        // Check if user exists
        if (!fileManager.userExists(req.email))
            throw new Exception("Account not found");
        
        // Read user info
        String json = Files.readString(Paths.get(getInfoPath(req.email)));
        UserInfo user = gson.fromJson(json, UserInfo.class);
        
        // Verify password
        if (!encoder.matches(req.password, user.password))
            throw new Exception("Incorrect password");
        
        return user;
    }
}
