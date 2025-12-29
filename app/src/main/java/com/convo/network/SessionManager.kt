package com.convo.network

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Manages session state securely using EncryptedSharedPreferences.
 * For HTTP-only cookies, we track if a valid session exists.
 */
object SessionManager {

    private const val PREFS_NAME = "convo_session_prefs"
    private const val KEY_HAS_SESSION = "has_session"
    private const val KEY_SESSION_ID = "session_id"

    private var sharedPreferences: SharedPreferences? = null

    fun init(context: Context) {
        if (sharedPreferences == null) {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            sharedPreferences = EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }

    /**
     * Check if a valid session exists
     */
    fun hasSession(): Boolean {
        return sharedPreferences?.getBoolean(KEY_HAS_SESSION, false) ?: false
    }

    /**
     * Get the session ID if available
     */
    fun getSessionId(): String? {
        return sharedPreferences?.getString(KEY_SESSION_ID, null)
    }

    /**
     * Save session after successful login
     */
    fun saveSession(sessionId: String) {
        sharedPreferences?.edit()?.apply {
            putBoolean(KEY_HAS_SESSION, true)
            putString(KEY_SESSION_ID, sessionId)
            apply()
        }
    }

    /**
     * Clear session on logout
     */
    fun clearSession() {
        sharedPreferences?.edit()?.apply {
            putBoolean(KEY_HAS_SESSION, false)
            remove(KEY_SESSION_ID)
            apply()
        }
    }
}

