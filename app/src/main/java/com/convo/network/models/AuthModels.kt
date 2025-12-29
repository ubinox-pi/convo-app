package com.convo.network.models

import com.google.gson.annotations.SerializedName

/**
 * Request model for user registration
 */
data class RegisterRequest(
    @SerializedName("username")
    val username: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("phoneNumber")
    val phoneNumber: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("firstname")
    val firstname: String? = null,

    @SerializedName("lastname")
    val lastname: String? = null
)

/**
 * Response model for user registration
 */
data class RegisterResponse(
    @SerializedName("timestamp")
    val timestamp: String,

    @SerializedName("status")
    val status: Int,

    @SerializedName("statusText")
    val statusText: String,

    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: UserData?,

    @SerializedName("path")
    val path: String
)

/**
 * User data from registration response
 */
data class UserData(
    @SerializedName("userId")
    val userId: Long,

    @SerializedName("username")
    val username: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("isEmailVerified")
    val isEmailVerified: Boolean,

    @SerializedName("phoneNumber")
    val phoneNumber: String,

    @SerializedName("isPhoneNumberVerified")
    val isPhoneNumberVerified: Boolean,

    @SerializedName("firstname")
    val firstname: String?,

    @SerializedName("lastname")
    val lastname: String?
)

/**
 * Generic API error response
 */
data class ApiErrorResponse(
    @SerializedName("timestamp")
    val timestamp: String?,

    @SerializedName("status")
    val status: Int,

    @SerializedName("statusText")
    val statusText: String?,

    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("path")
    val path: String?
)

