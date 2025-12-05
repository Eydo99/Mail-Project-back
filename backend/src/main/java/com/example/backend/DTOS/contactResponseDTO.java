package com.example.backend.DTOS;

import com.example.backend.model.Email;
import com.example.backend.model.Phone;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class contactResponseDTO {

    private String id;
    private String name;
    private List<Email> emails;
    private List<Phone> phones;
    private String initials;
    private String avatarColor;
}
