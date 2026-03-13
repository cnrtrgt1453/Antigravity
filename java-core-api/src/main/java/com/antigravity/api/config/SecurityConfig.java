package com.antigravity.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // CSRF'yi kapatıyoruz ve CORS/Authorization ayarlarına standart izin veriyoruz.
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/users/register", "/api/v1/users/login").permitAll()
                .anyRequest().permitAll() // Şimdilik geliştirme ortamı için hepsi açık
            );
            
        return http.build();
    }
}
