package com.example.backend.model;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Setter
@Getter
public class Contact {
   private String id;
   private String name;
   private List<Email> email;
   private List<Phone> phone;
   private String colour;
   private String initials;
}
