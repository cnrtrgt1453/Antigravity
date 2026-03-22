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
        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken != null) {
            return idToken.getPayload();
        } else {
            throw new IllegalArgumentException("Geçersiz Google ID Token");
        }
    }
}
