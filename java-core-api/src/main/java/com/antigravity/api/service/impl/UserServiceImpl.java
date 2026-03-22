package com.antigravity.api.service.impl;

import com.antigravity.api.dto.LoginRequestDto;
import com.antigravity.api.dto.UserRegistrationDto;
import com.antigravity.api.dto.GoogleLoginRequestDto;
import com.antigravity.api.dto.SocialLoginRequestDto;
import com.antigravity.api.entity.User;
import com.antigravity.api.repository.UserRepository;
import com.antigravity.api.service.UserService;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.FirebaseAuthException;
import com.antigravity.api.service.FirebaseAuthService;
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
    private final FirebaseAuthService firebaseAuthService;

    @Override
    @Transactional
    public User registerUser(UserRegistrationDto registrationDto) {
        String email = registrationDto.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Bu e-posta adresi kullanımda.");
        }

        User newUser = User.builder()
                .email(email)
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
        String email = loginRequestDto.getEmail().trim().toLowerCase();
        log.info("Kullanıcı giriş isteği: {}", email);
        User user = userRepository.findByEmail(email)
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
            FirebaseToken decodedToken = firebaseAuthService.verifyIdToken(socialLoginRequestDto.getIdToken());
            String email = decodedToken.getEmail();
            String fullName = (String) decodedToken.getClaims().get("name");
            String profilePictureUrl = decodedToken.getPicture();
            String firebaseUid = decodedToken.getUid();

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
        } catch (FirebaseAuthException e) {
            log.error("Firebase token doğrulama hatası: {}", e.getMessage());
            throw new RuntimeException("Firebase token doğrulama hatası: " + e.getMessage());
        } catch (Exception e) {
            log.error("Sosyal login hatası: {}", e.getMessage());
            throw new RuntimeException("Sosyal login işlemi sırasında hata oluştu: " + e.getMessage());
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
