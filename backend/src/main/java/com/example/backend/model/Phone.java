package com.example.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Phone {

    @JsonProperty("id")
    private String id;

    @JsonProperty("number")
    private String number;

    @JsonProperty("isPrimary")
    @SerializedName("isPrimary")  // Keep this for Gson
    private boolean isPrimary;
}