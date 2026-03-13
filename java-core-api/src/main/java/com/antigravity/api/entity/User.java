package com.antigravity.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Kullanıcı varlık (Entity) sınıfı.
 * Veritabanında (PostgreSQL) "users" tablosuna karşılık gelir.
 * Sadece durum(state) barındırır, iş mantığı(logic) barındırmaz. (SRP - Single Responsibility Principle)
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Firebase Authentication Opsiyonel Kullanım İçin
    @Column(name = "firebase_uid", unique = true, length = 128)
    private String firebaseUid;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // Analiz için: Kayıt Tarihi
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Analiz için: Son Giriş Tarihi
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
}
