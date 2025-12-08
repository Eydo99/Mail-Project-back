package com.example.backend.DTOS;

import lombok.Getter;
import lombok.Setter;

/**
 * Request DTO for creating/updating folders
 */
@Getter
@Setter
public class FolderRequestDTO {
    private String name;
    private String description;
    private String color;
}
