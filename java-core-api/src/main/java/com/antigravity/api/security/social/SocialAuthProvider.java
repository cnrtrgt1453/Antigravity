package com.antigravity.api.security.social;

public interface SocialAuthProvider {
    boolean supports(String platform);
    SocialUserInfo verifyToken(String idToken);
}
