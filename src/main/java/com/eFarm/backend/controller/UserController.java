package com.eFarm.backend.controller;

import com.eFarm.backend.dto.ApiResponse;
import com.eFarm.backend.entity.User;
import com.eFarm.backend.service.AuthService;
import com.eFarm.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<User>> getProfile() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User user = userService.findByEmail(email);
            return ResponseEntity.ok(ApiResponse.success("Profili i përdoruesit.", user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.badRequest(e.getMessage()));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<User>> updateProfile(@RequestBody UpdateProfileRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User currentUser = userService.findByEmail(email);

            User updatedUser = userService.updateUser(
                    currentUser.getId(),
                    request.getFirstName(),
                    request.getLastName(),
                    request.getPhoneNumber()
            );

            return ResponseEntity.ok(ApiResponse.success("Profili u përditësua me sukses.", updatedUser));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.badRequest(e.getMessage()));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(@RequestBody ChangePasswordRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User currentUser = userService.findByEmail(email);

            userService.changePassword(currentUser.getId(), request.getCurrentPassword(), request.getNewPassword());
            return ResponseEntity.ok(ApiResponse.success("Password-i u ndryshua me sukses.", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.badRequest(e.getMessage()));
        }
    }

    // Admin endpoints
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok(ApiResponse.success("Lista e të gjithë përdoruesve.", users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalServerError(e.getMessage()));
        }
    }

    @GetMapping("/admin/enabled")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<User>>> getEnabledUsers() {
        try {
            List<User> users = userService.getEnabledUsers();
            return ResponseEntity.ok(ApiResponse.success("Lista e përdoruesve të aktivizuar.", users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalServerError(e.getMessage()));
        }
    }

    @GetMapping("/admin/by-role/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<User>>> getUsersByRole(@PathVariable String roleName) {
        try {
            List<User> users = userService.getUsersByRole(roleName);
            return ResponseEntity.ok(ApiResponse.success("Lista e përdoruesve sipas rolit: " + roleName, users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.badRequest(e.getMessage()));
        }
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
        try {
            User user = userService.findById(id);
            return ResponseEntity.ok(ApiResponse.success("Të dhënat e përdoruesit.", user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notFound(e.getMessage()));
        }
    }

    @PutMapping("/admin/{id}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> enableUser(@PathVariable Long id) {
        try {
            userService.enableUser(id);
            return ResponseEntity.ok(ApiResponse.success("Përdoruesi u aktivizua me sukses.", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.badRequest(e.getMessage()));
        }
    }

    @PutMapping("/admin/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> disableUser(@PathVariable Long id) {
        try {
            userService.disableUser(id);
            return ResponseEntity.ok(ApiResponse.success("Përdoruesi u çaktivizua me sukses.", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.badRequest(e.getMessage()));
        }
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(ApiResponse.success("Përdoruesi u fshi me sukses.", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.badRequest(e.getMessage()));
        }
    }

    @GetMapping("/admin/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<User>>> searchUsers(@RequestParam String query) {
        try {
            List<User> users = userService.searchUsers(query);
            return ResponseEntity.ok(ApiResponse.success("Rezultatet e kërkimit.", users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.badRequest(e.getMessage()));
        }
    }

    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserStats>> getUserStats() {
        try {
            UserStats stats = new UserStats();
            stats.setTotalUsers(userService.getTotalUsersCount());
            stats.setEnabledUsers(userService.getEnabledUsersCount());
            stats.setAdminCount(userService.getUsersCountByRole("ADMIN"));
            stats.setKujdestarKafsheshCount(userService.getUsersCountByRole("KUJDESTAR_KAFSHESH"));
            stats.setKujdestarFermeCount(userService.getUsersCountByRole("KUJDESTAR_FERME"));
            stats.setNewUsersThisMonth(userService.getNewUsersCount(LocalDateTime.now().withDayOfMonth(1)));

            return ResponseEntity.ok(ApiResponse.success("Statistikat e përdoruesve.", stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalServerError(e.getMessage()));
        }
    }

    @PostMapping("/admin/cleanup-tokens")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> cleanupExpiredTokens() {
        try {
            userService.cleanupExpiredTokens();
            return ResponseEntity.ok(ApiResponse.success("Token-at e skaduara u pastruan me sukses.", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalServerError(e.getMessage()));
        }
    }

    // DTO classes for request bodies
    public static class UpdateProfileRequest {
        private String firstName;
        private String lastName;
        private String phoneNumber;

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }
    }

    public static class ChangePasswordRequest {
        private String currentPassword;
        private String newPassword;

        public String getCurrentPassword() {
            return currentPassword;
        }

        public void setCurrentPassword(String currentPassword) {
            this.currentPassword = currentPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }

    public static class UserStats {
        private long totalUsers;
        private long enabledUsers;
        private long adminCount;
        private long kujdestarKafsheshCount;
        private long kujdestarFermeCount;
        private long newUsersThisMonth;

        public long getTotalUsers() {
            return totalUsers;
        }

        public void setTotalUsers(long totalUsers) {
            this.totalUsers = totalUsers;
        }

        public long getEnabledUsers() {
            return enabledUsers;
        }

        public void setEnabledUsers(long enabledUsers) {
            this.enabledUsers = enabledUsers;
        }

        public long getAdminCount() {
            return adminCount;
        }

        public void setAdminCount(long adminCount) {
            this.adminCount = adminCount;
        }

        public long getKujdestarKafsheshCount() {
            return kujdestarKafsheshCount;
        }

        public void setKujdestarKafsheshCount(long kujdestarKafsheshCount) {
            this.kujdestarKafsheshCount = kujdestarKafsheshCount;
        }

        public long getKujdestarFermeCount() {
            return kujdestarFermeCount;
        }

        public void setKujdestarFermeCount(long kujdestarFermeCount) {
            this.kujdestarFermeCount = kujdestarFermeCount;
        }

        public long getNewUsersThisMonth() {
            return newUsersThisMonth;
        }

        public void setNewUsersThisMonth(long newUsersThisMonth) {
            this.newUsersThisMonth = newUsersThisMonth;
        }
    }