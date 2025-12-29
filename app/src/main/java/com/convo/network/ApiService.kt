package com.convo.network

import com.convo.network.models.RegisterRequest
import com.convo.network.models.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * API Service interface defining all API endpoints.
 * Add new endpoints here as needed.
 */
interface ApiService {

    @GET("v1/test/message")
    suspend fun getTestMessage(): Response<String>

    @POST("api/v1/users/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>
}

