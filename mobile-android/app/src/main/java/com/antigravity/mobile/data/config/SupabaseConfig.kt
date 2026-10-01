package com.antigravity.mobile.data.config

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.antigravity.mobile.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
internal data class SupabaseRefreshResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String? = null
)

@Serializable
internal data class SupabaseRefreshTokenRequest(
    @SerialName("refresh_token") val refreshToken: String
)

@Singleton
class SupabaseConfig @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        // BuildConfig (local.properties) üzerinden güvenli yüklenir, kodda gizli kalır
        var SUPABASE_URL: String = BuildConfig.SUPABASE_URL
        var SUPABASE_ANON_KEY: String = BuildConfig.SUPABASE_ANON_KEY
        var PYTHON_BASE_URL: String = BuildConfig.PYTHON_BASE_URL

        private const val PREFS_NAME = "financeup_supabase_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val jsonHelper = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun saveSession(accessToken: String, userId: String, email: String, name: String?, refreshToken: String? = null) {
        val editor = prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_USER_ID, userId)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_NAME, name ?: "")
        if (!refreshToken.isNullOrEmpty()) {
            editor.putString(KEY_REFRESH_TOKEN, refreshToken)
        }
        editor.apply()
    }

    fun updateAccessToken(accessToken: String, refreshToken: String? = null) {
        val editor = prefs.edit().putString(KEY_ACCESS_TOKEN, accessToken)
        if (!refreshToken.isNullOrEmpty()) {
            editor.putString(KEY_REFRESH_TOKEN, refreshToken)
        }
        editor.apply()
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun isTokenExpired(token: String?): Boolean {
        if (token.isNullOrEmpty()) return true
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return true
            val payloadBytes = android.util.Base64.decode(
                parts[1],
                android.util.Base64.URL_SAFE or android.util.Base64.NO_PADDING or android.util.Base64.NO_WRAP
            )
            val payload = String(payloadBytes, Charsets.UTF_8)
            val expRegex = """"exp"\s*:\s*(\d+)""".toRegex()
            val match = expRegex.find(payload)
            if (match != null) {
                val expSeconds = match.groupValues[1].toLongOrNull() ?: 0L
                val currentSeconds = System.currentTimeMillis() / 1000L
                currentSeconds >= expSeconds
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun isTokenExpiringSoon(token: String?): Boolean {
        if (token.isNullOrEmpty()) return true
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return true
            val payloadBytes = android.util.Base64.decode(
                parts[1],
                android.util.Base64.URL_SAFE or android.util.Base64.NO_PADDING or android.util.Base64.NO_WRAP
            )
            val payload = String(payloadBytes, Charsets.UTF_8)
            val expRegex = """"exp"\s*:\s*(\d+)""".toRegex()
            val match = expRegex.find(payload)
            if (match != null) {
                val expSeconds = match.groupValues[1].toLongOrNull() ?: 0L
                val currentSeconds = System.currentTimeMillis() / 1000L
                // 60 saniye pay bırakıyoruz
                currentSeconds + 60 >= expSeconds
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Token süresi dolduysa veya dolmak üzereyse refresh_token kullanarak otomatik yeniler.
     */
    suspend fun refreshTokenIfNeeded(client: HttpClient): Boolean {
        val token = getAccessToken()
        val refreshToken = getRefreshToken()

        if (!token.isNullOrEmpty() && !isTokenExpiringSoon(token)) {
            return true
        }

        if (refreshToken.isNullOrEmpty()) {
            Log.w("SupabaseConfig", "Refresh token bulunamadı, mevcut token durumu kontrol ediliyor.")
            return !isTokenExpired(token)
        }

        return try {
            val response = client.post("${SUPABASE_URL}/auth/v1/token?grant_type=refresh_token") {
                contentType(ContentType.Application.Json)
                header("apikey", SUPABASE_ANON_KEY)
                setBody(SupabaseRefreshTokenRequest(refreshToken = refreshToken))
            }

            if (response.status.value in 200..299) {
                val bodyText = response.bodyAsText()
                val authResp = jsonHelper.decodeFromString<SupabaseRefreshResponse>(bodyText)
                updateAccessToken(authResp.accessToken, authResp.refreshToken ?: refreshToken)
                Log.d("SupabaseConfig", "Supabase token başarıyla yenilendi.")
                true
            } else {
                Log.e("SupabaseConfig", "Supabase token yenileme hatası: ${response.status} - ${response.bodyAsText()}")
                !isTokenExpired(token)
            }
        } catch (e: Exception) {
            Log.e("SupabaseConfig", "Supabase token yenileme istisnası: ${e.message}", e)
            !isTokenExpired(token)
        }
    }

    fun isLoggedIn(): Boolean {
        val token = prefs.getString(KEY_ACCESS_TOKEN, null)
        val userId = prefs.getString(KEY_USER_ID, null)
        return !token.isNullOrEmpty() && !userId.isNullOrEmpty()
    }

    fun getAccessToken(): String? {
        val token = prefs.getString(KEY_ACCESS_TOKEN, null)
        return token
    }

    fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)
    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)
    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)

    fun getAuthHeader(): String {
        val token = getAccessToken()
        return if (!token.isNullOrEmpty()) "Bearer $token" else "Bearer $SUPABASE_ANON_KEY"
    }

    fun getLocalWatchlist(): Set<String> {
        val userKey = getUserId() ?: "default_user"
        return prefs.getStringSet("watchlist_$userKey", emptySet()) ?: emptySet()
    }

    fun saveLocalWatchlist(symbols: Set<String>) {
        val userKey = getUserId() ?: "default_user"
        prefs.edit().putStringSet("watchlist_$userKey", symbols).apply()
    }
}
