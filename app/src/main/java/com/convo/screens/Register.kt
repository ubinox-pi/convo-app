package com.convo.screens

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.convo.R
import com.convo.network.ApiClient
import com.convo.network.models.RegisterRequest
import com.convo.ui.theme.*
import com.convo.utils.ValidationUtils
import com.google.gson.Gson
import kotlinx.coroutines.launch

class Register : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ConvoTheme(dynamicColor = false) {
                var showExitDialog by remember { mutableStateOf(false) }
                var isLoading by remember { mutableStateOf(false) }
                var errorMessage by remember { mutableStateOf<String?>(null) }

                // Handle back press
                DisposableEffect(Unit) {
                    val callback = object : OnBackPressedCallback(true) {
                        override fun handleOnBackPressed() {
                            if (!isLoading) {
                                showExitDialog = true
                            }
                        }
                    }
                    onBackPressedDispatcher.addCallback(callback)
                    onDispose { callback.remove() }
                }

                RegisterScreen(
                    showExitDialog = showExitDialog,
                    onDismissExitDialog = { showExitDialog = false },
                    onConfirmExit = { finishAffinity() },
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    onErrorDismiss = { errorMessage = null },
                    onRegisterClick = { username, email, phone, password, firstName, lastName ->
                        // Validate all fields
                        val usernameValidation = ValidationUtils.validateUsername(username)
                        val emailValidation = ValidationUtils.validateEmail(email)
                        val phoneValidation = ValidationUtils.validatePhoneNumber(phone)
                        val passwordValidation = ValidationUtils.validatePassword(password)
                        val firstNameValidation = ValidationUtils.validateName(firstName, "First name")
                        val lastNameValidation = ValidationUtils.validateName(lastName, "Last name")

                        // Check for validation errors
                        val validationError = listOf(
                            usernameValidation,
                            emailValidation,
                            phoneValidation,
                            passwordValidation,
                            firstNameValidation,
                            lastNameValidation
                        ).firstOrNull { !it.isValid() }?.errorMessage()

                        if (validationError != null) {
                            errorMessage = validationError
                            return@RegisterScreen
                        }

                        // Make API call
                        isLoading = true
                        errorMessage = null

                        lifecycleScope.launch {
                            try {
                                val request = RegisterRequest(
                                    username = username,
                                    email = email,
                                    phoneNumber = phone,
                                    password = password,
                                    firstname = firstName.ifBlank { null },
                                    lastname = lastName.ifBlank { null }
                                )


                                android.util.Log.d("Register", "Sending registration request: $request")

                                val response = ApiClient.apiService.register(request)

                                android.util.Log.d("Register", "Response code: ${response.code()}")

                                if (response.isSuccessful && response.body()?.success == true) {
                                    Toast.makeText(
                                        this@Register,
                                        response.body()?.message ?: "Registration successful!",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    // Navigate to Login
                                    startActivity(Intent(this@Register, Login::class.java))
                                    finish()
                                } else {
                                    // Parse error response
                                    val errorBody = response.errorBody()?.string()
                                    android.util.Log.e("Register", "Error response: $errorBody")

                                    val apiError = try {
                                        Gson().fromJson(errorBody, com.convo.network.models.ApiErrorResponse::class.java)
                                    } catch (e: Exception) {
                                        android.util.Log.e("Register", "Failed to parse error: ${e.message}")
                                        null
                                    }

                                    errorMessage = when (response.code()) {
                                        404 -> "Server endpoint not found. Please contact support."
                                        500 -> "Server error. Please try again later."
                                        400 -> apiError?.message ?: "Invalid request. Please check your input."
                                        409 -> apiError?.message ?: "User already exists."
                                        else -> apiError?.message ?: "Registration failed (${response.code()}). Please try again."
                                    }
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("Register", "Network exception: ${e.message}", e)
                                errorMessage = "Network error: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    onLoginClick = {
                        startActivity(Intent(this, Login::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun RegisterScreen(
    showExitDialog: Boolean = false,
    onDismissExitDialog: () -> Unit = {},
    onConfirmExit: () -> Unit = {},
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onErrorDismiss: () -> Unit = {},
    onRegisterClick: (username: String, email: String, phone: String, password: String, firstName: String, lastName: String) -> Unit = { _, _, _, _, _, _ -> },
    onLoginClick: () -> Unit = {}
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // Exit confirmation dialog
    if (showExitDialog) {
        ExitConfirmationDialog(
            onDismiss = onDismissExitDialog,
            onConfirm = onConfirmExit
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // App Logo/Title
            Text(
                text = "Convo",
                color = AccentPrimary,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Create your account",
                color = TextSecondary,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Error message
            if (errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = AccentError.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = errorMessage,
                            color = AccentError,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "✕",
                            color = AccentError,
                            fontSize = 16.sp,
                            modifier = Modifier.clickable { onErrorDismiss() }
                        )
                    }
                }
            }

            // First Name & Last Name Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // First Name Field
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name", color = TextMuted) },
                    singleLine = true,
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    colors = outlinedTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )

                // Last Name Field
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name", color = TextMuted) },
                    singleLine = true,
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    colors = outlinedTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Username Field
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username", color = TextMuted) },
                placeholder = { Text("Choose a username", color = TextMuted) },
                singleLine = true,
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                colors = outlinedTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", color = TextMuted) },
                placeholder = { Text("Enter your email", color = TextMuted) },
                singleLine = true,
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                colors = outlinedTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Phone Number Field
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() }) phoneNumber = it },
                label = { Text("Phone Number", color = TextMuted) },
                placeholder = { Text("Enter your phone number", color = TextMuted) },
                singleLine = true,
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                ),
                colors = outlinedTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = TextMuted) },
                placeholder = { Text("Create a password", color = TextMuted) },
                singleLine = true,
                enabled = !isLoading,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            painter = painterResource(
                                id = if (passwordVisible) R.drawable.ic_visibility_off
                                else R.drawable.ic_visibility
                            ),
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = TextMuted
                        )
                    }
                },
                colors = outlinedTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Password requirements hint
            Text(
                text = "8-16 chars, uppercase, lowercase, digit, special char",
                color = TextMuted,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, start = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Register Button
            Button(
                onClick = { onRegisterClick(username, email, phoneNumber, password, firstName, lastName) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentPrimary,
                    contentColor = TextPrimary,
                    disabledContainerColor = AccentPrimary.copy(alpha = 0.5f),
                    disabledContentColor = TextPrimary.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = TextPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Create Account",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Login Option
            Row(
                modifier = Modifier.padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Already have an account? ",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
                Text(
                    text = "Login",
                    color = AccentPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(enabled = !isLoading) { onLoginClick() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun outlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedBorderColor = AccentPrimary,
    unfocusedBorderColor = BorderColor,
    focusedLabelColor = AccentPrimary,
    unfocusedLabelColor = TextMuted,
    cursorColor = AccentPrimary,
    focusedContainerColor = BgSecondary,
    unfocusedContainerColor = BgSecondary,
    disabledTextColor = TextMuted,
    disabledBorderColor = BorderColor.copy(alpha = 0.5f),
    disabledContainerColor = BgSecondary.copy(alpha = 0.5f)
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    ConvoTheme(dynamicColor = false) {
        RegisterScreen()
    }
}
