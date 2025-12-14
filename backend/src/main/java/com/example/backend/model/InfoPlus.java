package com.example.backend.model;

public class InfoPlus {
    public String jobTitle;
    public String phone;
    public String bio;
    public String profilePhoto;  
    
    public InfoPlus() {
        this.jobTitle = "";
        this.phone = "";
        this.bio = "";
        this.profilePhoto = null;
    }
    
    public InfoPlus(String jobTitle, String phone, String bio, String profilePhoto) {
        this.jobTitle = jobTitle;
        this.phone = phone;
        this.bio = bio;
        this.profilePhoto = profilePhoto;
    }
}