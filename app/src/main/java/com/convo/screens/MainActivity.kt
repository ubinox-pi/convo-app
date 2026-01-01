package com.convo.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.convo.network.ApiClient
import com.convo.ui.theme.AccentPrimary
import com.convo.ui.theme.BgPrimary
import com.convo.ui.theme.ConvoTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ApiClient with context (required for CookieJar)
        ApiClient.init(this)

        setContent {
            ConvoTheme(dynamicColor = false) {
                SplashScreen()

                LaunchedEffect(Unit) {
                    // Small delay to show splash
                    delay(1000)

                    // Check session and navigate
                    checkSessionAndNavigate()
                }
            }
        }
    }

    private fun checkSessionAndNavigate() {
        // First check if session cookie exists locally
        if (!ApiClient.hasValidSession()) {
            // No session cookie - go directly to Login
            android.util.Log.d("MainActivity", "No session cookie found, navigating to Login")
            navigateToLogin()
            return
        }

        // Session cookie exists - verify with server
        android.util.Log.d("MainActivity", "Session cookie found, checking with server...")
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.checkSessionExpire()
                val responseBody = response.body() ?: ""
                android.util.Log.d("MainActivity", "Session check response: ${response.code()} - $responseBody")

                when {
                    response.code() == 200 -> {
                        // Session valid - go to Dashboard
                        android.util.Log.d("MainActivity", "Session valid, navigating to Dashboard")
                        navigateToDashboard()
                    }
                    response.code() == 503 -> {
                        // Session expired - clear cookies and go to Login
                        android.util.Log.d("MainActivity", "Session expired, clearing and navigating to Login")
                        ApiClient.clearSession()
                        navigateToLogin()
                    }
                    else -> {
                        // Other error - treat as expired for safety
                        android.util.Log.w("MainActivity", "Unexpected response: ${response.code()}, treating as expired")
                        ApiClient.clearSession()
                        navigateToLogin()
                    }
                }
            } catch (e: Exception) {
                // Network error - go to login for safety
                android.util.Log.e("MainActivity", "Session check failed: ${e.message}", e)
                navigateToLogin()
            }
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, Login::class.java))
        finish()
    }

    private fun navigateToDashboard() {
        startActivity(Intent(this, Dashboard::class.java))
        finish()
    }
}

@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgPrimary),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Convo",
            color = AccentPrimary,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true, showSystemUi = false)
@Composable
fun SplashScreenPreview() {
    ConvoTheme {
        SplashScreen()
    }
}
