package com.convo.network

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

/**
 * A persistent CookieJar that stores cookies in EncryptedSharedPreferences.
 * Handles HttpOnly cookies, session fixation protection, and automatic cookie management.
 */
class PersistentCookieJar(context: Context) : CookieJar {

    private val sharedPreferences: SharedPreferences
    private val cookieStore = mutableMapOf<String, MutableList<Cookie>>()

    init {
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

        // Load persisted cookies on initialization
        loadCookies()
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val host = url.host
        android.util.Log.d("CookieJar", "saveFromResponse - Host: $host, Cookies received: ${cookies.size}")

        // Get or create cookie list for this host
        val hostCookies = cookieStore.getOrPut(host) { mutableListOf() }

        for (cookie in cookies) {
            android.util.Log.d("CookieJar", "Cookie: ${cookie.name}=${cookie.value}, expires=${cookie.expiresAt}, httpOnly=${cookie.httpOnly}")

            // Remove existing cookie with same name (handles session fixation)
            hostCookies.removeAll { it.name == cookie.name }

            // Add new cookie if not expired
            if (!isExpired(cookie)) {
                hostCookies.add(cookie)
                android.util.Log.d("CookieJar", "Cookie saved: ${cookie.name}")
            } else {
                android.util.Log.d("CookieJar", "Cookie expired, not saving: ${cookie.name}")
            }
        }

        // Persist cookies
        persistCookies()
        android.util.Log.d("CookieJar", "Cookies persisted. Total hosts: ${cookieStore.size}")
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val host = url.host
        val hostCookies = cookieStore[host] ?: return emptyList()

        // Filter out expired cookies
        val validCookies = hostCookies.filter { !isExpired(it) && matchesPath(it, url) }

        android.util.Log.d("CookieJar", "loadForRequest - Host: $host, Cookies to send: ${validCookies.map { it.name }}")

        // Update store if any cookies were expired
        if (validCookies.size != hostCookies.size) {
            hostCookies.clear()
            hostCookies.addAll(validCookies)
            persistCookies()
        }

        return validCookies
    }

    /**
     * Check if a valid session cookie exists
     */
    fun hasValidSession(): Boolean {
        android.util.Log.d("CookieJar", "hasValidSession - cookieStore hosts: ${cookieStore.keys}")
        for ((host, cookies) in cookieStore) {
            android.util.Log.d("CookieJar", "Host: $host, cookies: ${cookies.map { "${it.name}=${it.value}" }}")
            for (cookie in cookies) {
                if (cookie.name == SESSION_COOKIE_NAME && !isExpired(cookie)) {
                    android.util.Log.d("CookieJar", "Found valid $SESSION_COOKIE_NAME")
                    return true
                }
            }
        }
        android.util.Log.d("CookieJar", "No valid $SESSION_COOKIE_NAME found")
        return false
    }

    /**
     * Clear all cookies (for logout)
     */
    fun clearCookies() {
        cookieStore.clear()
        sharedPreferences.edit().clear().apply()
    }

    /**
     * Clear session cookies only
     */
    fun clearSessionCookies() {
        for ((_, cookies) in cookieStore) {
            cookies.removeAll { it.name == SESSION_COOKIE_NAME }
        }
        persistCookies()
    }

    private fun isExpired(cookie: Cookie): Boolean {
        return cookie.expiresAt < System.currentTimeMillis()
    }

    private fun matchesPath(cookie: Cookie, url: HttpUrl): Boolean {
        val urlPath = url.encodedPath
        val cookiePath = cookie.path
        return urlPath.startsWith(cookiePath)
    }

    private fun persistCookies() {
        val editor = sharedPreferences.edit()
        editor.clear()

        for ((host, cookies) in cookieStore) {
            val nonExpiredCookies = cookies.filter { !isExpired(it) }
            if (nonExpiredCookies.isNotEmpty()) {
                val cookieStrings = nonExpiredCookies.map { serializeCookie(it) }
                editor.putStringSet("cookies_$host", cookieStrings.toSet())
            }
        }

        editor.apply()
    }

    private fun loadCookies() {
        val allPrefs = sharedPreferences.all
        for ((key, value) in allPrefs) {
            if (key.startsWith("cookies_") && value is Set<*>) {
                val host = key.removePrefix("cookies_")
                val cookies = mutableListOf<Cookie>()

                @Suppress("UNCHECKED_CAST")
                for (cookieString in value as Set<String>) {
                    deserializeCookie(cookieString, host)?.let { cookie ->
                        if (!isExpired(cookie)) {
                            cookies.add(cookie)
                        }
                    }
                }

                if (cookies.isNotEmpty()) {
                    cookieStore[host] = cookies
                }
            }
        }
    }

    private fun serializeCookie(cookie: Cookie): String {
        return buildString {
            append(cookie.name).append("=").append(cookie.value)
            append("|domain=").append(cookie.domain)
            append("|path=").append(cookie.path)
            append("|expires=").append(cookie.expiresAt)
            append("|secure=").append(cookie.secure)
            append("|httpOnly=").append(cookie.httpOnly)
        }
    }

    private fun deserializeCookie(cookieString: String, host: String): Cookie? {
        return try {
            val parts = cookieString.split("|")
            val nameValue = parts[0].split("=", limit = 2)
            val name = nameValue[0]
            val value = nameValue.getOrElse(1) { "" }

            var domain = host
            var path = "/"
            var expiresAt = Long.MAX_VALUE
            var secure = false
            var httpOnly = false

            for (i in 1 until parts.size) {
                val part = parts[i]
                when {
                    part.startsWith("domain=") -> domain = part.removePrefix("domain=")
                    part.startsWith("path=") -> path = part.removePrefix("path=")
                    part.startsWith("expires=") -> expiresAt = part.removePrefix("expires=").toLongOrNull() ?: Long.MAX_VALUE
                    part.startsWith("secure=") -> secure = part.removePrefix("secure=").toBoolean()
                    part.startsWith("httpOnly=") -> httpOnly = part.removePrefix("httpOnly=").toBoolean()
                }
            }

            Cookie.Builder()
                .name(name)
                .value(value)
                .domain(domain)
                .path(path)
                .expiresAt(expiresAt)
                .apply {
                    if (secure) secure()
                    if (httpOnly) httpOnly()
                }
                .build()
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        private const val PREFS_NAME = "convo_cookies"
        private const val SESSION_COOKIE_NAME = "CONVO_SESSION"
    }
}
