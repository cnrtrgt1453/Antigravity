package com.antigravity.api.service.impl;

import com.antigravity.api.service.GoogleAuthService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class GoogleAuthServiceImpl implements GoogleAuthService {

    private static final String WEB_CLIENT_ID = "777162969154-ha4tnq6c6bu0b4ijcpb01ae8m3d9gpc9.apps.googleusercontent.com";

    private final GoogleIdTokenVerifier verifier;

    public GoogleAuthServiceImpl() {
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(WEB_CLIENT_ID))
                .build();
    }

    @Override
    public GoogleIdToken.Payload verifyIdToken(String idTokenString) throws GeneralSecurityException, IOException {
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                return idToken.getPayload();
            } else {
                // Hata detayını anlamak için token'ı doğrulamadan parse edip loglayalım
                GoogleIdToken parsedToken = GoogleIdToken.parse(new GsonFactory(), idTokenString);
                GoogleIdToken.Payload payload = parsedToken.getPayload();
                System.out.println("--- Google ID Token Doğrulama Hatası ---");
                System.out.println("Audience: " + payload.getAudience());
                System.out.println("Expected Audience: " + WEB_CLIENT_ID);
                System.out.println("Issuer: " + payload.getIssuer());
                System.out.println("Subject: " + payload.getSubject());
                System.out.println("Email: " + payload.getEmail());
                System.out.println("---------------------------------------");
                
                throw new IllegalArgumentException("Geçersiz Google ID Token: Doğrulama başarısız (Audience veya Issuer uyumsuzluğu olabilir)");
            }
        } catch (Exception e) {
            System.err.println("Token doğrulama sırasında teknik hata: " + e.getMessage());
            throw e;
        }
    }
}
