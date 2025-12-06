package com.example.backend.DTOS;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class mailContentDTO {
    private String body ;
    private String subject ;
    private List<String> recipients ;
    private List<attachementDTO> attachements;
    private int piriority ;
}
