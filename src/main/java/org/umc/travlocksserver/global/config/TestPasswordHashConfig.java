package org.umc.travlocksserver.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class TestPasswordHashConfig {

    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner makeTestHash() {
        return args -> {
            System.out.println("=== TEST PASSWORD HASH ===");
            System.out.println(passwordEncoder.encode("dummy"));
            System.out.println("==========================");
        };
    }
}

