package com.convo.screens

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.convo.network.models.ApiErrorResponse
import com.convo.ui.components.ExitConfirmationDialog
import com.convo.ui.theme.AccentError
import com.convo.ui.theme.AccentPrimary
import com.convo.ui.theme.BgPrimary
import com.convo.ui.theme.BgSecondary
import com.convo.ui.theme.BorderColor
import com.convo.ui.theme.ConvoTheme
import com.convo.ui.theme.TextMuted
import com.convo.ui.theme.TextPrimary
import com.convo.ui.theme.TextSecondary
import com.google.gson.Gson
import kotlinx.coroutines.launch

class Login : BaseActivity() {

    private fun getDeviceModel(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL}"
    }

    private fun getDeviceOs(): String {
        return "Android ${Build.VERSION.RELEASE}"
    }

    private fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    private fun getDeviceToken(): String {
        // TODO: Replace with actual FCM token when Firebase is integrated
        return "placeholder_token"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ConvoTheme(dynamicColor = false) {
                var showExitDialog by remember { mutableStateOf(false) }
                var isLoading by remember { mutableStateOf(false) }
                var errorMessage by remember { mutableStateOf<String?>(null) }

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

                LoginScreen(
                    showExitDialog = showExitDialog,
                    onDismissExitDialog = { showExitDialog = false },
                    onConfirmExit = { finishAffinity() },
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    onErrorDismiss = { errorMessage = null },
                    onLoginClick = { username, password ->
                        if (username.isBlank()) {
                            errorMessage = "Please enter your username"
                            return@LoginScreen
                        }
                        if (password.isBlank()) {
                            errorMessage = "Please enter your password"
                            return@LoginScreen
                        }

                        isLoading = true
                        errorMessage = null

                        lifecycleScope.launch {
                            try {
                                val deviceModel = getDeviceModel()
                                val deviceOs = getDeviceOs()
                                val deviceId = getDeviceId(this@Login)
                                val deviceToken = getDeviceToken()

                                android.util.Log.d("Login", "Logging in user: $username")
                                android.util.Log.d("Login", "Device: $deviceModel, OS: $deviceOs, ID: $deviceId")

                                val response = ApiClient.apiService.login(
                                    username = username,
                                    password = password,
                                    deviceModel = deviceModel,
                                    deviceOs = deviceOs,
                                    deviceId = deviceId,
                                    deviceToken = deviceToken
                                )

                                android.util.Log.d("Login", "Response code: ${response.code()}")

                                if (response.isSuccessful && response.body()?.success == true) {
                                    // Session cookie is automatically handled by CookieJar
                                    Toast.makeText(
                                        this@Login,
                                        response.body()?.message ?: "Login successful!",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    // Navigate to Dashboard
                                    startActivity(Intent(this@Login, Dashboard::class.java))
                                    finish()
                                } else {
                                    val errorBody = response.errorBody()?.string()
                                    android.util.Log.e("Login", "Error response: $errorBody")

                                    val apiError = try {
                                        Gson().fromJson(errorBody, ApiErrorResponse::class.java)
                                    } catch (e: Exception) {
                                        null
                                    }

                                    errorMessage = apiError?.message ?: when (response.code()) {
                                        401 -> "Invalid username or password"
                                        403 -> "Account is locked or disabled"
                                        404 -> "User not found"
                                        else -> "Login failed (${response.code()}). Please try again."
                                    }
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("Login", "Network exception: ${e.message}", e)
                                errorMessage = "Network error: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    onRegisterClick = {
                        startActivity(Intent(this, Register::class.java))
                        finish()
                    },
                    onForgotPasswordClick = {
                        // Navigate to forgot password
                    }
                )
            }
        }
    }
}

@Composable
fun LoginScreen(
    showExitDialog: Boolean = false,
    onDismissExitDialog: () -> Unit = {},
    onConfirmExit: () -> Unit = {},
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onErrorDismiss: () -> Unit = {},
    onLoginClick: (username: String, password: String) -> Unit = { _, _ -> },
    onRegisterClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {}
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // App Logo/Title
            Text(
                text = "Convo",
                color = AccentPrimary,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Welcome back!",
                color = TextSecondary,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(60.dp))

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

            // Username Field
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username", color = TextMuted) },
                placeholder = { Text("Enter your username", color = TextMuted) },
                singleLine = true,
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                colors = OutlinedTextFieldDefaults.colors(
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
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = TextMuted) },
                placeholder = { Text("Enter your password", color = TextMuted) },
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
                colors = OutlinedTextFieldDefaults.colors(
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
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Forgot Password
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "Forgot Password?",
                    color = AccentPrimary,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable(enabled = !isLoading) { onForgotPasswordClick() }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Login Button
            Button(
                onClick = { onLoginClick(username, password) },
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
                        text = "Login",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Register Option
            Row(
                modifier = Modifier.padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Don't have an account? ",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
                Text(
                    text = "Register",
                    color = AccentPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(enabled = !isLoading) { onRegisterClick() }
                )
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    ConvoTheme(dynamicColor = false) {
        LoginScreen()
    }
}
