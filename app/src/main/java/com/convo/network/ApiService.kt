package com.convo.network

import com.convo.network.models.LoginResponse
import com.convo.network.models.OtpResponse
import com.convo.network.models.RegisterRequest
import com.convo.network.models.RegisterResponse
import com.convo.network.models.SessionCheckResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * API Service interface defining all API endpoints.
 * Add new endpoints here as needed.
 */
interface ApiService {

    @GET("v1/test/message")
    suspend fun getTestMessage(): Response<String>

    @POST("api/v1/users/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("api/v1/auth/send-otp")
    suspend fun sendOtp(@Query("phoneNumber") phoneNumber: String): Response<OtpResponse>

    @POST("api/v1/auth/verify-otp")
    suspend fun verifyOtp(
        @Query("phoneNumber") phoneNumber: String,
        @Query("otp") otp: String
    ): Response<OtpResponse>

    @POST("api/v1/auth/login")
    suspend fun login(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("deviceModel") deviceModel: String,
        @Query("deviceOs") deviceOs: String,
        @Query("deviceId") deviceId: String,
        @Query("deviceToken") deviceToken: String
    ): Response<LoginResponse>

    @GET("api/v1/test/check-session-expire")
    suspend fun checkSessionExpire(): Response<String>
}

