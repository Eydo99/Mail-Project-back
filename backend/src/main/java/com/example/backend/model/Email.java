package com.example.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Email {

  //jackson annotation for mapping
  @JsonProperty("id")
  private String id;

  //ensure that the address field is not blank and triggered with @valid
  @NotBlank(message = "Email address cannot be empty")
  @jakarta.validation.constraints.Email(message = "Invalid email format")
  @JsonProperty("address")
  private String address;

  @JsonProperty("isPrimary")
  @SerializedName("isPrimary")  // Keep this for Gson
  private boolean isPrimary;
}