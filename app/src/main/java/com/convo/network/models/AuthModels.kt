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

    @SerializedName("error")
    val error: String?,

    @SerializedName("errorCode")
    val errorCode: String?,

    @SerializedName("message")
    val message: String,

    @SerializedName("path")
    val path: String?
)

/**
 * Request model for OTP verification
 */
data class VerifyOtpRequest(
    @SerializedName("phoneNumber")
    val phoneNumber: String,

    @SerializedName("otp")
    val otp: String
)

/**
 * Response model for OTP operations
 */
data class OtpResponse(
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

/**
 * Response model for login
 */
data class LoginResponse(
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

    @SerializedName("data")
    val data: LoginData?,

    @SerializedName("path")
    val path: String?
)

/**
 * Login data containing session ID
 */
data class LoginData(
    @SerializedName("sessionId")
    val sessionId: String
)

/**
 * Response model for session check
 */
data class SessionCheckResponse(
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

