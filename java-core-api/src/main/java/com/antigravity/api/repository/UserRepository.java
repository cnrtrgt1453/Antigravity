package com.antigravity.api.repository;

import com.antigravity.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Data Access Layer (Veri Erişim Katmanı).
 * Spring Data JPA, bu arayüz için çalışma zamanında (runtime) bir implementasyon üretir.
 * Doğrudan Entity döndürür, iş mantığı bilmez.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByFirebaseUid(String firebaseUid);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByFirebaseUid(String firebaseUid);
    
    boolean existsByEmail(String email);
}
