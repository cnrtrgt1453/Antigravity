package com.antigravity.api.security.filter;

import com.antigravity.api.security.model.FirebaseAuthenticationToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Her gelen HTTP isteğini araya girip süzecek (Intercept) olan Firebase
 * Filtresi.
 * (Gerçek ortamda FirebaseAuth.getInstance().verifyIdToken() ile doğrulama
 * yapacaktır)
 */
@Component
@Slf4j
public class FirebaseTokenFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String bearerToken = extractBearerToken(request);

        if (bearerToken != null) {
            try {
                // TODO: Uygulama canlıya alınırken burası açılacak (Firebase Admin SDK json
                // eklendikten sonra)
                // FirebaseToken decodedToken =
                // FirebaseAuth.getInstance().verifyIdToken(bearerToken);
                // String uid = decodedToken.getUid();
                // String email = decodedToken.getEmail();

                // MOCK Doğrulama: Şu an veritabanını denerken hızlıca Postman ile bağlanmak
                // için bypass edildi:
                String uid = "test_uid_" + bearerToken;
                String email = "test@user.com";

                FirebaseAuthenticationToken authentication = new FirebaseAuthenticationToken(uid, email,
                        Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Kullanıcı doğrulandı (Mocked): {}", uid);

            } catch (Exception e) {
                log.error("Token doğrulama hatası (Unauthorized): {}", e.getMessage());
                // Güvenlik: Geçersiz token durumunda Context'i temizleyin
                SecurityContextHolder.clearContext();
            }
        }

        // Filtre zincirini (Security Chain) devam ettirir (İstek Controller'a ulaşır)
        filterChain.doFilter(request, response);
    }

    /**
     * Gelen isteğin (Request) Header'ından "Authorization: Bearer <token>" yapısını
     * çıkarır.
     */
    private String extractBearerToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " stringini at, sadece Hash'i al
        }
        return null;
    }
}
