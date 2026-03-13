package com.antigravity.api.service.impl;

import com.antigravity.api.dto.UserRegistrationDto;
import com.antigravity.api.entity.User;
import com.antigravity.api.repository.UserRepository;
import com.antigravity.api.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User registerUser(UserRegistrationDto registrationDto) {
        log.info("Yeni kullanıcı kaydı isteği: {}", registrationDto.getEmail());

        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new IllegalArgumentException("Bu e-posta adresi kullanımda.");
        }

        User newUser = User.builder()
                .email(registrationDto.getEmail())
                // Şifreyi açık metin olarak değil, BCrypt ile şifrelenmiş (Hashlenmiş) formatta kaydet (Güvenlik)
                .password(passwordEncoder.encode(registrationDto.getPassword()))
                .fullName(registrationDto.getFullName())
                .profilePictureUrl(registrationDto.getProfilePictureUrl())
                // Kayıt olan kullanıcının son girişi "şu an"dır
                .lastLoginAt(LocalDateTime.now())
                .build();

        return userRepository.save(newUser);
    }

    @Override
    @Transactional
    public User loginUser(String email, String password) {
        log.info("Kullanıcı giriş isteği: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("E-posta adresi sistemde bulunamadı."));
                
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Hatalı şifre girişi yapıldı.");
        }
        
        user.setLastLoginAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByFirebaseUid(String firebaseUid) {
        return userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı"));
    }

    @Override
    @Transactional
    public void updateLastLogin(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if(user != null) {
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
        }
    }
}
