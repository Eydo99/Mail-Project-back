package com.example.backend.facade;

import com.example.backend.DTOS.SignupRequest;
import com.example.backend.DTOS.LoginRequest;
import com.example.backend.model.UserInfo;
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

    public void signup(SignupRequest req) throws Exception {

        if (!req.email.endsWith("@gmail.com"))
            throw new Exception("Only Gmail accounts allowed");

        if (fileManager.userExists(req.email))
            throw new Exception("Email already exists");

        fileManager.createUserFolder(req.email);

        UserInfo user = new UserInfo();
        user.firstName = req.firstName;
        user.lastName = req.lastName;
        user.email = req.email;
        user.password = encoder.encode(req.password); // hashing 🔐
        user.phoneNumber = req.phoneNumber;
        user.birthDate = req.birthDate;

        try (FileWriter fw = new FileWriter(getInfoPath(req.email))) {
            gson.toJson(user, fw);
        }
    }

    public UserInfo login(LoginRequest req) throws Exception {

        if (!fileManager.userExists(req.email))
            throw new Exception("Account not found");

        String json = Files.readString(Paths.get(getInfoPath(req.email)));
        UserInfo user = gson.fromJson(json, UserInfo.class);

        if (!encoder.matches(req.password, user.password))
            throw new Exception("Incorrect password");

        return user;
    }
}
