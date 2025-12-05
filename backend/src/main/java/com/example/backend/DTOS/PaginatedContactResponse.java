package com.example.backend.DTOS;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaginatedContactResponse {
    private List<contactResponseDTO> contacts;
    private int totalItems;
}