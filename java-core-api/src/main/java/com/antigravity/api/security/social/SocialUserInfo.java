package com.antigravity.api.security.social;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SocialUserInfo {
    private String email;
    private String fullName;
    private String profilePictureUrl;
    private String providerUid;
}
