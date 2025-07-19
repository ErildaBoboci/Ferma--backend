package com.eFarm.backend.controller;

import com.eFarm.backend.dto.*;
import com.eFarm.backend.entity.User;
import com.eFarm.backend.repository.UserRepository;
import com.eFarm.backend.service.AuthService;
import com.eFarm.backend.service.EmailService;
import com.eFarm.backend.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Login endpoint
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            System.out.println("🔐 Login përpjekje për: " + request.getEmail());

            AuthResponse response = authService.login(request);

            System.out.println("✅ Login i suksesshëm për: " + request.getEmail());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Login dështoi për " + request.getEmail() + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(VerificationResponse.error(e.getMessage()));
        }
    }

    /**
     * Register endpoint
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            System.out.println("📝 Përpjekje regjistrimi për: " + request.getEmail());
            System.out.println("   Emri: " + request.getName());
            System.out.println("   Roli: " + request.getRole());

            String message = authService.register(request);

            System.out.println("✅ Regjistrimi i suksesshëm për: " + request.getEmail());
            return ResponseEntity.ok(VerificationResponse.success(message, "EMAIL_VERIFICATION"));

        } catch (Exception e) {
            System.err.println("❌ Regjistrimi dështoi për " + request.getEmail() + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(VerificationResponse.error(e.getMessage()));
        }
    }

    /**
     * Email verification endpoint
     */
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody EmailVerificationRequest request) {
        try {
            System.out.println("✉️ Përpjekje verifikimi për: " + request.getEmail() + " me kod: " + request.getCode());

            String message = authService.verifyEmail(request.getEmail(), request.getCode());

            System.out.println("✅ Verifikimi i suksesshëm për: " + request.getEmail());
            return ResponseEntity.ok(VerificationResponse.success(message, "LOGIN"));

        } catch (Exception e) {
            System.err.println("❌ Verifikimi dështoi për " + request.getEmail() + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(VerificationResponse.error(e.getMessage()));
        }
    }

    /**
     * Resend verification code endpoint
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerificationCode(@Valid @RequestBody ResendCodeRequest request) {
        try {
            System.out.println("🔄 Ridërgim kodi për: " + request.getEmail());

            String message = authService.resendVerificationCode(request.getEmail());

            System.out.println("✅ Kodi u ridërgua për: " + request.getEmail());
            return ResponseEntity.ok(VerificationResponse.success(message));

        } catch (Exception e) {
            System.err.println("❌ Ridërgimi dështoi për " + request.getEmail() + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(VerificationResponse.error(e.getMessage()));
        }
    }

    /**
     * FORGOT PASSWORD - Dërgo email për reset
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            System.out.println("🔒 Forgot password për: " + request.getEmail());

            String message = passwordResetService.sendForgotPasswordEmail(request.getEmail());

            System.out.println("✅ Forgot password email u dërgua për: " + request.getEmail());
            return ResponseEntity.ok(VerificationResponse.success(message, "PASSWORD_RESET"));

        } catch (Exception e) {
            System.err.println("❌ Forgot password dështoi për " + request.getEmail() + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(VerificationResponse.error(e.getMessage()));
        }
    }

    /**
     * RESET PASSWORD - Reset password me token
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            System.out.println("🔑 Reset password për: " + request.getEmail() + " me token: " + request.getToken());

            // Validim shtesë për password matching
            if (!request.isPasswordMatching()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(VerificationResponse.error("Fjalëkalimet nuk përputhen"));
            }

            String message = passwordResetService.resetPassword(
                    request.getEmail(),
                    request.getToken(),
                    request.getNewPassword(),
                    request.getConfirmPassword()
            );

            System.out.println("✅ Password u reset me sukses për: " + request.getEmail());
            return ResponseEntity.ok(VerificationResponse.success(message, "LOGIN"));

        } catch (Exception e) {
            System.err.println("❌ Password reset dështoi për " + request.getEmail() + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(VerificationResponse.error(e.getMessage()));
        }
    }

    /**
     * Test endpoint për të kontrolluar nëse backend-i funksionon
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        System.out.println("🧪 Test endpoint u thirr");
        return ResponseEntity.ok("✅ eFarm Backend funksionon! " + java.time.LocalDateTime.now());
    }

    /**
     * Test endpoint për email (për debugging)
     */
    @GetMapping("/test-email/{email}")
    public ResponseEntity<?> testEmail(@PathVariable String email) {
        try {
            String testCode = emailService.generateVerificationCode();
            emailService.sendVerificationEmail(email, testCode);

            System.out.println("📧 Test email u dërgua në: " + email);
            System.out.println("🔐 Kod test: " + testCode);

            return ResponseEntity.ok(VerificationResponse.success(
                    "Test email u dërgua me sukses! Kod: " + testCode
            ));

        } catch (Exception e) {
            System.err.println("❌ Test email dështoi: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(VerificationResponse.error("Test email dështoi: " + e.getMessage()));
        }
    }

    /**
     * Test endpoint për password reset email
     */
    @GetMapping("/test-reset-email/{email}")
    public ResponseEntity<?> testResetEmail(@PathVariable String email) {
        try {
            String message = passwordResetService.sendForgotPasswordEmail(email);

            System.out.println("📧 Test reset email u dërgua në: " + email);

            return ResponseEntity.ok(VerificationResponse.success(
                    "Test reset email u dërgua me sukses!"
            ));

        } catch (Exception e) {
            System.err.println("❌ Test reset email dështoi: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(VerificationResponse.error("Test reset email dështoi: " + e.getMessage()));
        }
    }

    /**
     * Test welcome email
     */
    @GetMapping("/test-welcome/{email}")
    public ResponseEntity<?> testWelcomeEmail(@PathVariable String email) {
        try {
            User testUser = new User();
            testUser.setEmail(email);
            testUser.setFirstName("Test");
            testUser.setLastName("User");
            testUser.setUsername("testuser");
            testUser.setCreatedAt(LocalDateTime.now());

            com.eFarm.backend.entity.Role testRole = new com.eFarm.backend.entity.Role("KUJDESTAR");
            Set<com.eFarm.backend.entity.Role> roles = new HashSet<>();
            roles.add(testRole);
            testUser.setRoles(roles);

            emailService.sendWelcomeEmail(testUser);

            return ResponseEntity.ok(VerificationResponse.success(
                    "Welcome email u dërgua me sukses!"
            ));

        } catch (Exception e) {
            System.err.println("❌ Test welcome email dështoi: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(VerificationResponse.error("Test welcome email dështoi: " + e.getMessage()));
        }
    }

    /**
     * Check email availability
     */
    @GetMapping("/check-email/{email}")
    public ResponseEntity<?> checkEmailAvailability(@PathVariable String email) {
        try {
            System.out.println("📧 Kontrollim email: " + email);

            boolean exists = authService.checkEmailExists(email);
            if (exists) {
                return ResponseEntity.ok(VerificationResponse.error("Email është përdorur nga dikush tjetër"));
            } else {
                return ResponseEntity.ok(VerificationResponse.success("Email është i disponueshëm"));
            }
        } catch (Exception e) {
            System.err.println("❌ Gabim në kontrollimin e email-it: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(VerificationResponse.error("Gabim në kontrollimin e email-it"));
        }
    }

    /**
     * Application status endpoint
     */
    @GetMapping("/status")
    public ResponseEntity<?> getApplicationStatus() {
        try {
            return ResponseEntity.ok(VerificationResponse.success(
                    "✅ eFarm Backend po funksionon normalisht",
                    "READY"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(VerificationResponse.error("❌ Gabim në server"));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        try {
            return ResponseEntity.ok().body(new HealthResponse(
                    true,
                    "Backend Health OK",
                    java.time.LocalDateTime.now().toString(),
                    "1.0.0"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new HealthResponse(
                            false,
                            "Backend Health ERROR: " + e.getMessage(),
                            java.time.LocalDateTime.now().toString(),
                            "1.0.0"
                    ));
        }
    }

    /**
     * Simple health response class
     */
    public static class HealthResponse {
        private boolean healthy;
        private String message;
        private String timestamp;
        private String version;

        public HealthResponse(boolean healthy, String message, String timestamp, String version) {
            this.healthy = healthy;
            this.message = message;
            this.timestamp = timestamp;
            this.version = version;
        }

        // Getters
        public boolean isHealthy() { return healthy; }
        public String getMessage() { return message; }
        public String getTimestamp() { return timestamp; }
        public String getVersion() { return version; }
    }
}