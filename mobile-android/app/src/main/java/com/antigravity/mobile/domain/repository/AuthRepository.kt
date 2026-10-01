package com.antigravity.mobile.domain.repository

import com.antigravity.mobile.domain.model.User

interface AuthRepository {
    fun getOAuthLoginUrl(): String
    suspend fun handleOAuthCallback(url: String): Result<User>
    suspend fun loginWithAccessToken(accessToken: String): Result<User>
    suspend fun loginWithGoogle(idToken: String): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun getCurrentUser(): User?
    suspend fun deleteAccount(): Result<Unit>
}
