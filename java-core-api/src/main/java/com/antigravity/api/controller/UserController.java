package com.antigravity.api.controller;

import com.antigravity.api.dto.UserRegistrationDto;
import com.antigravity.api.entity.User;
import com.antigravity.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Presentation Layer (REST Controller).
 * Sadece HTTP isteklerini (Request/Response) karşılar.
 * Asla kendi içinde veritabanı veya iş mantığı (Transaction/Logic) yürütmez. (SRP)
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    // Bağımlılık (Dependency) Controller'a arayüz (Interface) üzerinden enjekte ediliyor. (DIP)
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
        User createdUser = userService.registerUser(registrationDto);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @GetMapping("/{firebaseUid}")
    public ResponseEntity<User> getUserByFirebaseUid(@PathVariable String firebaseUid) {
        User user = userService.getUserByFirebaseUid(firebaseUid);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/{firebaseUid}/login")
    public ResponseEntity<Void> logUserLogin(@PathVariable String firebaseUid) {
        userService.updateLastLogin(firebaseUid);
        return ResponseEntity.ok().build();
    }
}
