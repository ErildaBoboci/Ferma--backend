package com.eFarm.backend.dto;

import java.time.LocalDateTime;

public class LoginResponse {

    private String token;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private UserInfo user;

    // Constructors
    public LoginResponse() {}

    public LoginResponse(String token, Long expiresIn, UserInfo user) {
        this.token = token;
        this.expiresIn = expiresIn;
        this.user = user;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    // Inner class for user information
    public static class UserInfo {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
        private String roleName;
        private boolean isEnabled;
        private boolean isEmailVerified;
        private LocalDateTime lastLogin;

        // Constructors
        public UserInfo() {}

        public UserInfo(Long id, String firstName, String lastName, String email, String phoneNumber,
                        String roleName, boolean isEnabled, boolean isEmailVerified, LocalDateTime lastLogin) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.phoneNumber = phoneNumber;
            this.roleName = roleName;
            this.isEnabled = isEnabled;
            this.isEmailVerified = isEmailVerified;
            this.lastLogin = lastLogin;
        }

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

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

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getRoleName() {
            return roleName;
        }

        public void setRoleName(String roleName) {
            this.roleName = roleName;
        }

        public boolean isEnabled() {
            return isEnabled;
        }

        public void setEnabled(boolean enabled) {
            isEnabled = enabled;
        }

        public boolean isEmailVerified() {
            return isEmailVerified;
        }

        public void setEmailVerified(boolean emailVerified) {
            isEmailVerified = emailVerified;
        }

        public LocalDateTime getLastLogin() {
            return lastLogin;
        }

        public void setLastLogin(LocalDateTime lastLogin) {
            this.lastLogin = lastLogin;
        }

        public String getFullName() {
            return firstName + " " + lastName;
        }
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "token='[PROTECTED]'" +
                ", tokenType='" + tokenType + '\'' +
                ", expiresIn=" + expiresIn +
                ", user=" + user +
                '}';
    }
}