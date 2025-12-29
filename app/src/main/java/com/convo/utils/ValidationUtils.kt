package com.convo.utils

/**
 * Validation utilities for form fields
 */
object ValidationUtils {

    /**
     * Validate username: Required, 3-30 characters
     */
    fun validateUsername(username: String): ValidationResult {
        return when {
            username.isBlank() -> ValidationResult.Error("Username is required")
            username.length < 3 -> ValidationResult.Error("Username must be at least 3 characters")
            username.length > 30 -> ValidationResult.Error("Username must be at most 30 characters")
            else -> ValidationResult.Success
        }
    }

    /**
     * Validate email: Required, valid email format
     */
    fun validateEmail(email: String): ValidationResult {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return when {
            email.isBlank() -> ValidationResult.Error("Email is required")
            !email.matches(emailRegex) -> ValidationResult.Error("Please enter a valid email")
            else -> ValidationResult.Success
        }
    }

    /**
     * Validate phone number: Required, exactly 10 digits (no country code)
     */
    fun validatePhoneNumber(phoneNumber: String): ValidationResult {
        val phoneRegex = "^[0-9]{10}$".toRegex()
        return when {
            phoneNumber.isBlank() -> ValidationResult.Error("Phone number is required")
            !phoneNumber.matches(phoneRegex) -> ValidationResult.Error("Phone number must be exactly 10 digits")
            else -> ValidationResult.Success
        }
    }

    /**
     * Validate password: Required, 8-16 chars, 1 uppercase, 1 lowercase, 1 digit, 1 special char
     */
    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult.Error("Password is required")
            password.length < 8 -> ValidationResult.Error("Password must be at least 8 characters")
            password.length > 16 -> ValidationResult.Error("Password must be at most 16 characters")
            !password.any { it.isUpperCase() } -> ValidationResult.Error("Password must contain at least 1 uppercase letter")
            !password.any { it.isLowerCase() } -> ValidationResult.Error("Password must contain at least 1 lowercase letter")
            !password.any { it.isDigit() } -> ValidationResult.Error("Password must contain at least 1 digit")
            !password.any { !it.isLetterOrDigit() } -> ValidationResult.Error("Password must contain at least 1 special character")
            else -> ValidationResult.Success
        }
    }

    /**
     * Validate name: Optional, but if provided must be 3-30 characters
     */
    fun validateName(name: String, fieldName: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Success // Optional field
            name.length < 3 -> ValidationResult.Error("$fieldName must be at least 3 characters")
            name.length > 30 -> ValidationResult.Error("$fieldName must be at most 30 characters")
            else -> ValidationResult.Success
        }
    }
}

sealed class ValidationResult {
    data object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()

    fun isValid(): Boolean = this is Success
    fun errorMessage(): String? = (this as? Error)?.message
}

