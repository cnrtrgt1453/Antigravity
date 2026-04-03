package com.antigravity.api.service;

import com.antigravity.api.dto.GoogleLoginRequestDto;
import com.antigravity.api.dto.SocialLoginRequestDto;
import com.antigravity.api.entity.User;

/**
 * Service Layer Interface.
 * İş mantığını (Business Logic) tanımlar.
 * Controller Katmanı, somut sınıfa (UserServiceImpl) değil, bu arayüze bağımlı olur. (Dependency Inversion)
 */
public interface UserService {
    
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

    /**
     * Kullanıcıyı ve ilişkili tüm verilerini siler.
     */
    void deleteUser(User user);

    /**
     * Kullanıcının Push Token'ını günceller.
     */
    void updatePushToken(User user, String pushToken);
}
