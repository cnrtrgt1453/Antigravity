package com.antigravity.api.service;

import com.antigravity.api.dto.UserRegistrationDto;
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
    User loginUser(String email, String password);

    /**
     * Firebase UID ile kullanıcı bulunur (Gelecek destek).
     */
    User getUserByFirebaseUid(String firebaseUid);

    /**
     * Kullanıcının platforma girme tarihini günceller.
     */
    void updateLastLogin(String email);
}
