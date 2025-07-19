package com.eFarm.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EFarmApplication {

	public static void main(String[] args) {
		System.out.println("\n" + "=".repeat(60));
		System.out.println("🚀 DUKE NISUR EFARM BACKEND APPLICATION");
		System.out.println("=".repeat(60));

		SpringApplication.run(EFarmApplication.class, args);

		System.out.println("\n" + "🎉".repeat(20));
		System.out.println("✅ EFARM BACKEND APLIKACIONI ËSHTË GATI!");
		System.out.println("🌐 Frontend URL: http://localhost:3000");
		System.out.println("🔧 Backend URL: http://localhost:8080");
		System.out.println("📚 API Docs: http://localhost:8080/api/auth/");
		System.out.println("🎉".repeat(20) + "\n");
	}
}