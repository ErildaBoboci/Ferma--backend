package com.eFarm.backend.service;

import com.eFarm.backend.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name}")
    private String appName;

    @Value("${app.base-url}")
    private String baseUrl;

    public void sendVerificationEmail(User user, String token) {
        try {
            String verificationUrl = baseUrl + "/auth/verify-email?token=" + token;

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, appName);
            helper.setTo(user.getEmail());
            helper.setSubject("Verifikoni Email-in tuaj - " + appName);

            String htmlContent = buildVerificationEmailContent(user, verificationUrl);
            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException("Dështoi dërgimi i email-it të verifikimit", e);
        }
    }

    public void sendPasswordResetEmail(User user, String token) {
        try {
            String resetUrl = baseUrl + "/auth/reset-password?token=" + token;

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, appName);
            helper.setTo(user.getEmail());
            helper.setSubject("Rivendosni Password-in tuaj - " + appName);

            String htmlContent = buildPasswordResetEmailContent(user, resetUrl);
            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException("Dështoi dërgimi i email-it të rivendosjes së password-it", e);
        }
    }

    public void sendWelcomeEmail(User user) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, appName);
            helper.setTo(user.getEmail());
            helper.setSubject("Mirë se erdhe në " + appName + "!");

            String htmlContent = buildWelcomeEmailContent(user);
            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException("Dështoi dërgimi i email-it të mirëseardhjes", e);
        }
    }

    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Dështoi dërgimi i email-it", e);
        }
    }

    private String buildVerificationEmailContent(User user, String verificationUrl) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Verifikoni Email-in tuaj</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9f9f9; }
                    .button { display: inline-block; padding: 12px 30px; background-color: #4CAF50; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { padding: 20px; text-align: center; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>""" + appName + """
                        </h1>
                    </div>
                    <div class="content">
                        <h2>Përshëndetje """ + user.getFullName() + """
                        !</h2>
                        <p>Faleminderit për regjistrimin në """ + appName + """
                        . Për të aktivizuar llogarinë tuaj, ju lutem klikoni butonin më poshtë për të verifikuar email-in tuaj:</p>
                        
                        <a href=\"""" + verificationUrl + """
                        \" class="button">Verifikoni Email-in</a>
                        
                        <p>Nëse butoni nuk funksionon, kopjoni dhe ngjisni këtë link në shfletuesin tuaj:</p>
                        <p>""" + verificationUrl + """
                        </p>
                        
                        <p><strong>Shënim:</strong> Ky link do të skadojë pas 24 orësh për arsye sigurie.</p>
                        
                        <p>Nëse nuk keni kërkuar këtë verifikim, ju lutem injoroni këtë email.</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 """ + appName + """
                        . Të gjitha të drejtat e rezervuara.</p>
                    </div>
                </div>
            </body>
            </html>
            """;
    }

    private String buildPasswordResetEmailContent(User user, String resetUrl) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Rivendosni Password-in tuaj</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #FF9800; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9f9f9; }
                    .button { display: inline-block; padding: 12px 30px; background-color: #FF9800; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { padding: 20px; text-align: center; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>""" + appName + """
                        </h1>
                    </div>
                    <div class="content">
                        <h2>Përshëndetje """ + user.getFullName() + """
                        !</h2>
                        <p>Kemi marrë një kërkesë për të rivendosur password-in tuaj për llogarinë në """ + appName + """
                        .</p>
                        
                        <p>Nëse keni bërë këtë kërkesë, klikoni butonin më poshtë për të rivendosur password-in tuaj:</p>
                        
                        <a href=\"""" + resetUrl + """
                        \" class="button">Rivendosni Password-in</a>
                        
                        <p>Nëse butoni nuk funksionon, kopjoni dhe ngjisni këtë link në shfletuesin tuaj:</p>
                        <p>""" + resetUrl + """
                        </p>
                        
                        <p><strong>Shënim:</strong> Ky link do të skadojë pas 1 ore për arsye sigurie.</p>
                        
                        <p>Nëse nuk keni kërkuar rivendosjen e password-it, ju lutem injoroni këtë email ose kontaktoni përkrahjen tonë.</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 """ + appName + """
                        . Të gjitha të drejtat e rezervuara.</p>
                    </div>
                </div>
            </body>
            </html>
            """;
    }

    private String buildWelcomeEmailContent(User user) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Mirë se erdhe!</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #2196F3; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9f9f9; }
                    .button { display: inline-block; padding: 12px 30px; background-color: #2196F3; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { padding: 20px; text-align: center; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Mirë se erdhe në """ + appName + """
                        !</h1>
                    </div>
                    <div class="content">
                        <h2>Përshëndetje """ + user.getFullName() + """
                        !</h2>
                        <p>Urime! Llogaria juaj në """ + appName + """
                         është verifikuar me sukses dhe tani jeni gati të filloni.</p>
                        
                        <p>Si """ + user.getRole().getName() + """
                        , ju keni qasje në të gjitha funksionalitetet e nevojshme për të menaxhuar detyrat tuaja.</p>
                        
                        <p>Nëse keni ndonjë pyetje ose keni nevojë për ndihmë, mos hezitoni të na kontaktoni.</p>
                        
                        <a href=\"""" + baseUrl + """
                        \" class="button">Shkoni në Platformë</a>
                        
                        <p>Faleminderit që zgjodhët """ + appName + """
                        !</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 """ + appName + """
                        . Të gjitha të drejtat e rezervuara.</p>
                    </div>
                </div>
            </body>
            </html>
            """;
    }
}