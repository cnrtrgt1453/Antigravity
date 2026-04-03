package com.antigravity.api.controller;

import com.antigravity.api.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Belirli bir token'a test bildirimi gönderir.
     */
    @PostMapping("/send-test")
    public ResponseEntity<Void> sendTest(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String title = request.getOrDefault("title", "Test Bildirimi");
        String body = request.getOrDefault("body", "Bu bir deneme mesajıdır.");
        
        notificationService.sendToToken(token, title, body);
        return ResponseEntity.ok().build();
    }
}
