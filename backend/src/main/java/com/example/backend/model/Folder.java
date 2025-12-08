package com.example.backend.model;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class Folder {
    private String id;
    private String name;
    private String description;
    private String color;
    private int emailCount;

    public Folder() {
    }

    public Folder(String id, String name, String description, String color) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.color = color;
        this.emailCount = 0;
    }
}