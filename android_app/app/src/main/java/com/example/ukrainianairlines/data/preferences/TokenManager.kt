package com.example.ukrainianairlines.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.ukrainianairlines.data.model.AuthTokens
import com.google.gson.Gson

class TokenManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "ukrainian_airlines_prefs"
        private const val ACCESS_TOKEN = "access_token"
        private const val REFRESH_TOKEN = "refresh_token"
        private const val USER_DATA = "user_data"
        private const val TOKEN_EXPIRY = "token_expiry"
    }

    fun saveTokens(tokens: AuthTokens) {
        prefs.edit().apply {
            putString(ACCESS_TOKEN, tokens.access)
            putString(REFRESH_TOKEN, tokens.refresh)
            // Assuming JWT tokens have expiry info, you might want to decode and save expiry
            putLong(TOKEN_EXPIRY, System.currentTimeMillis() + 15 * 60 * 1000) // 15 minutes
            apply()
        }
    }

    fun getAccessToken(): String? {
        return prefs.getString(ACCESS_TOKEN, null)
    }

    fun getRefreshToken(): String? {
        return prefs.getString(REFRESH_TOKEN, null)
    }

    fun isTokenExpired(): Boolean {
        val expiry = prefs.getLong(TOKEN_EXPIRY, 0)
        return System.currentTimeMillis() >= expiry
    }

    fun clearTokens() {
        prefs.edit().apply {
            remove(ACCESS_TOKEN)
            remove(REFRESH_TOKEN)
            remove(TOKEN_EXPIRY)
            remove(USER_DATA)
            apply()
        }
    }

    fun hasValidToken(): Boolean {
        return getAccessToken() != null && !isTokenExpired()
    }

    fun updateAccessToken(newAccessToken: String) {
        prefs.edit().apply {
            putString(ACCESS_TOKEN, newAccessToken)
            putLong(TOKEN_EXPIRY, System.currentTimeMillis() + 15 * 60 * 1000) // 15 minutes
            apply()
        }
    }
}