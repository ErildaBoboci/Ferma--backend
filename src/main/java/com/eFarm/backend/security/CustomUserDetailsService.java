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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@Transactional
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User nuk u gjet me email: " + email));

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

            // Add role as authority
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName()));

            // Add specific permissions based on role
            switch (user.getRole().getName()) {
                case "ADMIN":
                    authorities.add(new SimpleGrantedAuthority("PERMISSION_MANAGE_USERS"));
                    authorities.add(new SimpleGrantedAuthority("PERMISSION_MANAGE_FARM"));
                    authorities.add(new SimpleGrantedAuthority("PERMISSION_MANAGE_ANIMALS"));
                    authorities.add(new SimpleGrantedAuthority("PERMISSION_VIEW_REPORTS"));
                    authorities.add(new SimpleGrantedAuthority("PERMISSION_MANAGE_SETTINGS"));
                    break;
                case "KUJDESTAR_KAFSHESH":
                    authorities.add(new SimpleGrantedAuthority("PERMISSION_MANAGE_ANIMALS"));
                    authorities.add(new SimpleGrantedAuthority("PERMISSION_VIEW_ANIMALS"));
                    authorities.add(new SimpleGrantedAuthority("PERMISSION_UPDATE_ANIMAL_STATUS"));
                    break;
                case "KUJDESTAR_FERME":
                    authorities.add(new SimpleGrantedAuthority("PERMISSION_MANAGE_FARM"));
                    authorities.add(new SimpleGrantedAuthority("PERMISSION_VIEW_FARM"));
                    authorities.add(new SimpleGrantedAuthority("PERMISSION_UPDATE_FARM_STATUS"));
                    break;
            }

            return authorities;
        }

        @Override
        public String getPassword() {
            return user.getPassword();
        }

        @Override
        public String getUsername() {
            return user.getEmail();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true; // Account never expires
        }

        @Override
        public boolean isAccountNonLocked() {
            return true; // Account is never locked (can be implemented later)
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true; // Credentials never expire
        }

        @Override
        public boolean isEnabled() {
            return user.isEnabled() && user.isEmailVerified();
        }

        // Additional methods to get user information
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

        public String getRoleName() {
            return user.getRole().getName();
        }

        public boolean hasRole(String roleName) {
            return user.getRole().getName().equals(roleName);
        }

        public boolean hasPermission(String permission) {
            return getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals(permission));
        }

        public boolean isAdmin() {
            return hasRole("ADMIN");
        }

        public boolean isKujdestarKafshesh() {
            return hasRole("KUJDESTAR_KAFSHESH");
        }

        public boolean isKujdestarFerme() {
            return hasRole("KUJDESTAR_FERME");
        }
    }
}