package com.antigravity.api.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Uygulama başladığında Firebase Admin SDK'sını hazır hale getiren konfigürasyon sınıfı.
 */
@Configuration
@Slf4j
public class FirebaseConfig {

    // İleride Service Account JSON dosyasının yolunu application.yml üzerinden okuyacağız.
    // Şimdilik test için placeholder bir default değer atanmıştır.
    @Value("${firebase.service-account-path:firebase-service-account.json}")
    private String firebaseConfigPath;

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                // Şimdilik sadece mock olarak yapılandırıyoruz ki uygulama ayağa kalkabilsin
                // GoogleCredentials.fromStream(new FileInputStream(firebaseConfigPath))
                log.info("Firebase Config path: {}", firebaseConfigPath);
                log.warn("Firebase Admin SDK Initialization is currently mocked! Service Account JSON required for real requests.");
                // Gerçek ortamda burası çalışacak
                /* 
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(new FileInputStream(firebaseConfigPath)))
                        .build();
                FirebaseApp.initializeApp(options);
                log.info("Firebase application has been initialized");
                */
            }
        } catch (Exception e) {
            log.error("Firebase başlatılamadı: {}", e.getMessage());
        }
    }
}
