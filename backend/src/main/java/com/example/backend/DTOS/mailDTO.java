package com.example.backend.DTOS;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class mailDTO {
    private long id;
    private List<String> to;
    private String subject;
    private String body;
    private String time;
    private List<String> attachments;
} 
