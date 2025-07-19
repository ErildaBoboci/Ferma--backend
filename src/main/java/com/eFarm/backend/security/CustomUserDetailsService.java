package com.eFarm.backend.security;

import com.eFarm.backend.entity.User;
import com.eFarm.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@Transactional
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        System.out.println("🔍 Duke kërkuar user: " + usernameOrEmail);

        // Provo së pari me username, pastaj me email
        User user = userRepository.findByUsername(usernameOrEmail)
                .orElseGet(() -> {
                    System.out.println("   Username nuk u gjet, duke provuar me email...");
                    return userRepository.findByEmail(usernameOrEmail)
                            .orElseThrow(() -> new UsernameNotFoundException("User nuk u gjet: " + usernameOrEmail));
                });

        System.out.println("✅ User u gjet: " + user.getEmail() + " (" + user.getUsername() + ")");
        return new CustomUserPrincipal(user);
    }

    public static class CustomUserPrincipal implements UserDetails {
        private final User user;

        public CustomUserPrincipal(User user) {
            this.user = user;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            List<GrantedAuthority> authorities = new ArrayList<>();

            // Shto të gjitha rolet si ROLE_ROLENAME
            if (user.getRoles() != null) {
                user.getRoles().forEach(role -> {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
                    System.out.println("🎭 Role added: ROLE_" + role.getName());
                });
            }

            return authorities;
        }

        @Override
        public String getPassword() {
            return user.getPassword();
        }

        @Override
        public String getUsername() {
            return user.getUsername();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            // Kontrollo nëse llogaria është e bllokuar
            if (user.getAccountLockedUntil() == null) {
                return true;
            }

            boolean isUnlocked = user.getAccountLockedUntil().isBefore(LocalDateTime.now());
            if (!isUnlocked) {
                System.out.println("⚠️ Account is locked until: " + user.getAccountLockedUntil());
            }
            return isUnlocked;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            // User duhet të jetë aktiv DHE email i verifikuar
            boolean isActive = user.isEnabled();
            boolean isEmailVerified = user.isEmailVerified();

            System.out.println("🔍 User enabled check:");
            System.out.println("   Is Active: " + isActive);
            System.out.println("   Email Verified: " + isEmailVerified);

            if (!isActive) {
                System.out.println("❌ Account is not active");
            }
            if (!isEmailVerified) {
                System.out.println("❌ Email is not verified");
            }

            return isActive && isEmailVerified;
        }

        // Metodat shtesë
        public User getUser() {
            return user;
        }

        public Long getId() {
            return user.getId();
        }

        public String getFirstName() {
            return user.getFirstName();
        }

        public String getLastName() {
            return user.getLastName();
        }

        public String getFullName() {
            return user.getFullName();
        }

        public String getEmail() {
            return user.getEmail();
        }
    }
}