package com.antigravity.api.service;

import com.antigravity.api.dto.LoginRequestDto;
import com.antigravity.api.dto.UserRegistrationDto;
import com.antigravity.api.dto.SocialLoginRequestDto;
import com.antigravity.api.dto.GoogleLoginRequestDto;
import com.antigravity.api.entity.User;

/**
 * Service Layer Interface.
 * İş mantığını (Business Logic) tanımlar.
 * Controller Katmanı, somut sınıfa (UserServiceImpl) değil, bu arayüze bağımlı olur. (Dependency Inversion)
 */
public interface UserService {
    
    /**
     * Yeni kullanıcı kaydı oluşturur.
     */
    User registerUser(UserRegistrationDto registrationDto);

    /**
     * Kullanıcı girişi ve şifre doğrulaması yapar.
     */
    User loginUser(LoginRequestDto loginRequestDto);

    /**
     * Firebase UID ile kullanıcı bulunur (Gelecek destek).
     */
    User getUserByFirebaseUid(String firebaseUid);

    /**
     * Sosyal ağlar (Google/Facebook) ile giriş yapar ve gerekirse otomatik kayıt oluşturur.
     */
    User loginWithSocial(SocialLoginRequestDto socialLoginRequestDto);

    User loginWithGoogle(GoogleLoginRequestDto googleLoginRequestDto);

    void updateLastLogin(String email);

    /**
     * E-posta adresi ile kullanıcıyı getirir.
     */
    User getUserByEmail(String email);
}
