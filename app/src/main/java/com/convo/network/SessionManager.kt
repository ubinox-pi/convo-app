package com.convo.network

import android.content.Context

/**
 * SessionManager now delegates to ApiClient's CookieJar for session management.
 * This maintains backward compatibility while using the new cookie-based session handling.
 */
object SessionManager {

    /**
     * Initialize SessionManager (delegates to ApiClient)
     */
    fun init(context: Context) {
        ApiClient.init(context)
    }

    /**
     * Check if a valid session exists
     */
    fun hasSession(): Boolean {
        return ApiClient.hasValidSession()
    }

    /**
     * Clear session on logout
     */
    fun clearSession() {
        ApiClient.clearSession()
    }
}
