package com.eFarm.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ResetPasswordRequest {
    @NotBlank(message = "Email është i detyrueshëm")
    @Email(message = "Format i pavlefshëm email-i")
    private String email;

    @NotBlank(message = "Kodi i reset-it është i detyrueshëm")
    @Pattern(regexp = "^[0-9]{6}$", message = "Kodi duhet të jetë 6 shifra")
    private String token;

    @NotBlank(message = "Fjalëkalimi i ri është i detyrueshëm")
    @Size(min = 6, max = 100, message = "Fjalëkalimi duhet të jetë së paku 6 karaktere")
    private String newPassword;

    @NotBlank(message = "Konfirmimi i fjalëkalimit është i detyrueshëm")
    private String confirmPassword;

    public ResetPasswordRequest() {}

    public ResetPasswordRequest(String email, String token, String newPassword, String confirmPassword) {
        this.email = email;
        this.token = token;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }

    // Validim shtesë
    public boolean isPasswordMatching() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }
}