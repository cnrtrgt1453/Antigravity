package com.antigravity.api.config;

import com.antigravity.api.security.filter.FirebaseTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security ana konfigürasyon sınıfı.
 * Hangi API uç noktalarının (endpoints) herkese açık (public), hangilerinin token gerektirdiğini tanımlar.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final FirebaseTokenFilter firebaseTokenFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // REST API yaptığımız için CSRF korumasına ihtiyacımız yok, JWT kullanıyoruz.
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Durumsuz (Stateless) oturum: Sunucu tarafında HttpSession (çerez) tutulmayacak. Her istek Header'da Token göndermeli.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                // Route (Yönlendirme) Yetkileri
                .authorizeHttpRequests(auth -> auth
                        // Kayıt olma (Register) ve Swagger dokümanı gibi yerlere herkes (Token'sız) erişebilir.
                        .requestMatchers("/api/v1/users/register").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        
                        // Bunlar dışındaki tüm API uç noktaları için mutlaka DOĞRULANMIŞ (Authenticated) bir kullanıcı gerekir.
                        .anyRequest().authenticated()
                )
                // Yazdığımız FirebaseTokenFilter'ı, Spring'in standart UsernamePassword filtrenin ÖNCESİNE yerleştiriyoruz.
                .addFilterBefore(firebaseTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Cross-Origin Resource Sharing (CORS) ayarları.
     * Mobil (React Native) veya Web (React/Next) arayüzlerinden API'ye istek gelirken tarayıcı engeline takılmamak için çalışır.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Gerçekte `http://localhost:3000` gibi sınırlandırmak güvenlik açısından iyidir.
        configuration.setAllowedOriginPatterns(List.of("*")); 
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "x-auth-token"));
        configuration.setExposedHeaders(List.of("x-auth-token"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
