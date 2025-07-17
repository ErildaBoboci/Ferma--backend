package com.eFarm.backend.controller;

import com.eFarm.backend.dto.*;
import com.eFarm.backend.entity.User;
import com.eFarm.backend.service.AuthService;
import com.eFarm.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.created("Regjistrimi u krye me sukses. Ju lutem kontrolloni email-in tuaj për verifikim.", user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.badRequest(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success("Hyrja u krye me sukses.", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.unauthorized(e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.substring(7); // Remove "Bearer " prefix
            authService.logout(jwtToken);
            return ResponseEntity.ok(ApiResponse.success("Dalja u krye me sukses.", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.badRequest(e.getMessage()));
        }
    }

    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<String>> verifyEmail(@RequestParam("token") String token) {
        try {
            boolean isVerified = authService.verifyEmail(token);
            if (isVerified) {
                return ResponseEntity.ok(ApiResponse.success("Email-i u verifikua me sukses. Llogaria juaj është tani aktive.", null));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.badRequest("Token i verifikimit është i pavlefshëm ose ka skaduar."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.badRequest(e.getMessage()));
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<String>> resendVerification(@RequestBody ResendVerificationRequest request) {
        try {
            authService.resendVerificationEmail(request.getEmail());
            return ResponseEntity.ok(ApiResponse.success("Email-i i verifikimit u dërgua përsëri.", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.badRequest(e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            authService.forgotPassword(request.getEmail());
            return ResponseEntity.ok(ApiResponse.success("Udhëzimet për rivendosjen e password-it u dërguan në email.", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.badRequest(e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            boolean isReset = authService.resetPassword(request.getToken(), request.getNewPassword());
            if (isReset) {
                return ResponseEntity.ok(ApiResponse.success("Password-i u rivendos me sukses.", null));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.badRequest("Token i rivendosjes është i pavlefshëm ose ka skaduar."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.badRequest(e.getMessage()));
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<String>> refreshToken(@RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.substring(7); // Remove "Bearer " prefix
            String newToken = authService.refreshToken(jwtToken);
            return ResponseEntity.ok(ApiResponse.success("Token u rifreskua me sukses.", newToken));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.unauthorized(e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>> getCurrentUser(@RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.substring(7); // Remove "Bearer " prefix
            User user = authService.getCurrentUser(jwtToken);
            return ResponseEntity.ok(ApiResponse.success("Të dhënat e përdoruesit.", user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.unauthorized(e.getMessage()));
        }
    }

    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<List<String>>> getValidRoles() {
        try {
            List<String> roles = authService.getValidRoles();
            return ResponseEntity.ok(ApiResponse.success("Rolet e vlefshme.", roles));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalServerError(e.getMessage()));
        }
    }

    // Additional DTO classes for request bodies
    public static class ResendVerificationRequest {
        private String email;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    public static class ForgotPasswordRequest {
        private String email;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    public static class ResetPasswordRequest {
        private String token;
        private String newPassword;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
}