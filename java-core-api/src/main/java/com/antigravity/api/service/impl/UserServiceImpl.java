package com.antigravity.api.service.impl;

import com.antigravity.api.dto.LoginRequestDto;
import com.antigravity.api.dto.UserRegistrationDto;
import com.antigravity.api.dto.GoogleLoginRequestDto;
import com.antigravity.api.entity.User;
import com.antigravity.api.repository.UserRepository;
import com.antigravity.api.service.UserService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
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
    public User loginUser(LoginRequestDto loginRequestDto) {
        log.info("Kullanıcı giriş isteği: {}", loginRequestDto.getEmail());
        User user = userRepository.findByEmail(loginRequestDto.getEmail())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));

        if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Hatalı şifre.");
        }

        updateLastLogin(user.getEmail());
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByFirebaseUid(String firebaseUid) {
        return userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı"));
    }

    @Override
    public User loginWithGoogle(GoogleLoginRequestDto googleLoginRequestDto) {
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(googleLoginRequestDto.getIdToken());
            String email = decodedToken.getEmail();
            String fullName = (String) decodedToken.getClaims().get("name");
            String profilePictureUrl = decodedToken.getPicture();
            String firebaseUid = decodedToken.getUid();

            return userRepository.findByEmail(email)
                    .map(user -> {
                        user.setFirebaseUid(firebaseUid);
                        user.setLastLoginAt(java.time.LocalDateTime.now());
                        return userRepository.save(user);
                    })
                    .orElseGet(() -> {
                        User newUser = User.builder()
                                .email(email)
                                .fullName(fullName != null ? fullName : email.split("@")[0])
                                .profilePictureUrl(profilePictureUrl)
                                .firebaseUid(firebaseUid)
                                .lastLoginAt(java.time.LocalDateTime.now())
                                .build();
                        return userRepository.save(newUser);
                    });
        } catch (FirebaseAuthException e) {
            throw new RuntimeException("Firebase token doğrulama hatası: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Google login işlemi sırasında hata oluştu: " + e.getMessage());
        }
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
