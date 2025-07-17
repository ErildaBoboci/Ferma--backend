package com.eFarm.backend;

import com.eFarm.backend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class EFarmApplication implements CommandLineRunner {

	@Autowired
	private AuthService authService;

	public static void main(String[] args) {
		SpringApplication.run(EFarmApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// Initialize default roles on application startup
		authService.initializeRoles();

		System.out.println("=================================================");
		System.out.println("         eFarm Backend Successfully Started     ");
		System.out.println("=================================================");
		System.out.println("Server is running on: http://localhost:8080");
		System.out.println("Available endpoints:");
		System.out.println("  - POST /api/auth/register");
		System.out.println("  - POST /api/auth/login");
		System.out.println("  - GET  /api/auth/verify-email");
		System.out.println("  - GET  /api/auth/roles");
		System.out.println("  - GET  /api/users/profile");
		System.out.println("=================================================");
	}
}