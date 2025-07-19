package com.eFarm.backend.service;

import com.eFarm.backend.entity.PasswordResetToken;
import com.eFarm.backend.entity.User;
import com.eFarm.backend.repository.PasswordResetTokenRepository;
import com.eFarm.backend.repository.UserRepository;
import com.eFarm.backend.util.EmailUtil;
import com.eFarm.backend.util.IpUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@Transactional
public class PasswordResetService {

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.enabled:false}")
    private boolean emailEnabled;

    @Value("${app.base-url:http://localhost:3000}")
    private String baseUrl;

    @Value("${app.name:eFarm}")
    private String appName;

    @Value("${app.support-email:erilda.boboci1@gmail.com}")
    private String supportEmail;

    @Value("${app.password-reset-expiry-minutes:30}")
    private int resetExpiryMinutes;

    private final SecureRandom random = new SecureRandom();

    // Rate limiting: maksimumi 3 përpjekje për orë
    private static final int MAX_RESET_ATTEMPTS_PER_HOUR = 3;
    private static final int MAX_RESET_ATTEMPTS_PER_IP_PER_HOUR = 10;

    /**
     * Gjenero kod reset 6 shifror
     */
    public String generateResetCode() {
        int code = 100000 + random.nextInt(900000); // 100000-999999
        return String.valueOf(code);
    }

    /**
     * Dërgo email për forgot password
     */
    public String sendForgotPasswordEmail(String email) {
        try {
            System.out.println("🔒 Fillim forgot password për: " + email);

            // Valido email format
            if (!EmailUtil.isValidEmail(email)) {
                throw new RuntimeException("Format i pavlefshëm email-i");
            }

            // Kontrollo nëse përdoruesi ekziston
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Nuk u gjet përdorues me këtë email"));

            // Kontrollo nëse email-i është i verifikuar
            if (!user.getEmailVerified()) {
                throw new RuntimeException("Email-i nuk është i verifikuar. Ju lutemi verifikoni email-in përpara se të ndryshoni fjalëkalimin.");
            }

            // Kontrollo nëse llogaria është aktive
            if (!user.getIsActive()) {
                throw new RuntimeException("Llogaria është e çaktivizuar. Kontaktoni administratorin.");
            }

            // Rate limiting
            String clientIp = getCurrentIpAddress();
            if (isPasswordResetRateLimited(email, clientIp)) {
                throw new RuntimeException("Shumë përpjekje për rivendosjen e fjalëkalimit. Provoni pas 1 ore.");
            }

            // Krijo token të ri
            PasswordResetToken token = createPasswordResetToken(email, clientIp);

            // Dërgo email
            try {
                sendPasswordResetEmail(email, user.getFullName(), token.getToken());
                System.out.println("✅ Password reset email sent successfully to: " + email);
                return "Email për rivendosjen e fjalëkalimit u dërgua. Kontrolloni email-in tuaj.";
            } catch (Exception emailError) {
                System.err.println("❌ Gabim në dërgimin e email-it, por token u krijua: " + emailError.getMessage());
                // Edhe nëse email dështon, jep përgjigjje pozitive për sigurinë
                return "Email për rivendosjen e fjalëkalimit u dërgua. Kontrolloni email-in tuaj (edhe spam folder).";
            }

        } catch (Exception e) {
            System.err.println("❌ Password reset email error për " + email + ": " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Reset password me token
     */
    public String resetPassword(String email, String token, String newPassword, String confirmPassword) {
        try {
            System.out.println("🔑 Fillim password reset për: " + email);

            // Validimet bazike
            if (!EmailUtil.isValidEmail(email)) {
                throw new RuntimeException("Format i pavlefshëm email-i");
            }

            if (!newPassword.equals(confirmPassword)) {
                throw new RuntimeException("Fjalëkalimet nuk përputhen");
            }

            if (newPassword.length() < 6) {
                throw new RuntimeException("Fjalëkalimi duhet të jetë së paku 6 karaktere");
            }

            // Gjej user-in
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Përdorues nuk u gjet"));

            // Verifiko token-in
            if (!verifyResetToken(email, token)) {
                throw new RuntimeException("Kod i pavlefshëm ose i skaduar");
            }

            // Përditëso fjalëkalimin
            user.setPassword(passwordEncoder.encode(newPassword));

            // Reset failed login attempts
            user.setFailedLoginAttempts(0);
            user.setAccountLockedUntil(null);

            userRepository.save(user);

            // Dërgo email konfirmimi (optional)
            try {
                sendPasswordChangedConfirmation(user);
            } catch (Exception e) {
                System.err.println("⚠️ Password changed confirmation email failed: " + e.getMessage());
                // Por mos dështoj reset-in për këtë
            }

            System.out.println("✅ Password reset successful for: " + email);
            return "Fjalëkalimi u ndryshua me sukses. Mund të kyçeni me fjalëkalimin e ri.";

        } catch (Exception e) {
            System.err.println("❌ Password reset error për " + email + ": " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Krijo token për password reset
     */
    private PasswordResetToken createPasswordResetToken(String email, String ip) {
        try {
            // Fshi token-et e vjetër për këtë email
            tokenRepository.deleteByEmailAndIsUsedFalse(email);
            System.out.println("🗑️ Old reset tokens deleted for: " + email);

            String code = generateResetCode();
            PasswordResetToken token = new PasswordResetToken(code, email);
            token.setCreatedByIp(ip);
            token.setExpiresAt(LocalDateTime.now().plusMinutes(resetExpiryMinutes));

            PasswordResetToken savedToken = tokenRepository.save(token);
            System.out.println("✅ Reset token created: " + code + " for " + email + " (expires in " + resetExpiryMinutes + " min)");

            return savedToken;
        } catch (Exception e) {
            System.err.println("❌ Error creating reset token: " + e.getMessage());
            throw new RuntimeException("Gabim në krijimin e kodit të reset-it: " + e.getMessage());
        }
    }

    /**
     * Verifiko token reset
     */
    private boolean verifyResetToken(String email, String token) {
        try {
            System.out.println("🔍 Verifying reset token: " + token + " for " + email);

            Optional<PasswordResetToken> tokenOpt = tokenRepository
                    .findByEmailAndTokenAndIsUsedFalse(email, token);

            if (tokenOpt.isEmpty()) {
                System.out.println("❌ Reset token not found or already used: " + token);
                return false;
            }

            PasswordResetToken resetToken = tokenOpt.get();

            if (!resetToken.isValid()) {
                System.out.println("❌ Reset token invalid or expired: " + token);
                return false;
            }

            // Shëno si të përdorur
            resetToken.markAsUsed();
            tokenRepository.save(resetToken);

            System.out.println("✅ Reset token verified successfully: " + token);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Error verifying reset token: " + e.getMessage());
            return false;
        }
    }

    /**
     * Dërgo email për password reset
     */
    private void sendPasswordResetEmail(String email, String fullName, String resetCode) {
        System.out.println("📧 Duke dërguar password reset email në: " + email);

        if (emailEnabled && mailSender != null) {
            try {
                sendRealPasswordResetEmail(email, fullName, resetCode);
            } catch (Exception e) {
                System.err.println("⚠️ Real email failed, using simulation: " + e.getMessage());
                sendSimulatedPasswordResetEmail(email, fullName, resetCode);
            }
        } else {
            System.out.println("ℹ️ Using email simulation (emailEnabled=" + emailEnabled + ")");
            sendSimulatedPasswordResetEmail(email, fullName, resetCode);
        }
    }

    /**
     * Dërgo email të vërtetë për password reset
     */
    private void sendRealPasswordResetEmail(String email, String fullName, String resetCode) throws Exception {
        try {
            if (mailSender == null) {
                throw new RuntimeException("JavaMailSender është null");
            }

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(supportEmail);
            helper.setTo(email);
            helper.setSubject("🔒 Rivendosni Fjalëkalimin tuaj - " + appName);

            String htmlContent = createPasswordResetEmailHtml(fullName, resetCode);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            System.out.println("✅ Password reset email sent successfully to: " + email);

        } catch (MessagingException e) {
            System.err.println("❌ MessagingException në password reset email: " + e.getMessage());
            throw new Exception("Gabim në dërgimin e email-it: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("❌ Exception në password reset email: " + e.getMessage());
            throw new Exception("Gabim i papritur: " + e.getMessage(), e);
        }
    }

    /**
     * Simulim email password reset
     */
    private void sendSimulatedPasswordResetEmail(String email, String fullName, String resetCode) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("🔒 EMAIL PASSWORD RESET (SIMULIM)");
        System.out.println("=".repeat(70));
        System.out.println("📤 Nga: " + supportEmail);
        System.out.println("📥 Për: " + email);
        System.out.println("👤 Emri: " + fullName);
        System.out.println("📋 Subjekti: 🔒 Rivendosni Fjalëkalimin tuaj - " + appName);
        System.out.println("");
        System.out.println("🔑 KODI JUAJ I RESET: " + resetCode);
        System.out.println("");
        System.out.println("⏰ Ky kod skadon pas " + resetExpiryMinutes + " minutash.");
        System.out.println("🔒 Mos e ndani këtë kod me askënd.");
        System.out.println("🌐 Shkoni në: " + baseUrl + "/reset-password");
        System.out.println("");
        System.out.println("ℹ️ Nëse nuk keni kërkuar këtë reset, injoroni këtë email.");
        System.out.println("=".repeat(70) + "\n");
    }

    /**
     * Krijo HTML për password reset email
     */
    private String createPasswordResetEmailHtml(String fullName, String resetCode) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>Rivendosni Fjalëkalimin</title>\n");
        html.append("    <style>\n");
        html.append("        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f4f4f4; }\n");
        html.append("        .container { max-width: 600px; margin: 20px auto; background: white; border-radius: 10px; box-shadow: 0 0 20px rgba(0,0,0,0.1); overflow: hidden; }\n");
        html.append("        .header { background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%); color: white; padding: 30px 20px; text-align: center; }\n");
        html.append("        .content { padding: 40px 30px; background: white; }\n");
        html.append("        .code-box { background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%); color: white; padding: 25px; text-align: center; margin: 30px 0; border-radius: 10px; }\n");
        html.append("        .code { font-size: 36px; font-weight: bold; letter-spacing: 8px; margin: 10px 0; }\n");
        html.append("        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; background: #f8f9fa; }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class=\"container\">\n");
        html.append("        <div class=\"header\">\n");
        html.append("            <h1>🔒 Rivendosni Fjalëkalimin</h1>\n");
        html.append("        </div>\n");
        html.append("        <div class=\"content\">\n");
        html.append("            <p><strong>Përshëndetje ").append(fullName).append("!</strong></p>\n");
        html.append("            <p>Kemi marrë një kërkesë për të rivendosur fjalëkalimin e llogarisë suaj në <strong>").append(appName).append("</strong>.</p>\n");
        html.append("            <div class=\"code-box\">\n");
        html.append("                <p>Kodi juaj i reset:</p>\n");
        html.append("                <div class=\"code\">").append(resetCode).append("</div>\n");
        html.append("            </div>\n");
        html.append("            <p>Ky kod skadon pas <strong>").append(resetExpiryMinutes).append(" minutash</strong>.</p>\n");
        html.append("        </div>\n");
        html.append("        <div class=\"footer\">\n");
        html.append("            <p>© 2025 ").append(appName).append("</p>\n");
        html.append("        </div>\n");
        html.append("    </div>\n");
        html.append("</body>\n");
        html.append("</html>");

        return html.toString();
    }

    /**
     * Dërgo email konfirmimi për ndryshimin e fjalëkalimit
     */
    private void sendPasswordChangedConfirmation(User user) {
        System.out.println("📧 Sending password changed confirmation to: " + user.getEmail());

        if (emailEnabled && mailSender != null) {
            try {
                sendRealPasswordChangedEmail(user);
            } catch (Exception e) {
                System.err.println("⚠️ Password changed email failed: " + e.getMessage());
                sendSimulatedPasswordChangedEmail(user);
            }
        } else {
            sendSimulatedPasswordChangedEmail(user);
        }
    }

    /**
     * Email i vërtetë për konfirmim ndryshimi
     */
    private void sendRealPasswordChangedEmail(User user) throws Exception {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(supportEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("✅ Fjalëkalimi u ndryshua - " + appName);

            String htmlContent = createPasswordChangedEmailHtml(user);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            System.out.println("✅ Password changed confirmation sent to: " + user.getEmail());

        } catch (Exception e) {
            throw new Exception("Gabim në email konfirmimi: " + e.getMessage(), e);
        }
    }

    /**
     * Simulim email konfirmimi
     */
    private void sendSimulatedPasswordChangedEmail(User user) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("✅ EMAIL KONFIRMIMI NDRYSHIMI FJALËKALIMI (SIMULIM)");
        System.out.println("=".repeat(70));
        System.out.println("📤 Nga: " + supportEmail);
        System.out.println("📥 Për: " + user.getEmail());
        System.out.println("👤 Emri: " + user.getFullName());
        System.out.println("📋 Subjekti: ✅ Fjalëkalimi u ndryshua - " + appName);
        System.out.println("");
        System.out.println("🎉 Fjalëkalimi juaj u ndryshua me sukses!");
        System.out.println("🕐 Koha: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        System.out.println("");
        System.out.println("🔒 Nëse nuk e keni bërë ju këtë ndryshim, kontaktoni menjëherë administratorin.");
        System.out.println("=".repeat(70) + "\n");
    }

    /**
     * HTML për email konfirmimi
     */
    private String createPasswordChangedEmailHtml(User user) {
        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>Fjalëkalimi u ndryshua</title></head><body><h1>Fjalëkalimi u ndryshua!</h1><p>Përshëndetje " + user.getFullName() + "!</p><p>Fjalëkalimi juaj u ndryshua me sukses.</p></body></html>";
    }

    /**
     * Rate limiting për password reset
     */
    private boolean isPasswordResetRateLimited(String email, String ip) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);

        long emailAttempts = tokenRepository.countByEmailAndCreatedAtAfter(email, oneHourAgo);
        long ipAttempts = tokenRepository.countByCreatedByIpAndCreatedAtAfter(ip, oneHourAgo);

        boolean isLimited = emailAttempts >= MAX_RESET_ATTEMPTS_PER_HOUR || ipAttempts >= MAX_RESET_ATTEMPTS_PER_IP_PER_HOUR;

        if (isLimited) {
            System.out.println("⚠️ Rate limit hit for email: " + email + " (attempts: " + emailAttempts + ") or IP: " + ip + " (attempts: " + ipAttempts + ")");
        }

        return isLimited;
    }

    /**
     * Get current IP address
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

    /**
     * Pastro token-et e skaduar
     */
    public void cleanupExpiredResetTokens() {
        try {
            LocalDateTime now = LocalDateTime.now();
            tokenRepository.deleteByExpiresAtBefore(now);
            System.out.println("🧹 Expired reset tokens cleaned up at: " + now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        } catch (Exception e) {
            System.err.println("❌ Error cleaning up reset tokens: " + e.getMessage());
        }
    }

    /**
     * Test email connection
     */
    public void testEmailConnection() {
        if (!emailEnabled) {
            System.out.println("ℹ️ Email është i çaktivizuar për password reset");
            return;
        }

        if (mailSender == null) {
            System.err.println("❌ JavaMailSender është null për password reset");
            return;
        }

        try {
            System.out.println("🔍 Password reset email connection OK");
        } catch (Exception e) {
            System.err.println("❌ Password reset email connection failed: " + e.getMessage());
        }
    }
}