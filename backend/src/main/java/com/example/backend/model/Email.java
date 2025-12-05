package com.example.backend.model;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Email {
    private String id;
    private String address;
    private boolean isPrimary;

}
