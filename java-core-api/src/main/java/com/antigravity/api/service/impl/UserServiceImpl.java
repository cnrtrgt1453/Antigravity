package com.antigravity.api.service.impl;

import com.antigravity.api.dto.GoogleLoginRequestDto;
import com.antigravity.api.dto.SocialLoginRequestDto;
import com.antigravity.api.entity.User;
import com.antigravity.api.repository.UserRepository;
import com.antigravity.api.service.UserService;
import com.google.firebase.auth.FirebaseToken;
import com.antigravity.api.service.FirebaseAuthService;
import com.antigravity.api.service.GoogleAuthService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
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
    private final FirebaseAuthService firebaseAuthService;
    private final GoogleAuthService googleAuthService;

    @Override
    @Transactional(readOnly = true)
    public User getUserByFirebaseUid(String firebaseUid) {
        return userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı"));
    }

    @Override
    public User loginWithGoogle(GoogleLoginRequestDto googleLoginRequestDto) {
        SocialLoginRequestDto socialDto = SocialLoginRequestDto.builder()
                .idToken(googleLoginRequestDto.getIdToken())
                .platform("GOOGLE")
                .build();
        return loginWithSocial(socialDto);
    }

    @Override
    @Transactional
    public User loginWithSocial(SocialLoginRequestDto socialLoginRequestDto) {
        try {
            String email;
            String fullName;
            String profilePictureUrl;
            String firebaseUid;

            if ("GOOGLE".equalsIgnoreCase(socialLoginRequestDto.getPlatform())) {
                // Google ID Token doğrulaması
                GoogleIdToken.Payload payload = googleAuthService.verifyIdToken(socialLoginRequestDto.getIdToken());
                email = payload.getEmail();
                fullName = (String) payload.get("name");
                profilePictureUrl = (String) payload.get("picture");
                firebaseUid = payload.getSubject(); // Google için sub alanı
            } else {
                // Firebase Token doğrulaması (Diğer platformlar için varsayılan)
                FirebaseToken decodedToken = firebaseAuthService.verifyIdToken(socialLoginRequestDto.getIdToken());
                email = decodedToken.getEmail();
                fullName = (String) decodedToken.getClaims().get("name");
                profilePictureUrl = decodedToken.getPicture();
                firebaseUid = decodedToken.getUid();
            }

            log.info("Sosyal login isteği ({}): {}", socialLoginRequestDto.getPlatform(), email);

            return userRepository.findByEmail(email)
                    .map(user -> {
                        user.setFirebaseUid(firebaseUid);
                        user.setLastLoginAt(java.time.LocalDateTime.now());
                        if (user.getFullName() == null || user.getFullName().isEmpty()) {
                            user.setFullName(fullName);
                        }
                        if (user.getProfilePictureUrl() == null || user.getProfilePictureUrl().isEmpty()) {
                            user.setProfilePictureUrl(profilePictureUrl);
                        }
                        return userRepository.save(user);
                    })
                    .orElseGet(() -> {
                        User newUser = User.builder()
                                .email(email)
                                .fullName(fullName != null ? fullName : email.split("@")[0])
                                .profilePictureUrl(profilePictureUrl)
                                .firebaseUid(firebaseUid)
                                .lastLoginAt(java.time.LocalDateTime.now())
                                .isActive(true)
                                .build();
                        return userRepository.save(newUser);
                    });
        } catch (Exception e) {
            log.error("Sosyal login hatası ({}): {}", socialLoginRequestDto.getPlatform(), e.getMessage());
            throw new RuntimeException(socialLoginRequestDto.getPlatform() + " giriş işlemi başarısız oldu: " + e.getMessage());
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

    @Override
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı: " + email));
    }
}
