package com.antigravity.mobile.data.repository

import com.antigravity.mobile.data.config.SupabaseConfig
import com.antigravity.mobile.domain.model.User
import com.antigravity.mobile.domain.repository.AuthRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
internal data class SupabaseIdTokenRequest(
    val provider: String = "google",
    @SerialName("id_token") val idToken: String
)

@Serializable
internal data class SupabaseUserMetadata(
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null
)

@Serializable
internal data class SupabaseUserDto(
    val id: String,
    val email: String? = "",
    @SerialName("user_metadata") val userMetadata: SupabaseUserMetadata? = null
)

@Serializable
internal data class SupabaseAuthResponse(
    @SerialName("access_token") val accessToken: String,
    val user: SupabaseUserDto
)

class ApiAuthRepository @Inject constructor(
    private val client: HttpClient,
    private val config: SupabaseConfig
) : AuthRepository {

    private val authUrl: String
        get() = "${SupabaseConfig.SUPABASE_URL}/auth/v1"

    override suspend fun loginWithGoogle(idToken: String): Result<User> {
        return try {
            val response = client.post("$authUrl/token?grant_type=id_token") {
                contentType(ContentType.Application.Json)
                header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                setBody(SupabaseIdTokenRequest(provider = "google", idToken = idToken))
            }.body<SupabaseAuthResponse>()

            val user = User(
                id = response.user.id,
                email = response.user.email ?: "",
                fullName = response.user.userMetadata?.fullName ?: response.user.email?.substringBefore("@"),
                profilePictureUrl = response.user.userMetadata?.avatarUrl
            )

            // Oturumu yerel hafızaya kaydet
            config.saveSession(
                accessToken = response.accessToken,
                userId = user.id,
                email = user.email,
                name = user.fullName
            )

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        config.clearSession()
        return Result.success(Unit)
    }

    override suspend fun getCurrentUser(): User? {
        val userId = config.getUserId() ?: return null
        return User(
            id = userId,
            email = config.getUserEmail() ?: "",
            fullName = config.getUserName(),
            profilePictureUrl = null
        )
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            // Kullanıcı profilini sil (CASCADE ile tüm ilişkili veriler temizlenir)
            val userId = config.getUserId()
            if (userId != null) {
                client.post("${SupabaseConfig.SUPABASE_URL}/rest/v1/rpc/delete_user") {
                    header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                    header("Authorization", config.getAuthHeader())
                }
            }
            config.clearSession()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
