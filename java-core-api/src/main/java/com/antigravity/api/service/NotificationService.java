package com.antigravity.api.service;

import com.antigravity.api.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final RestTemplate restTemplate;
    private static final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";

    /**
     * Belirli bir kullanıcıya bildirim gönderir.
     */
    public void sendPushNotification(User user, String title, String body) {
        if (user.getPushToken() == null || user.getPushToken().isEmpty()) {
            log.warn("Kullanıcının Push Token'ı bulunamadı, bildirim gönderilemedi: {}", user.getEmail());
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> payload = new HashMap<>();
            payload.put("to", user.getPushToken());
            payload.put("title", title);
            payload.put("body", body);
            payload.put("sound", "default");
            payload.put("data", new HashMap<String, String>()); // Ek veri gönderilebilir

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            restTemplate.postForObject(EXPO_PUSH_URL, request, String.class);
            log.info("Bildirim başarıyla gönderildi: {} -> {}", user.getEmail(), title);
        } catch (Exception e) {
            log.error("Bildirim gönderilirken hata oluştu: {}", e.getMessage());
        }
    }

    /**
     * Belirli bir cihaz token'ına doğrudan bildirim gönderir (Test için).
     */
    public void sendToToken(String token, String title, String body) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> payload = new HashMap<>();
            payload.put("to", token);
            payload.put("title", title);
            payload.put("body", body);
            payload.put("sound", "default");

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            restTemplate.postForObject(EXPO_PUSH_URL, request, String.class);
            log.info("Test bildirimi token'a gönderildi: {}", token);
        } catch (Exception e) {
            log.error("Test bildirimi hatası: {}", e.getMessage());
        }
    }
}
