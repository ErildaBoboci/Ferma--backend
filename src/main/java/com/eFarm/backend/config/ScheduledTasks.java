package com.eFarm.backend.config;

import com.eFarm.backend.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class ScheduledTasks {

    @Autowired
    private EmailService emailService;

    /**
     * Pastro token-et e skaduar çdo 30 minuta
     */
    @Scheduled(fixedRate = 30 * 60 * 1000) // 30 minuta
    public void cleanupExpiredTokens() {
        try {
            emailService.cleanupExpiredTokens();
            System.out.println("Cleanup i token-eve të skaduar u krye me sukses");
        } catch (Exception e) {
            System.err.println("Gabim gjatë cleanup të token-eve: " + e.getMessage());
        }
    }

    /**
     * Statistika të përgjithshme çdo ditë në mëngjes (8:00 AM)
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void dailyStatistics() {
        try {
            System.out.println("=== Statistika ditore ===");
            System.out.println("Sistemi po funksionon normalisht");
            System.out.println("========================");
        } catch (Exception e) {
            System.err.println("Gabim gjatë gjenerimit të statistikave: " + e.getMessage());
        }
    }
}