package com.antigravity.mobile.data.config

import android.content.Context
import android.content.SharedPreferences
import com.antigravity.mobile.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

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
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveSession(accessToken: String, userId: String, email: String, name: String?) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_USER_ID, userId)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_NAME, name ?: "")
            .apply()
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)
    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)
    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)
    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)

    fun getAuthHeader(): String {
        val token = getAccessToken()
        return if (!token.isNullOrEmpty()) "Bearer $token" else "Bearer $SUPABASE_ANON_KEY"
    }
}
