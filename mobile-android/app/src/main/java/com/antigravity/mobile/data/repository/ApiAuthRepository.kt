package com.antigravity.mobile.data.repository

import com.antigravity.mobile.data.config.SupabaseConfig
import com.antigravity.mobile.domain.model.User
import com.antigravity.mobile.domain.repository.AuthRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
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
    @SerialName("refresh_token") val refreshToken: String? = null,
    val user: SupabaseUserDto
)

class ApiAuthRepository @Inject constructor(
    private val client: HttpClient,
    private val config: SupabaseConfig
) : AuthRepository {

    private val authUrl: String
        get() = "${SupabaseConfig.SUPABASE_URL}/auth/v1"

    override fun getOAuthLoginUrl(): String {
        return "$authUrl/authorize?provider=google&redirect_to=financeup://auth-callback"
    }

    override suspend fun handleOAuthCallback(url: String): Result<User> {
        return try {
            val uri = android.net.Uri.parse(url)

            // 1. Fragment veya query parametresindeki hata mesajını kontrol et
            var errorMsg: String? = null
            val fragment = uri.fragment
            if (!fragment.isNullOrEmpty()) {
                val params = fragment.split("&").associate {
                    val parts = it.split("=", limit = 2)
                    if (parts.size == 2) parts[0] to parts[1] else parts[0] to ""
                }
                errorMsg = params["error_description"] ?: params["error"]
            }
            if (errorMsg.isNullOrEmpty()) {
                errorMsg = uri.getQueryParameter("error_description") ?: uri.getQueryParameter("error")
            }

            if (!errorMsg.isNullOrEmpty()) {
                val decoded = java.net.URLDecoder.decode(errorMsg, "UTF-8")
                return Result.failure(Exception("Giriş hatası: $decoded"))
            }

            // 2. access_token ve refresh_token ayıkla
            var accessToken: String? = null
            var refreshToken: String? = null
            if (!fragment.isNullOrEmpty()) {
                val params = fragment.split("&").associate {
                    val parts = it.split("=", limit = 2)
                    if (parts.size == 2) parts[0] to parts[1] else parts[0] to ""
                }
                accessToken = params["access_token"]
                refreshToken = params["refresh_token"]
            }
            if (accessToken.isNullOrEmpty()) {
                accessToken = uri.getQueryParameter("access_token")
                refreshToken = uri.getQueryParameter("refresh_token")
            }

            if (accessToken.isNullOrEmpty()) {
                return Result.failure(Exception("Yetkilendirme anahtarı (access_token) bulunamadı."))
            }

            loginWithAccessTokenInternal(accessToken, refreshToken)
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "OAuth callback parse error: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun loginWithAccessToken(accessToken: String): Result<User> {
        return loginWithAccessTokenInternal(accessToken, null)
    }

    suspend fun loginWithAccessTokenInternal(accessToken: String, refreshToken: String?): Result<User> {
        return try {
            val httpResponse = client.get("$authUrl/user") {
                header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                header("Authorization", "Bearer $accessToken")
            }

            if (httpResponse.status.value !in 200..299) {
                val errorText = httpResponse.bodyAsText()
                android.util.Log.e("AuthRepository", "Supabase user error: ${httpResponse.status.value} - $errorText")
                return Result.failure(Exception("Kullanıcı bilgisi alınamadı (${httpResponse.status.value}): $errorText"))
            }

            val userDto = httpResponse.body<SupabaseUserDto>()
            val user = User(
                id = userDto.id,
                email = userDto.email ?: "",
                fullName = userDto.userMetadata?.fullName ?: userDto.email?.substringBefore("@"),
                profilePictureUrl = userDto.userMetadata?.avatarUrl
            )

            // Oturumu yerel hafızaya kaydet
            config.saveSession(
                accessToken = accessToken,
                userId = user.id,
                email = user.email,
                name = user.fullName,
                refreshToken = refreshToken
            )

            Result.success(user)
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "loginWithAccessToken exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun loginWithGoogle(idToken: String): Result<User> {
        return try {
            val httpResponse = client.post("$authUrl/token?grant_type=id_token") {
                contentType(ContentType.Application.Json)
                header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                setBody(SupabaseIdTokenRequest(provider = "google", idToken = idToken))
            }

            if (httpResponse.status.value !in 200..299) {
                val errorText = httpResponse.bodyAsText()
                android.util.Log.e("AuthRepository", "Supabase token error: ${httpResponse.status.value} - $errorText")
                return Result.failure(Exception("Supabase girişi reddetti (${httpResponse.status.value}): $errorText"))
            }

            val response = httpResponse.body<SupabaseAuthResponse>()

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
                name = user.fullName,
                refreshToken = response.refreshToken
            )

            Result.success(user)
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "loginWithGoogle exception: ${e.message}", e)
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
