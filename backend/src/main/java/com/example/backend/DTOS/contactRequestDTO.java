package com.example.backend.DTOS;

import com.example.backend.model.Email;
import com.example.backend.model.Phone;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class contactRequestDTO {

    private String name;
    private List<Email> emails;
    private List<Phone> phones;

}