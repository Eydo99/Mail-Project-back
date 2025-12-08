package com.example.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Email {

  @JsonProperty("id")
  private String id;

  @NotBlank(message = "Email address cannot be empty")
  @jakarta.validation.constraints.Email(message = "Invalid email format")
  @JsonProperty("address")
  private String address;

  @JsonProperty("isPrimary")
  @SerializedName("isPrimary")  // Keep this for Gson (used in file storage)
  private boolean isPrimary;
}