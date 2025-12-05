package com.example.backend.DTOS;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class attachementDTO {
    private String filename;
    private String filePath;  // or fileId
    private String mimeType;  // e.g., "application/pdf", "image/jpeg"
    private long fileSize;    // in bytes
}
