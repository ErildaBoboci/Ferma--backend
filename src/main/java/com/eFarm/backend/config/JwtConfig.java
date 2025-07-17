package com.eFarm.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    private String secret;
    private long expiration;
    private String tokenPrefix = "Bearer ";
    private String headerString = "Authorization";
    private long refreshExpiration;

    public JwtConfig() {
        // Default values
        this.secret = "defaultSecretKey";
        this.expiration = 86400000; // 24 hours in milliseconds
        this.refreshExpiration = 604800000; // 7 days in milliseconds
    }

    // Getters and Setters
    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    public String getTokenPrefix() {
        return tokenPrefix;
    }

    public void setTokenPrefix(String tokenPrefix) {
        this.tokenPrefix = tokenPrefix;
    }

    public String getHeaderString() {
        return headerString;
    }

    public void setHeaderString(String headerString) {
        this.headerString = headerString;
    }

    public long getRefreshExpiration() {
        return refreshExpiration;
    }

    public void setRefreshExpiration(long refreshExpiration) {
        this.refreshExpiration = refreshExpiration;
    }

    // Helper methods
    public long getExpirationInSeconds() {
        return expiration / 1000;
    }

    public long getRefreshExpirationInSeconds() {
        return refreshExpiration / 1000;
    }

    public String getFullTokenPrefix() {
        return tokenPrefix.trim() + " ";
    }

    public boolean isValidTokenPrefix(String authHeader) {
        return authHeader != null && authHeader.startsWith(tokenPrefix);
    }

    public String extractTokenFromHeader(String authHeader) {
        if (isValidTokenPrefix(authHeader)) {
            return authHeader.substring(tokenPrefix.length());
        }
        return null;
    }
}