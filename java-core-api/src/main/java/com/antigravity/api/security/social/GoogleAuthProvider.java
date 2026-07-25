package com.antigravity.api.security.social;

import com.antigravity.api.service.GoogleAuthService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GoogleAuthProvider implements SocialAuthProvider {

    private final GoogleAuthService googleAuthService;

    @Override
    public boolean supports(String platform) {
        return "GOOGLE".equalsIgnoreCase(platform);
    }

    @Override
    public SocialUserInfo verifyToken(String idToken) {
        try {
            GoogleIdToken.Payload payload = googleAuthService.verifyIdToken(idToken);
            return SocialUserInfo.builder()
                    .email(payload.getEmail())
                    .fullName((String) payload.get("name"))
                    .profilePictureUrl((String) payload.get("picture"))
                    .providerUid(payload.getSubject())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Google token verification failed: " + e.getMessage(), e);
        }
    }
}
