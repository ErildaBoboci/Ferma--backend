package com.eFarm.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "Emri është i detyrueshëm")
    @Size(min = 2, max = 50, message = "Emri duhet të jetë midis 2 dhe 50 karakteresh")
    private String firstName;

    @NotBlank(message = "Mbiemri është i detyrueshëm")
    @Size(min = 2, max = 50, message = "Mbiemri duhet të jetë midis 2 dhe 50 karakteresh")
    private String lastName;

    @NotBlank(message = "Email është i detyrueshëm")
    @Email(message = "Email nuk është i vlefshëm")
    private String email;

    @NotBlank(message = "Password është i detyrueshëm")
    @Size(min = 6, message = "Password duhet të jetë të paktën 6 karaktere")
    private String password;

    @NotBlank(message = "Konfirmimi i password është i detyrueshëm")
    private String confirmPassword;

    @NotBlank(message = "Roli është i detyrueshëm")
    private String roleName;

    private String phoneNumber;

    // Constructors
    public RegisterRequest() {}

    public RegisterRequest(String firstName, String lastName, String email, String password, String confirmPassword, String roleName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.roleName = roleName;
    }

    // Getters and Setters
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    // Helper method to check if passwords match
    public boolean isPasswordsMatching() {
        return password != null && password.equals(confirmPassword);
    }

    @Override
    public String toString() {
        return "RegisterRequest{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", password='[PROTECTED]'" +
                ", confirmPassword='[PROTECTED]'" +
                ", roleName='" + roleName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}