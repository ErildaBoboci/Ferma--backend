package com.eFarm.backend.service;

import com.eFarm.backend.entity.Role;
import com.eFarm.backend.entity.User;
import com.eFarm.backend.repository.RoleRepository;
import com.eFarm.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class DataInitializationService implements ApplicationRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🚀 INICIALIZIMI I TË DHËNAVE TË APLIKACIONIT");
        System.out.println("=".repeat(60));

        initializeRoles();
        createDefaultUsers();
        printSystemInfo();

        System.out.println("✅ Inicializimi i kompletuar me sukses!");
        System.out.println("=".repeat(60) + "\n");
    }

    /**
     * Inicializo rolet
     */
    private void initializeRoles() {
        System.out.println("🏷️ Duke inicializuar rolet...");

        if (roleRepository.findByName("ADMIN").isEmpty()) {
            Role adminRole = new Role("ADMIN");
            roleRepository.save(adminRole);
            System.out.println("   ✅ Role ADMIN u krijua!");
        } else {
            System.out.println("   ℹ️ Role ADMIN tashmë ekziston");
        }

        if (roleRepository.findByName("KUJDESTAR").isEmpty()) {
            Role kujdestarRole = new Role("KUJDESTAR");
            roleRepository.save(kujdestarRole);
            System.out.println("   ✅ Role KUJDESTAR u krijua!");
        } else {
            System.out.println("   ℹ️ Role KUJDESTAR tashmë ekziston");
        }
    }

    /**
     * Krijo përdorues default
     */
    private void createDefaultUsers() {
        System.out.println("👤 Duke krijuar përdorues default...");

        createDefaultAdmin();
        createTestUsers();
    }

    /**
     * Krijo admin default
     */
    private void createDefaultAdmin() {
        if (!userRepository.existsByEmail("admin@ferma.al")) {
            User admin = new User("admin", "admin@ferma.al",
                    passwordEncoder.encode("admin123"), "Admin", "Ferma");

            admin.setEmailVerified(true);
            admin.setIsActive(true);

            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseThrow(() -> new RuntimeException("Role ADMIN not found"));

            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            admin.setRoles(roles);

            userRepository.save(admin);
            System.out.println("   ✅ Default admin u krijua: admin@ferma.al / admin123");
        } else {
            System.out.println("   ℹ️ Default admin tashmë ekziston");
        }
    }

    /**
     * Krijo përdorues për test
     */
    private void createTestUsers() {
        // Test Kujdestar
        if (!userRepository.existsByEmail("kujdestar@test.al")) {
            User kujdestar = new User("kujdestar", "kujdestar@test.al",
                    passwordEncoder.encode("test123"), "Kujdestar", "Test");

            kujdestar.setEmailVerified(true);
            kujdestar.setIsActive(true);

            Role kujdestarRole = roleRepository.findByName("KUJDESTAR")
                    .orElseThrow(() -> new RuntimeException("Role KUJDESTAR not found"));

            Set<Role> roles = new HashSet<>();
            roles.add(kujdestarRole);
            kujdestar.setRoles(roles);

            userRepository.save(kujdestar);
            System.out.println("   ✅ Test kujdestar u krijua: kujdestar@test.al / test123");
        } else {
            System.out.println("   ℹ️ Test kujdestar tashmë ekziston");
        }
    }

    /**
     * Printo informacionet e sistemit
     */
    private void printSystemInfo() {
        System.out.println("\n📊 INFORMACIONE TË SISTEMIT:");

        long totalUsers = userRepository.count();
        long totalRoles = roleRepository.count();

        System.out.println("   👥 Total përdorues: " + totalUsers);
        System.out.println("   🏷️ Total role: " + totalRoles);

        System.out.println("\n🔗 ENDPOINT-ET E DISPONUESHËM:");
        System.out.println("   📋 Status: http://localhost:8080/api/auth/status");
        System.out.println("   🧪 Test: http://localhost:8080/api/auth/test");
        System.out.println("   💚 Health: http://localhost:8080/api/auth/health");
        System.out.println("   🔐 Login: POST http://localhost:8080/api/auth/login");
        System.out.println("   📝 Register: POST http://localhost:8080/api/auth/register");
        System.out.println("   ✉️ Verify: POST http://localhost:8080/api/auth/verify-email");

        System.out.println("\n🔑 KREDENCIALET DEFAULT:");
        System.out.println("   👑 Admin: admin@ferma.al / admin123");
        System.out.println("   👤 Kujdestar: kujdestar@test.al / test123");

        System.out.println("\n⚙️ KONFIGURIMI:");
        System.out.println("   📧 Email Verification: Console (4 digit code, 15 min expiry)");
        System.out.println("   🛡️ JWT Expiry: 24 orë");
        System.out.println("   🔄 Rate Limiting: 5 përpjekje/orë për email verification");
    }
}