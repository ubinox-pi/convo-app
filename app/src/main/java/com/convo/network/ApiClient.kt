package com.convo.network

import android.content.Context
import com.convo.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private var cookieJar: PersistentCookieJar? = null

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private var okHttpClient: OkHttpClient? = null
    private var retrofit: Retrofit? = null
    private var _apiService: ApiService? = null

    /**
     * Initialize ApiClient with context (required for CookieJar)
     * Call this in Application.onCreate() or MainActivity.onCreate()
     */
    fun init(context: Context) {
        if (cookieJar == null) {
            cookieJar = PersistentCookieJar(context.applicationContext)

            okHttpClient = OkHttpClient.Builder()
                .cookieJar(cookieJar!!)
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL.ensureTrailingSlash())
                .client(okHttpClient!!)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            _apiService = retrofit!!.create(ApiService::class.java)
        }
    }

    val apiService: ApiService
        get() = _apiService ?: throw IllegalStateException(
            "ApiClient not initialized. Call ApiClient.init(context) first."
        )

    /**
     * Check if a valid session exists
     */
    fun hasValidSession(): Boolean {
        return cookieJar?.hasValidSession() ?: false
    }

    /**
     * Clear all cookies (for logout)
     */
    fun clearSession() {
        cookieJar?.clearCookies()
    }

    /**
     * Clear only session cookies
     */
    fun clearSessionCookies() {
        cookieJar?.clearSessionCookies()
    }

    private fun String.ensureTrailingSlash(): String {
        return if (this.endsWith("/")) this else "$this/"
    }
}
