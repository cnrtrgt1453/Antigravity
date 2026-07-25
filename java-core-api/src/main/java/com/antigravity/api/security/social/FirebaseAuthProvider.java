package com.antigravity.api.security.social;

import com.antigravity.api.service.FirebaseAuthService;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FirebaseAuthProvider implements SocialAuthProvider {

    private final FirebaseAuthService firebaseAuthService;

    @Override
    public boolean supports(String platform) {
        return platform == null || platform.isEmpty() || "FIREBASE".equalsIgnoreCase(platform);
    }

    @Override
    public SocialUserInfo verifyToken(String idToken) {
        try {
            FirebaseToken decodedToken = firebaseAuthService.verifyIdToken(idToken);
            String name = decodedToken.getClaims() != null ? (String) decodedToken.getClaims().get("name") : null;
            return SocialUserInfo.builder()
                    .email(decodedToken.getEmail())
                    .fullName(name)
                    .profilePictureUrl(decodedToken.getPicture())
                    .providerUid(decodedToken.getUid())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Firebase token verification failed: " + e.getMessage(), e);
        }
    }
}
