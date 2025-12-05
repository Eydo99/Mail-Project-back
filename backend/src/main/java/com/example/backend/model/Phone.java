package com.example.backend.model;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Phone {
    private String id;
    private String number;
    private boolean isPrimary;
}
