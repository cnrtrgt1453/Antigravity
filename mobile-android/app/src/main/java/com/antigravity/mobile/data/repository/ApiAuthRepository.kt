package com.antigravity.mobile.data.repository

import com.antigravity.mobile.domain.model.User
import com.antigravity.mobile.domain.repository.AuthRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
data class SocialLoginRequest(
    val idToken: String,
    val platform: String
)

class ApiAuthRepository @Inject constructor(
    private val client: HttpClient
) : AuthRepository {

    private val baseUrl = "http://10.0.2.2:8080/api/v1/users" // Emulator localhost for Java Core API

    override suspend fun loginWithGoogle(idToken: String): Result<User> {
        return try {
            val response = client.post("$baseUrl/login/social") {
                contentType(ContentType.Application.Json)
                setBody(SocialLoginRequest(idToken, "GOOGLE"))
            }.body<User>()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        // Implementation for clearing local tokens can be added here
        return Result.success(Unit)
    }

    override suspend fun getCurrentUser(): User? {
        // Future implementation with local storage
        return null
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            client.post("$baseUrl/me/delete")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
