package com.example.backend.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Queue;

import com.example.backend.DTOS.attachementDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class mail {
    private int id   ;
    private Queue<String> to;
    private String from ;
    private String subject;
    private String body;
    private String preview ;
    private boolean starred;
    private boolean hasAttachment;
    private LocalDateTime timestamp;
    private int priority;
    private List<attachementDTO> attachments;

    // ADD THIS FIELD - for custom folder support
    private String customFolderId; // null = system folder, "folder_123" = custom folder
}
