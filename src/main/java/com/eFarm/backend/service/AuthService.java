package com.eFarm.backend.service;

import com.eFarm.backend.dto.AuthResponse;
import com.eFarm.backend.dto.LoginRequest;
import com.eFarm.backend.dto.RegisterRequest;
import com.eFarm.backend.entity.EmailVerificationToken;
import com.eFarm.backend.entity.Role;
import com.eFarm.backend.entity.User;
import com.eFarm.backend.repository.EmailVerificationTokenRepository;
import com.eFarm.backend.repository.RoleRepository;
import com.eFarm.backend.repository.UserRepository;
import com.eFarm.backend.security.CustomUserDetailsService;
import com.eFarm.backend.util.EmailUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private EmailVerificationTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserActivityService userActivityService;

    // Rate limiting: maksimumi 5 përpjekje për orë
    private static final int MAX_VERIFICATION_ATTEMPTS_PER_HOUR = 5;

    /**
     * Login i përmirësuar me validime më të mira
     */
    public AuthResponse login(LoginRequest request) {
        try {
            System.out.println("🔐 Tentativë login për: " + request.getEmail());

            // Valido format e email-it
            if (!EmailUtil.isValidEmail(request.getEmail())) {
                userActivityService.logAnonymousActivity("LOGIN_INVALID_EMAIL",
                        "Tentativë login me email të pavlefshëm: " + request.getEmail());
                throw new BadCredentialsException("Format i pavlefshëm email-i");
            }

            // Gjej user-in me email
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> {
                        userActivityService.logAnonymousActivity("LOGIN_USER_NOT_FOUND",
                                "Tentativë login për email që nuk ekziston: " + request.getEmail());
                        return new BadCredentialsException("Email ose fjalëkalim i gabuar");
                    });

            // Kontrollo nëse llogaria është aktive
            if (!user.getIsActive()) {
                userActivityService.logActivity(user, "LOGIN_ACCOUNT_DISABLED",
                        "Tentativë login për llogari të çaktivizuar");
                throw new DisabledException("Llogaria është e çaktivizuar. Kontaktoni administratorin.");
            }

            // Kontrollo nëse email-i është i verifikuar
            if (!user.getEmailVerified()) {
                userActivityService.logActivity(user, "LOGIN_EMAIL_NOT_VERIFIED",
                        "Tentativë login pa verifikim email");
                throw new DisabledException("Email-i nuk është i verifikuar. Kontrolloni email-in tuaj për kodin e verifikimit.");
            }

            // Kontrollo nëse llogaria është e bllokuar
            if (user.getAccountLockedUntil() != null && LocalDateTime.now().isBefore(user.getAccountLockedUntil())) {
                userActivityService.logActivity(user, "LOGIN_ACCOUNT_LOCKED",
                        "Tentativë login për llogari të bllokuar");
                throw new DisabledException("Llogaria është e bllokuar deri në: " + user.getAccountLockedUntil());
            }

            try {
                // Autentifiko duke përdorur username-in
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(user.getUsername(), request.getPassword())
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Reset failed attempts nëse login është i suksesshëm
                if (user.getFailedLoginAttempts() > 0) {
                    user.setFailedLoginAttempts(0);
                    user.setAccountLockedUntil(null);
                    userRepository.save(user);
                }

                // Përditëso last login
                user.setLastLogin(LocalDateTime.now());
                userRepository.save(user);

                // Gjenero JWT token
                String token = jwtService.generateToken(userDetailsService.loadUserByUsername(user.getUsername()));

                // Përgatit rolet
                Set<String> roles = user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet());

                // Log successful login
                userActivityService.logLogin(user, true);

                System.out.println("✅ Login i suksesshëm për: " + user.getEmail());
                return new AuthResponse(token, user.getId(), user.getUsername(), user.getEmail(),
                        user.getFirstName(), user.getLastName(), roles);

            } catch (BadCredentialsException e) {
                // Increment failed attempts
                int failedAttempts = user.getFailedLoginAttempts() + 1;
                user.setFailedLoginAttempts(failedAttempts);

                // Lock account after 5 failed attempts
                if (failedAttempts >= 5) {
                    user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(30));
                    userActivityService.logAccountLocked(user);
                    userRepository.save(user);
                    throw new RuntimeException("Shumë tentativa të dështuara. Llogaria u bllokua për 30 minuta.");
                }

                userRepository.save(user);
                userActivityService.logLogin(user, false);
                throw new RuntimeException("Email ose fjalëkalim i gabuar. Mbeten " + (5 - failedAttempts) + " tentativa.");
            }

        } catch (BadCredentialsException | DisabledException e) {
            throw new RuntimeException(e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Login error: " + e.getMessage());
            throw new RuntimeException("Gabim gjatë login: " + e.getMessage());
        }
    }

    /**
     * Regjistrimi i përmirësuar - Të gjithë janë KUJDESTAR
     */
    public String register(RegisterRequest request) {
        try {
            System.out.println("📝 Tentativë regjistrimi për: " + request.getEmail());
            System.out.println("   Emri: " + request.getName());

            // Validimet bazike
            validateRegistrationRequest(request);

            // Gjenero username nga email-i
            String username = request.getUsername();

            // Kontrollo nëse username ekziston
            if (userRepository.existsByUsername(username)) {
                // Nëse username ekziston, shto një numër në fund
                int counter = 1;
                String originalUsername = username;
                while (userRepository.existsByUsername(username)) {
                    username = originalUsername + counter;
                    counter++;
                }
            }

            // Kontrollo nëse email ekziston
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email është përdorur nga dikush tjetër!");
            }

            // Rate limiting për email verification
            if (isEmailVerificationRateLimited(request.getEmail())) {
                throw new RuntimeException("Shumë përpjekje verifikimi. Provoni pas 1 ore.");
            }

            // Krijo user të ri (pa e aktivizuar ende)
            User user = new User(username, request.getEmail(),
                    passwordEncoder.encode(request.getPassword()),
                    request.getFirstName(), request.getLastName());

            user.setEmailVerified(false); // Email i paverifikuar
            user.setIsActive(false); // Llogari e paaktivizuar

            // TË GJITHË PËRDORUESIT E RINJ JANË KUJDESTAR
            Set<Role> roles = new HashSet<>();
            Role kujdestarRole = roleRepository.findByName("KUJDESTAR")
                    .orElseThrow(() -> new RuntimeException("Role KUJDESTAR not found"));
            roles.add(kujdestarRole);
            user.setRoles(roles);

            User savedUser = userRepository.save(user);

            // Krijo dhe dërgo kod verifikimi
            EmailVerificationToken token = emailService.createVerificationToken(request.getEmail());
            emailService.sendVerificationEmail(request.getEmail(), token.getToken());

            // Log registration
            userActivityService.logRegistration(savedUser);

            System.out.println("✅ User u regjistrua me sukses: " + savedUser.getEmail() + " si KUJDESTAR");
            return "Regjistrimi u krye me sukses! Kontrolloni email-in tuaj për kodin e verifikimit.";

        } catch (Exception e) {
            System.err.println("❌ Registration error: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Verifiko email me kodin e dërguar
     */
    public String verifyEmail(String email, String code) {
        try {
            System.out.println("✉️ Tentativë verifikimi për: " + email + " me kod: " + code);

            // Valido email format
            if (!EmailUtil.isValidEmail(email)) {
                throw new RuntimeException("Format i pavlefshëm email-i");
            }

            // Gjej user-in
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User nuk u gjet"));

            // Kontrollo nëse email-i është tashmë i verifikuar
            if (user.getEmailVerified()) {
                throw new RuntimeException("Email-i është tashmë i verifikuar");
            }

            // Verifiko kodin
            if (!emailService.verifyCode(email, code)) {
                userActivityService.logEmailVerification(user, false);
                throw new RuntimeException("Kod i pavlefshëm ose i skaduar");
            }

            // Aktivizo user-in
            user.setEmailVerified(true);
            user.setIsActive(true);
            userRepository.save(user);

            // Dërgo email mirëseardhje
            emailService.sendWelcomeEmail(user);

            // Log successful verification
            userActivityService.logEmailVerification(user, true);

            System.out.println("✅ Email u verifikua me sukses për: " + email);
            return "Email-i u verifikua me sukses! Mund të kyçeni tani.";

        } catch (Exception e) {
            System.err.println("❌ Email verification error: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Ridërgo kod verifikimi
     */
    public String resendVerificationCode(String email) {
        try {
            System.out.println("🔄 Ridërgim kodi për: " + email);

            // Valido email format
            if (!EmailUtil.isValidEmail(email)) {
                throw new RuntimeException("Format i pavlefshëm email-i");
            }

            // Gjej user-in
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User nuk u gjet"));

            // Kontrollo nëse email-i është tashmë i verifikuar
            if (user.getEmailVerified()) {
                throw new RuntimeException("Email-i është tashmë i verifikuar");
            }

            // Rate limiting
            if (isEmailVerificationRateLimited(email)) {
                throw new RuntimeException("Shumë përpjekje verifikimi. Provoni pas 1 ore.");
            }

            // Krijo kod të ri dhe dërgoje
            EmailVerificationToken token = emailService.createVerificationToken(email);
            emailService.sendVerificationEmail(email, token.getToken());

            System.out.println("✅ Verification code resent to: " + email);
            return "Kod i ri verifikimi u dërgua në email-in tuaj.";

        } catch (Exception e) {
            System.err.println("❌ Resend verification error: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Kontrollo nëse email ekziston
     */
    public boolean checkEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Kontrollo nëse username ekziston
     */
    public boolean checkUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Valido të dhënat e regjistrimit
     */
    private void validateRegistrationRequest(RegisterRequest request) {
        // Kontrollo nëse fjalëkalimet përputhen
        if (!request.isPasswordMatching()) {
            throw new RuntimeException("Fjalëkalimet nuk përputhen");
        }

        // Kontrollo emrin
        if (request.getName() == null || request.getName().trim().length() < 2) {
            throw new RuntimeException("Emri duhet të ketë të paktën 2 karaktere");
        }

        // Kontrollo email-in
        if (!EmailUtil.isValidEmailForRegistration(request.getEmail())) {
            throw new RuntimeException("Email i pavlefshëm ose i palejueshëm");
        }

        // Kontrollo fjalëkalimin
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new RuntimeException("Fjalëkalimi duhet të ketë të paktën 6 karaktere");
        }

        // Kontrollo nëse emri ka të paktën një emër
        if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
            throw new RuntimeException("Emri duhet të përmbajë të paktën një fjalë");
        }

        // Kontrollo për karaktere të palejueshëm në emër
        if (!request.getName().matches("^[a-zA-ZëËçÇáÁéÉíÍóÓúÚ\\s.-]+$")) {
            throw new RuntimeException("Emri mund të përmbajë vetëm shkronja, hapësira, pika dhe viza");
        }
    }

    /**
     * Kontrollo rate limiting për email verification
     */
    private boolean isEmailVerificationRateLimited(String email) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long attempts = tokenRepository.countByEmailAndCreatedAtAfter(email, oneHourAgo);
        return attempts >= MAX_VERIFICATION_ATTEMPTS_PER_HOUR;
    }

    /**
     * Inicializo rolet
     */
    public void initializeRoles() {
        if (roleRepository.findByName("ADMIN").isEmpty()) {
            Role adminRole = new Role("ADMIN");
            roleRepository.save(adminRole);
            System.out.println("✅ Role ADMIN u krijua!");
        }

        if (roleRepository.findByName("KUJDESTAR").isEmpty()) {
            Role kujdestarRole = new Role("KUJDESTAR");
            roleRepository.save(kujdestarRole);
            System.out.println("✅ Role KUJDESTAR u krijua!");
        }
    }

    /**
     * Krijo admin default
     */
    public void createDefaultAdmin() {
        if (!userRepository.existsByEmail("admin@ferma.al")) {
            User admin = new User("admin", "admin@ferma.al",
                    passwordEncoder.encode("admin123"), "Admin", "Ferma");

            // Admin default është i verifikuar
            admin.setEmailVerified(true);
            admin.setIsActive(true);

            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseThrow(() -> new RuntimeException("Role ADMIN not found"));

            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            admin.setRoles(roles);

            userRepository.save(admin);
            System.out.println("✅ Default admin user created: admin@ferma.al / admin123");
        }
    }
}