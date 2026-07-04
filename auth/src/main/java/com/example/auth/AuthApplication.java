package com.example.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class AuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
        
        // Сгенерировать хеш для admin123
        String hash = new BCryptPasswordEncoder().encode("admin123");
        System.out.println("HASH FOR admin123: " + hash);
    }
}