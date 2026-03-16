package com.antigravity.api.service;

import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.FirebaseAuthException;

public interface FirebaseAuthService {
    FirebaseToken verifyIdToken(String idToken) throws FirebaseAuthException;
}
