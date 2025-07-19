package com.eFarm.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class EmailVerificationRequest {
    @NotBlank(message = "Email është i detyrueshëm")
    @Email(message = "Format i pavlefshëm email-i")
    private String email;

    @NotBlank(message = "Kodi i verifikimit është i detyrueshëm")
    @Pattern(regexp = "^[0-9]{4}$", message = "Kodi duhet të jetë 4 shifra")
    private String code;

    public EmailVerificationRequest() {}

    public EmailVerificationRequest(String email, String code) {
        this.email = email;
        this.code = code;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}