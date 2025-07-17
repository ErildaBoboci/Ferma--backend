package com.eFarm.backend.service;

import com.eFarm.backend.entity.Role;
import com.eFarm.backend.entity.User;
import com.eFarm.backend.repository.RoleRepository;
import com.eFarm.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    public User createUser(String firstName, String lastName, String email, String password, String roleName, String phoneNumber) {
        // Check if user already exists
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("User me këtë email tashmë ekziston");
        }

        // Find role
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Roli nuk u gjet: " + roleName));

        // Create user
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setPhoneNumber(phoneNumber);
        user.setRole(role);
        user.setEnabled(false);
        user.setEmailVerified(false);

        // Generate email verification token
        String verificationToken = UUID.randomUUID().toString();
        user.setEmailVerificationToken(verificationToken);
        user.setEmailVerificationExpiresAt(LocalDateTime.now().plusHours(24));

        User savedUser = userRepository.save(user);

        // Send verification email
        emailService.sendVerificationEmail(savedUser, verificationToken);

        return savedUser;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User nuk u gjet me email: " + email));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User nuk u gjet me ID: " + id));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getEnabledUsers() {
        return userRepository.findAllEnabledUsers();
    }

    public List<User> getUsersByRole(String roleName) {
        return userRepository.findByRoleName(roleName);
    }

    public boolean verifyEmail(String token) {
        Optional<User> userOptional = userRepository.findByEmailVerificationToken(token);

        if (userOptional.isEmpty()) {
            return false;
        }

        User user = userOptional.get();

        // Check if token is expired
        if (user.getEmailVerificationExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }

        // Verify email
        user.setEmailVerified(true);
        user.setEnabled(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationExpiresAt(null);

        userRepository.save(user);

        // Send welcome email
        emailService.sendWelcomeEmail(user);

        return true;
    }

    public void resendVerificationEmail(String email) {
        User user = findByEmail(email);

        if (user.isEmailVerified()) {
            throw new RuntimeException("Email-i është tashmë i verifikuar");
        }

        // Generate new token
        String verificationToken = UUID.randomUUID().toString();
        user.setEmailVerificationToken(verificationToken);
        user.setEmailVerificationExpiresAt(LocalDateTime.now().plusHours(24));

        userRepository.save(user);

        // Send verification email
        emailService.sendVerificationEmail(user, verificationToken);
    }

    public void initiatePasswordReset(String email) {
        User user = findByEmail(email);

        // Generate password reset token
        String resetToken = UUID.randomUUID().toString();
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetExpiresAt(LocalDateTime.now().plusHours(1));

        userRepository.save(user);

        // Send password reset email
        emailService.sendPasswordResetEmail(user, resetToken);
    }

    public boolean resetPassword(String token, String newPassword) {
        Optional<User> userOptional = userRepository.findByPasswordResetToken(token);

        if (userOptional.isEmpty()) {
            return false;
        }

        User user = userOptional.get();

        // Check if token is expired
        if (user.getPasswordResetExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }

        // Reset password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiresAt(null);

        userRepository.save(user);

        return true;
    }

    public User updateUser(Long id, String firstName, String lastName, String phoneNumber) {
        User user = findById(id);

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhoneNumber(phoneNumber);

        return userRepository.save(user);
    }

    public void updateLastLogin(String email) {
        User user = findByEmail(email);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }

    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = findById(userId);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Password aktuale është i gabuar");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void enableUser(Long userId) {
        User user = findById(userId);
        user.setEnabled(true);
        userRepository.save(user);
    }

    public void disableUser(Long userId) {
        User user = findById(userId);
        user.setEnabled(false);
        userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        User user = findById(userId);
        userRepository.delete(user);
    }

    public List<User> searchUsers(String query) {
        return userRepository.findByNameOrEmail(query);
    }

    public long getTotalUsersCount() {
        return userRepository.count();
    }

    public long getEnabledUsersCount() {
        return userRepository.countEnabledUsers();
    }

    public long getUsersCountByRole(String roleName) {
        return userRepository.countByRoleName(roleName);
    }

    public long getNewUsersCount(LocalDateTime since) {
        return userRepository.countUsersCreatedAfter(since);
    }

    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();

        // Find users with expired verification tokens
        List<User> expiredVerificationUsers = userRepository.findUsersWithExpiredVerificationTokens(now);
        for (User user : expiredVerificationUsers) {
            user.setEmailVerificationToken(null);
            user.setEmailVerificationExpiresAt(null);
        }

        // Find users with expired password reset tokens
        List<User> expiredResetUsers = userRepository.findUsersWithExpiredPasswordResetTokens(now);
        for (User user : expiredResetUsers) {
            user.setPasswordResetToken(null);
            user.setPasswordResetExpiresAt(null);
        }

        userRepository.saveAll(expiredVerificationUsers);
        userRepository.saveAll(expiredResetUsers);
    }
}