package com.eFarm.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    @NotBlank(message = "Emri është i detyrueshëm")
    @Size(min = 2, max = 100, message = "Emri duhet të jetë midis 2-100 karakteresh")
    private String name; // Emri i plotë nga frontend

    @NotBlank(message = "Email është i detyrueshëm")
    @Size(max = 100)
    @Email(message = "Formati i email-it nuk është i vlefshëm")
    private String email;

    @NotBlank(message = "Fjalëkalimi është i detyrueshëm")
    @Size(min = 6, max = 100, message = "Fjalëkalimi duhet të jetë së paku 6 karaktere")
    private String password;

    @NotBlank(message = "Përsëritja e fjalëkalimit është e detyrueshme")
    private String repeatPassword;

    // SHTUAR: Role field për compatibility me AuthService
    private String role = "kujdestar"; // Default role për të gjithë përdoruesit e rinj

    // Constructors
    public RegisterRequest() {
        this.role = "kujdestar"; // Sigurohu që është gjithmonë kujdestar
    }

    public RegisterRequest(String name, String email, String password, String repeatPassword) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.repeatPassword = repeatPassword;
        this.role = "kujdestar"; // Gjithmonë kujdestar për përdorues të rinj
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRepeatPassword() { return repeatPassword; }
    public void setRepeatPassword(String repeatPassword) { this.repeatPassword = repeatPassword; }

    // SHTUAR: Role getter/setter
    public String getRole() {
        // Gjithmonë kthej "kujdestar" pavarësisht nga ajo që dërgohet
        return "kujdestar";
    }

    public void setRole(String role) {
        // Ignore çdo rol tjetër - gjithmonë vendos kujdestar
        this.role = "kujdestar";
    }

    // Helper methods për të ndarë emrin e plotë
    public String getFirstName() {
        if (name == null || name.trim().isEmpty()) {
            return "";
        }
        String[] parts = name.trim().split("\\s+");
        return parts[0];
    }

    public String getLastName() {
        if (name == null || name.trim().isEmpty()) {
            return "";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length > 1) {
            // Bashko të gjitha pjesët e tjera si mbiemër
            StringBuilder lastName = new StringBuilder();
            for (int i = 1; i < parts.length; i++) {
                if (i > 1) lastName.append(" ");
                lastName.append(parts[i]);
            }
            return lastName.toString();
        }
        return ""; // Nëse ka vetëm një fjalë, mbiemri është bosh
    }

    // Gjenero username automatikisht nga email-i
    public String getUsername() {
        if (email == null || email.trim().isEmpty()) {
            return "";
        }
        return email.substring(0, email.indexOf('@')).toLowerCase();
    }

    // Validim shtesë
    public boolean isPasswordMatching() {
        return password != null && password.equals(repeatPassword);
    }

    @Override
    public String toString() {
        return "RegisterRequest{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", username='" + getUsername() + '\'' +
                ", firstName='" + getFirstName() + '\'' +
                ", lastName='" + getLastName() + '\'' +
                '}';
    }
}