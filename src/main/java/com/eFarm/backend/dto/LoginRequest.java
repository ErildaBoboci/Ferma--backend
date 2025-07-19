package com.eFarm.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    @NotBlank
    @Email
    private String email; // Ndryshuar nga username në email

    @NotBlank
    private String password;

    // Constructors, getters, and setters
    public LoginRequest() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    // Për backward compatibility, shtojmë edhe metodën getUsername()
    public String getUsername() { return email; }
    public void setUsername(String username) { this.email = username; }
}