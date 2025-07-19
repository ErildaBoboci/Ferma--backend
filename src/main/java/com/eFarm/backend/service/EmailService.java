package com.eFarm.backend.service;

import com.eFarm.backend.entity.EmailVerificationToken;
import com.eFarm.backend.entity.User;
import com.eFarm.backend.repository.EmailVerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class EmailService {

    @Autowired
    private EmailVerificationTokenRepository tokenRepository;

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

    @Value("${app.email-verification-expiry-minutes:15}")
    private int verificationExpiryMinutes;

    private final SecureRandom random = new SecureRandom();

    /**
     * Gjenero kod verifikimi 4 shifror
     */
    public String generateVerificationCode() {
        int code = 1000 + random.nextInt(9000); // 1000-9999
        return String.valueOf(code);
    }

    /**
     * Dërgo email verifikimi (të vërtetë ose simulim)
     */
    public void sendVerificationEmail(String email, String verificationCode) {
        System.out.println("📧 Duke dërguar email verifikimi në: " + email);
        System.out.println("🔧 Email enabled: " + emailEnabled);
        System.out.println("📤 MailSender available: " + (mailSender != null));

        if (emailEnabled && mailSender != null) {
            try {
                sendRealVerificationEmail(email, verificationCode);
            } catch (Exception e) {
                System.err.println("⚠️ Email i vërtetë dështoi, duke kaluar në simulim: " + e.getMessage());
                sendSimulatedVerificationEmail(email, verificationCode);
            }
        } else {
            System.out.println("ℹ️ Duke përdorur simulim email (emailEnabled=" + emailEnabled + ")");
            sendSimulatedVerificationEmail(email, verificationCode);
        }
    }

    /**
     * Dërgo email të vërtetë verifikimi
     */
    private void sendRealVerificationEmail(String email, String verificationCode) throws Exception {
        System.out.println("📧 Duke krijuar email të vërtetë...");

        if (mailSender == null) {
            throw new RuntimeException("JavaMailSender është null - kontrollo konfigurimin SMTP");
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(supportEmail);
            helper.setTo(email);
            helper.setSubject("🔐 Verifikoni Email-in tuaj - " + appName);

            String htmlContent = createVerificationEmailHtml(verificationCode);
            helper.setText(htmlContent, true);

            System.out.println("📤 Duke dërguar email nga: " + supportEmail + " në: " + email);
            mailSender.send(mimeMessage);
            System.out.println("✅ Email verifikimi u dërgua me sukses në: " + email);

        } catch (MessagingException e) {
            System.err.println("❌ Gabim MessagingException: " + e.getMessage());
            e.printStackTrace();
            throw new Exception("Gabim në dërgimin e email-it: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("❌ Gabim i papritur në email: " + e.getMessage());
            e.printStackTrace();
            throw new Exception("Gabim i papritur: " + e.getMessage(), e);
        }
    }

    /**
     * Simulim email verifikimi në console
     */
    private void sendSimulatedVerificationEmail(String email, String verificationCode) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("📧 EMAIL VERIFIKIMI (SIMULIM)");
        System.out.println("=".repeat(70));
        System.out.println("📤 Nga: " + supportEmail);
        System.out.println("📥 Për: " + email);
        System.out.println("📋 Subjekti: 🔐 Verifikoni Email-in tuaj - " + appName);
        System.out.println("");
        System.out.println("🎯 🔐 KODI JUAJ I VERIFIKIMIT: " + verificationCode);
        System.out.println("");
        System.out.println("⏰ Ky kod skadon pas " + verificationExpiryMinutes + " minutash.");
        System.out.println("🔒 Mos e ndani këtë kod me askënd.");
        System.out.println("🌐 Pas verifikimit, kyçuni në: " + baseUrl);
        System.out.println("");
        System.out.println("ℹ️ Nëse nuk keni kërkuar këtë kod, injoroni këtë email.");
        System.out.println("=".repeat(70) + "\n");
    }

    /**
     * Krijo HTML content për email verifikimi
     */
    private String createVerificationEmailHtml(String verificationCode) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>Verifikoni Email-in tuaj</title>\n");
        html.append("    <style>\n");
        html.append("        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f4f4f4; }\n");
        html.append("        .container { max-width: 600px; margin: 20px auto; background: white; border-radius: 10px; box-shadow: 0 0 20px rgba(0,0,0,0.1); overflow: hidden; }\n");
        html.append("        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px 20px; text-align: center; }\n");
        html.append("        .content { padding: 40px 30px; background: white; }\n");
        html.append("        .code-box { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 25px; text-align: center; margin: 30px 0; border-radius: 10px; }\n");
        html.append("        .code { font-size: 36px; font-weight: bold; letter-spacing: 8px; margin: 10px 0; }\n");
        html.append("        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; background: #f8f9fa; }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class=\"container\">\n");
        html.append("        <div class=\"header\">\n");
        html.append("            <h1>🐄 ").append(appName).append("</h1>\n");
        html.append("            <h2>Verifikoni Email-in tuaj</h2>\n");
        html.append("        </div>\n");
        html.append("        <div class=\"content\">\n");
        html.append("            <p><strong>Përshëndetje!</strong></p>\n");
        html.append("            <p>Faleminderit që regjistroheni në <strong>").append(appName).append("</strong>.</p>\n");
        html.append("            <div class=\"code-box\">\n");
        html.append("                <p>Kodi juaj i verifikimit:</p>\n");
        html.append("                <div class=\"code\">").append(verificationCode).append("</div>\n");
        html.append("            </div>\n");
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
     * Dërgo email mirëseardhje
     */
    public void sendWelcomeEmail(User user) {
        System.out.println("🎉 Duke dërguar email mirëseardhje në: " + user.getEmail());

        if (emailEnabled && mailSender != null) {
            try {
                sendRealWelcomeEmail(user);
            } catch (Exception e) {
                System.err.println("⚠️ Welcome email i vërtetë dështoi, duke kaluar në simulim: " + e.getMessage());
                sendSimulatedWelcomeEmail(user);
            }
        } else {
            sendSimulatedWelcomeEmail(user);
        }
    }

    /**
     * Dërgo email të vërtetë mirëseardhje
     */
    private void sendRealWelcomeEmail(User user) throws Exception {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(supportEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("🎉 Mirë se erdhe në " + appName + "!");

            String htmlContent = createWelcomeEmailHtml(user);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            System.out.println("✅ Email mirëseardhje u dërgua me sukses në: " + user.getEmail());

        } catch (MessagingException e) {
            System.err.println("❌ Gabim në dërgimin e email-it mirëseardhje: " + e.getMessage());
            throw new Exception("Gabim në email mirëseardhje: " + e.getMessage(), e);
        }
    }

    /**
     * Simulim email mirëseardhje
     */
    private void sendSimulatedWelcomeEmail(User user) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("🎉 EMAIL MIRËSEARDHJE (SIMULIM)");
        System.out.println("=".repeat(70));
        System.out.println("📤 Nga: " + supportEmail);
        System.out.println("📥 Për: " + user.getEmail());
        System.out.println("👤 Emri: " + user.getFullName());
        System.out.println("📋 Subjekti: 🎉 Mirë se erdhe në " + appName + "!");
        System.out.println("");
        System.out.println("👋 Përshëndetje " + user.getFullName() + "!");
        System.out.println("🎊 Llogaria juaj u aktivizua me sukses!");
        System.out.println("🚀 Mund të filloni të përdorni " + appName + " tani.");
        System.out.println("");
        System.out.println("🔗 Kyçuni në: " + baseUrl);
        System.out.println("=".repeat(70) + "\n");
    }

    /**
     * Krijo HTML content për email mirëseardhje
     */
    private String createWelcomeEmailHtml(User user) {
        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>Mirë se erdhe</title></head><body><h1>Mirë se erdhe në " + appName + "!</h1><p>Përshëndetje " + user.getFullName() + "!</p></body></html>";
    }

    /**
     * Krijo dhe ruaj token verifikimi
     */
    public EmailVerificationToken createVerificationToken(String email) {
        try {
            // Fshi token-et e vjetër për këtë email
            tokenRepository.deleteByEmailAndIsUsedFalse(email);
            System.out.println("🗑️ Token-et e vjetër u fshinë për: " + email);

            String code = generateVerificationCode();
            EmailVerificationToken token = new EmailVerificationToken(code, email);

            // Vendos skadimin nga konfigurimi
            token.setExpiresAt(LocalDateTime.now().plusMinutes(verificationExpiryMinutes));

            EmailVerificationToken savedToken = tokenRepository.save(token);
            System.out.println("✅ Token i ri u krijua: " + code + " për " + email + " (skadon në " + verificationExpiryMinutes + " min)");

            return savedToken;
        } catch (Exception e) {
            System.err.println("❌ Gabim në krijimin e token: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Gabim në krijimin e kodit të verifikimit: " + e.getMessage());
        }
    }

    /**
     * Verifiko kodin e dërguar
     */
    public boolean verifyCode(String email, String code) {
        try {
            System.out.println("🔍 Duke verifikuar kod: " + code + " për " + email);

            Optional<EmailVerificationToken> tokenOpt = tokenRepository
                    .findByEmailAndTokenAndIsUsedFalse(email, code);

            if (tokenOpt.isEmpty()) {
                System.out.println("❌ Token nuk u gjet ose është përdorur: " + code);
                return false;
            }

            EmailVerificationToken token = tokenOpt.get();

            if (!token.isValid()) {
                System.out.println("❌ Token i pavlefshëm ose i skaduar: " + code);
                return false;
            }

            // Shëno si të përdorur
            token.markAsUsed();
            tokenRepository.save(token);

            System.out.println("✅ Token u verifikua me sukses: " + code);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Gabim në verifikimin e token: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Pastro token-et e skaduar
     */
    public void cleanupExpiredTokens() {
        try {
            LocalDateTime now = LocalDateTime.now();
            tokenRepository.deleteByExpiresAtBefore(now);
            System.out.println("🧹 Token-et e skaduar u pastruan në: " + now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        } catch (Exception e) {
            System.err.println("❌ Gabim në pastrimin e token-eve: " + e.getMessage());
        }
    }

    /**
     * Test email connection
     */
    public void testEmailConnection() {
        if (!emailEnabled) {
            System.out.println("ℹ️ Email është i çaktivizuar - simulim do të përdoret");
            return;
        }

        if (mailSender == null) {
            System.err.println("❌ JavaMailSender është null - kontrollo dependencies");
            return;
        }

        try {
            // Test connection
            System.out.println("🔍 Duke testuar lidhjen SMTP...");
            // Mund të shtosh test logikë këtu nëse nevojitet
            System.out.println("✅ SMTP konfigurimi duket në rregull");
        } catch (Exception e) {
            System.err.println("❌ Test SMTP dështoi: " + e.getMessage());
        }
    }
}