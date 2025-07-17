package com.eFarm.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class EmailConfig {

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private int port;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    @Value("${spring.mail.properties.mail.smtp.auth:true}")
    private String auth;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable:true}")
    private String starttls;

    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", auth);
        props.put("mail.smtp.starttls.enable", starttls);
        props.put("mail.debug", "false");

        // Additional properties for better email delivery
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.writetimeout", "5000");

        // SSL configuration if needed
        if (port == 465) {
            props.put("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        }

        return mailSender;
    }

    @Bean
    public EmailConfigProperties emailConfigProperties() {
        return new EmailConfigProperties();
    }

    public static class EmailConfigProperties {
        private String fromName = "eFarm Team";
        private String fromEmail;
        private String replyToEmail;
        private boolean enableEmailValidation = true;
        private boolean enableEmailQueue = false;
        private int maxRetryAttempts = 3;
        private long retryDelay = 5000; // 5 seconds

        // Getters and Setters
        public String getFromName() {
            return fromName;
        }

        public void setFromName(String fromName) {
            this.fromName = fromName;
        }

        public String getFromEmail() {
            return fromEmail;
        }

        public void setFromEmail(String fromEmail) {
            this.fromEmail = fromEmail;
        }

        public String getReplyToEmail() {
            return replyToEmail;
        }

        public void setReplyToEmail(String replyToEmail) {
            this.replyToEmail = replyToEmail;
        }

        public boolean isEnableEmailValidation() {
            return enableEmailValidation;
        }

        public void setEnableEmailValidation(boolean enableEmailValidation) {
            this.enableEmailValidation = enableEmailValidation;
        }

        public boolean isEnableEmailQueue() {
            return enableEmailQueue;
        }

        public void setEnableEmailQueue(boolean enableEmailQueue) {
            this.enableEmailQueue = enableEmailQueue;
        }

        public int getMaxRetryAttempts() {
            return maxRetryAttempts;
        }

        public void setMaxRetryAttempts(int maxRetryAttempts) {
            this.maxRetryAttempts = maxRetryAttempts;
        }

        public long getRetryDelay() {
            return retryDelay;
        }

        public void setRetryDelay(long retryDelay) {
            this.retryDelay = retryDelay;
        }
    }
}