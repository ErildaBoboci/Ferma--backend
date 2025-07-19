package com.eFarm.backend.service;

import com.eFarm.backend.entity.User;
import com.eFarm.backend.util.IpUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Service
public class UserActivityService {

    /**
     * Regjistro aktivitet të përdoruesit
     */
    public void logActivity(User user, String activityType, String description) {
        try {
            String ipAddress = getCurrentIpAddress();
            String userAgent = getCurrentUserAgent();

            // Për tani vetëm log në console, por mund të ruhet në databazë
            System.out.println("📊 AKTIVITET: " + activityType);
            System.out.println("   Përdoruesi: " + (user != null ? user.getEmail() : "Anonymous"));
            System.out.println("   Përshkrimi: " + description);
            System.out.println("   IP: " + IpUtil.maskIpAddress(ipAddress));
            System.out.println("   Koha: " + LocalDateTime.now());
            System.out.println("   User Agent: " + (userAgent != null ? userAgent.substring(0, Math.min(50, userAgent.length())) + "..." : "N/A"));
            System.out.println();

            // TODO: Ruaj në databazë nëse ke krijuar tabelën user_activity_log
            // saveToDatabase(user, activityType, description, ipAddress, userAgent);

        } catch (Exception e) {
            System.err.println("❌ Gabim në logging aktivitetin: " + e.getMessage());
        }
    }

    /**
     * Regjistro aktivitet pa përdorues (p.sh. tentative hyrje të dështuara)
     */
    public void logAnonymousActivity(String activityType, String description) {
        logActivity(null, activityType, description);
    }

    /**
     * Metodat specifike për aktivitete të ndryshme
     */
    public void logLogin(User user, boolean successful) {
        if (successful) {
            logActivity(user, "LOGIN_SUCCESS", "Përdoruesi u kyç me sukses");
        } else {
            logActivity(user, "LOGIN_FAILED", "Tentativë e dështuar për t'u kyçur");
        }
    }

    public void logRegistration(User user) {
        logActivity(user, "REGISTRATION", "Përdorues i ri u regjistrua");
    }

    public void logEmailVerification(User user, boolean successful) {
        if (successful) {
            logActivity(user, "EMAIL_VERIFIED", "Email-i u verifikua me sukses");
        } else {
            logActivity(user, "EMAIL_VERIFICATION_FAILED", "Tentativë e dështuar për verifikim email-i");
        }
    }

    public void logPasswordChange(User user) {
        logActivity(user, "PASSWORD_CHANGED", "Fjalëkalimi u ndryshua");
    }

    public void logAccountLocked(User user) {
        logActivity(user, "ACCOUNT_LOCKED", "Llogaria u bllokua për shkak të shumë tentativave të dështuara");
    }

    public void logAccountUnlocked(User user) {
        logActivity(user, "ACCOUNT_UNLOCKED", "Llogaria u zhbllokua automatikisht");
    }

    public void logSuspiciousActivity(String email, String description) {
        logAnonymousActivity("SUSPICIOUS_ACTIVITY", "Aktivitet i dyshimtë për " + email + ": " + description);
    }

    /**
     * Helper methods për të marrë informacion nga request-i
     */
    private String getCurrentIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            return IpUtil.getClientIpAddress(request);
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private String getCurrentUserAgent() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            return request.getHeader("User-Agent");
        } catch (Exception e) {
            return "Unknown";
        }
    }

    /**
     * Metoda për të ruajtur në databazë (implemento nëse ke krijuar tabelën)
     */
    /*
    private void saveToDatabase(User user, String activityType, String description, String ipAddress, String userAgent) {
        try {
            UserActivityLog log = new UserActivityLog();
            log.setUserId(user != null ? user.getId() : null);
            log.setActivityType(activityType);
            log.setDescription(description);
            log.setIpAddress(ipAddress);
            log.setUserAgent(userAgent);
            log.setCreatedAt(LocalDateTime.now());

            // userActivityLogRepository.save(log);
        } catch (Exception e) {
            System.err.println("❌ Gabim në ruajtjen e aktivitetit në databazë: " + e.getMessage());
        }
    }
    */
}