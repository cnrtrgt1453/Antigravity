package com.antigravity.mobile.domain.repository

import com.antigravity.mobile.domain.model.User

interface AuthRepository {
    suspend fun loginWithGoogle(idToken: String): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun getCurrentUser(): User?
    suspend fun deleteAccount(): Result<Unit>
}
