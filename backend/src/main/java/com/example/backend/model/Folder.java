package com.example.backend.model;

import lombok.Getter;
import lombok.Setter;

//lombok annotations for generating getters and setters
@Getter
@Setter
public class Folder {
    private String id;
    private String name;
    private String description;
    private String color;
    private int emailCount;

    public Folder() {}
}