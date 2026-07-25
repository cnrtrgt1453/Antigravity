package com.antigravity.api.security.social;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SocialAuthProviderFactory {

    private final List<SocialAuthProvider> providers;

    public SocialAuthProvider getProvider(String platform) {
        return providers.stream()
                .filter(p -> p.supports(platform))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Desteklenmeyen giriş platformu: " + platform));
    }
}
