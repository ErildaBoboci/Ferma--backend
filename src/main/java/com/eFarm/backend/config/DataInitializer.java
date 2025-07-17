package com.ferma.config;

import com.ferma.entity.Role;
import com.ferma.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        // Krijo rolet nëse nuk ekzistojnë
        if (roleRepository.findByName("ADMIN").isEmpty()) {
            Role adminRole = new Role("ADMIN");
            roleRepository.save(adminRole);
            System.out.println("Role ADMIN u krijua!");
        }

        if (roleRepository.findByName("KUJDESTAR").isEmpty()) {
            Role kujdestarRole = new Role("KUJDESTAR");
            roleRepository.save(kujdestarRole);
            System.out.println("Role KUJDESTAR u krijua!");
        }
    }
}
