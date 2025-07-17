package com.eFarm.backend.util;

import java.util.regex.Pattern;

public class EmailUtil {

    private static final String EMAIL_PATTERN =
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        return pattern.matcher(email).matches();
    }

    public static String extractDomainFromEmail(String email) {
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Email i pavlefshëm");
        }

        int atIndex = email.indexOf('@');
        if (atIndex == -1) {
            throw new IllegalArgumentException("Email i pavlefshëm");
        }

        return email.substring(atIndex + 1);
    }

    public static String extractUsernameFromEmail(String email) {
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Email i pavlefshëm");
        }

        int atIndex = email.indexOf('@');
        if (atIndex == -1) {
            throw new IllegalArgumentException("Email i pavlefshëm");
        }

        return email.substring(0, atIndex);
    }

    public static String maskEmail(String email) {
        if (!isValidEmail(email)) {
            return email;
        }

        String username = extractUsernameFromEmail(email);
        String domain = extractDomainFromEmail(email);

        if (username.length() <= 2) {
            return username.charAt(0) + "*@" + domain;
        }

        StringBuilder masked = new StringBuilder();
        masked.append(username.charAt(0));

        for (int i = 1; i < username.length() - 1; i++) {
            masked.append('*');
        }

        masked.append(username.charAt(username.length() - 1));
        masked.append('@');
        masked.append(domain);

        return masked.toString();
    }

    public static boolean isEmailFromDomain(String email, String domain) {
        if (!isValidEmail(email)) {
            return false;
        }

        return extractDomainFromEmail(email).equalsIgnoreCase(domain);
    }

    public static String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }

        return email.trim().toLowerCase();
    }

    public static boolean isDisposableEmail(String email) {
        if (!isValidEmail(email)) {
            return false;
        }

        String domain = extractDomainFromEmail(email).toLowerCase();

        // List of common disposable email domains
        String[] disposableDomains = {
                "10minutemail.com", "temp-mail.org", "guerrillamail.com",
                "mailinator.com", "yopmail.com", "tempmail.net",
                "throwaway.email", "fakeinbox.com", "sharklasers.com",
                "getnada.com", "tempmail.io", "emailondeck.com"
        };

        for (String disposableDomain : disposableDomains) {
            if (domain.equals(disposableDomain)) {
                return true;
            }
        }

        return false;
    }

    public static String generateEmailSubject(String type, String appName) {
        return switch (type.toLowerCase()) {
            case "verification" -> "Verifikoni Email-in tuaj - " + appName;
            case "welcome" -> "Mirë se erdhe në " + appName + "!";
            case "password-reset" -> "Rivendosni Password-in tuaj - " + appName;
            case "password-changed" -> "Password-i juaj është ndryshuar - " + appName;
            case "account-locked" -> "Llogaria juaj është bllokuar - " + appName;
            case "account-unlocked" -> "Llogaria juaj është zhbllokuar - " + appName;
            case "login-alert" -> "Hyrje e re në llogarinë tuaj - " + appName;
            default -> "Njoftim nga " + appName;
        };
    }

    public static boolean isValidEmailForRegistration(String email) {
        if (!isValidEmail(email)) {
            return false;
        }

        if (isDisposableEmail(email)) {
            return false;
        }

        // Additional business rules can be added here
        return true;
    }

    public static String getEmailProviderName(String email) {
        if (!isValidEmail(email)) {
            return "Unknown";
        }

        String domain = extractDomainFromEmail(email).toLowerCase();

        return switch (domain) {
            case "gmail.com" -> "Gmail";
            case "yahoo.com", "yahoo.co.uk", "yahoo.fr" -> "Yahoo";
            case "outlook.com", "hotmail.com", "live.com" -> "Outlook";
            case "icloud.com" -> "iCloud";
            case "protonmail.com" -> "ProtonMail";
            case "aol.com" -> "AOL";
            default -> "Other";
        };
    }
}