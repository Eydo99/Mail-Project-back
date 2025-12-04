package com.example.backend.DTOS;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class mailDTO {
    private int id   ;
    private List<String> to;
    private String subject;
    private String body;
    private String preview ;
    private boolean isStared;
    private LocalDateTime timestamp;
    private int priority;
    private List<String> attachments;
} 
