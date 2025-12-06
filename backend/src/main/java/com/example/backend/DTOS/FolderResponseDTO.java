package com.example.backend.DTOS;

import lombok.Getter;
import lombok.Setter;

/**
 * Response DTO for folder data
 */
@Getter
@Setter
public class FolderResponseDTO {
    private String id;
    private String name;
    private String description;
    private String color;
    private int emailCount;

    public FolderResponseDTO() {
    }

    public FolderResponseDTO(String id, String name, String description, String color, int emailCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.color = color;
        this.emailCount = emailCount;
    }
}