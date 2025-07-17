package com.eFarm.backend.service;

import com.eFarm.backend.dto.LoginRequest;
import com.eFarm.backend.dto.LoginResponse;
import com.eFarm.backend.dto.RegisterRequest;
import com.eFarm.backend.entity.Role;
import com.eFarm.backend.entity.User;
import com.eFarm.backend.repository.RoleRepository;
import com.eFarm.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserDetailsService userDetailsService;

    private static final List<String> VALID_ROLES = Arrays.asList("ADMIN", "KUJDESTAR_KAFSHESH", "KUJDESTAR_FERME");

    public User register(RegisterRequest request) {
        // Validate request
        validateRegisterRequest(request);

        // Create user
        User user = userService.createUser(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getPassword(),
                request.getRoleName(),
                request.getPhoneNumber()
        );

        return user;
    }

    public LoginResponse login(LoginRequest request) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // Get user details
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByEmail(userDetails.getUsername());

            // Check if user is enabled
            if (!user.isEnabled()) {
                throw new RuntimeException("Llogaria nuk është aktivizuar. Ju lutem verifikoni email-in tuaj.");
            }

            // Check if email is verified
            if (!user.isEmailVerified()) {
                throw new RuntimeException("Email-i nuk është verifikuar. Ju lutem verifikoni email-in tuaj.");
            }

            // Generate JWT token
            String token = jwtService.generateToken(userDetails);

            // Update last login
            userService.updateLastLogin(user.getEmail());

            // Create response
            LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail(),
                    user.getPhoneNumber(),
                    user.getRole().getName(),
                    user.isEnabled(),
                    user.isEmailVerified(),
                    user.getLastLogin()
            );

            return new LoginResponse(token, jwtService.getExpirationTime(), userInfo);

        } catch (Exception e) {
            throw new RuntimeException("Email ose password i gabuar", e);
        }
    }

    public void logout