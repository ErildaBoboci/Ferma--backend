package com.ferma.service;

import com.ferma.dto.AuthResponse;
import com.ferma.dto.LoginRequest;
import com.ferma.dto.RegisterRequest;
import com.ferma.entity.Role;
import com.ferma.entity.User;
import com.ferma.repository.RoleRepository;
import com.ferma.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserService userService;

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtService.generateToken(userService.loadUserByUsername(user.getUsername()));

        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return new AuthResponse(token, user.getId(), user.getUsername(), user.getEmail(),
                user.getFirstName(), user.getLastName(), roles);
    }

    public String register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username është përdorur nga dikush tjetër!");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email është përdorur nga dikush tjetër!");
        }

        User user = new User(request.getUsername(), request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getFirstName(), request.getLastName());

        Set<Role> roles = new HashSet<>();

        if (request.getRole().equalsIgnoreCase("ADMIN")) {
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseThrow(() -> new RuntimeException("Role ADMIN not found"));
            roles.add(adminRole);
        } else {
            Role userRole = roleRepository.findByName("KUJDESTAR")
                    .orElseThrow(() -> new RuntimeException("Role KUJDESTAR not found"));
            roles.add(userRole);
        }

        user.setRoles(roles);
        userRepository.save(user);

        return "User u regjistrua me sukses!";
    }
}