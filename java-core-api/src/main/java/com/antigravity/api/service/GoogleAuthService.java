package com.antigravity.api.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

import java.io.IOException;
import java.security.GeneralSecurityException;

public interface GoogleAuthService {
    GoogleIdToken.Payload verifyIdToken(String idToken) throws GeneralSecurityException, IOException;
}
