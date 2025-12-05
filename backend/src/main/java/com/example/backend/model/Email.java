package com.example.backend.model;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Email {
    private String id;
    @NotBlank(message = "Email address cannot be empty")
    @jakarta.validation.constraints.Email(message = "Invalid email format")
    private String address;
    private boolean isPrimary;

}
