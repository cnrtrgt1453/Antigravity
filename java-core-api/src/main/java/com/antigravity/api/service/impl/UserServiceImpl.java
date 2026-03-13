package com.antigravity.api.service.impl;

import com.antigravity.api.dto.UserRegistrationDto;
import com.antigravity.api.entity.User;
import com.antigravity.api.repository.UserRepository;
import com.antigravity.api.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service Layer Implementation.
 * İş mantığının (Business Logic) barındığı yer.
 * Diğer katmanlarla iletişimi "Constructor Injection" yöntemi ile yapar.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public User registerUser(UserRegistrationDto registrationDto) {
        log.info("Yeni kullanıcı kaydı isteği: {}", registrationDto.getEmail());

        if (userRepository.existsByFirebaseUid(registrationDto.getFirebaseUid())) {
            throw new IllegalArgumentException("Bu kullanıcı zaten kayıtlı.");
        }

        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new IllegalArgumentException("Bu e-posta adresi kullanımda.");
        }

        User newUser = User.builder()
                .firebaseUid(registrationDto.getFirebaseUid())
                .email(registrationDto.getEmail())
                .fullName(registrationDto.getFullName())
                .profilePictureUrl(registrationDto.getProfilePictureUrl())
                // Kayıt olan kullanıcının son girişi "şu an"dır
                .lastLoginAt(LocalDateTime.now())
                .build();

        return userRepository.save(newUser);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByFirebaseUid(String firebaseUid) {
        return userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı"));
    }

    @Override
    @Transactional
    public void updateLastLogin(String firebaseUid) {
        User user = getUserByFirebaseUid(firebaseUid);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }
}
