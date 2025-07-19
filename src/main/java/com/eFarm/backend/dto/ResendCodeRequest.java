package com.eFarm.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ResendCodeRequest {
    @NotBlank(message = "Email është i detyrueshëm")
    @Email(message = "Format i pavlefshëm email-i")
    private String email;

    public ResendCodeRequest() {}

    public ResendCodeRequest(String email) {
        this.email = email;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}