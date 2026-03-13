package com.antigravity.api.security.model;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Spring Security'nin yetki mekanizması için özel Authentication modeli.
 * Doğrulanmış Firebase kullanıcısını (UID ve Email) Context içinde taşımamızı sağlar.
 */
public class FirebaseAuthenticationToken extends AbstractAuthenticationToken {

    private final String uid;
    private final String email;

    public FirebaseAuthenticationToken(String uid, String email, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.uid = uid;
        this.email = email;
        super.setAuthenticated(true); // Token filtreden (Verify) geçtiğinde bu sınıf oluşacağı için her zaman doğrulanmıştır.
    }

    @Override
    public Object getCredentials() {
        return null; // Kimlik bilgisi JWT ile sağlandığı için null dönebilir.
    }

    @Override
    public Object getPrincipal() {
        return uid; // "Principal" = Kimlik (Firebase UID)
    }

    public String getEmail() {
        return email;
    }
}
