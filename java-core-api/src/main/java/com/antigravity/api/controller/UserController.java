package com.antigravity.api.controller;

import com.antigravity.api.dto.LoginRequestDto;
import com.antigravity.api.dto.UserRegistrationDto;
import com.antigravity.api.dto.GoogleLoginRequestDto;
import com.antigravity.api.dto.SocialLoginRequestDto;
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

    @PostMapping("/login")
    public ResponseEntity<User> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        User user = userService.loginUser(loginRequestDto);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/login/google")
    public ResponseEntity<User> loginWithGoogle(@Valid @RequestBody GoogleLoginRequestDto googleLoginRequestDto) {
        User user = userService.loginWithGoogle(googleLoginRequestDto);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/login/social")
    public ResponseEntity<User> loginWithSocial(@Valid @RequestBody SocialLoginRequestDto socialLoginRequestDto) {
        User user = userService.loginWithSocial(socialLoginRequestDto);
        return ResponseEntity.ok(user);
    }
}
