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
import com.convo.network.SessionManager
import com.convo.ui.theme.AccentPrimary
import com.convo.ui.theme.BgPrimary
import com.convo.ui.theme.ConvoTheme
import kotlinx.coroutines.delay

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize SessionManager
        SessionManager.init(this)

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
        if (SessionManager.hasSession()) {
            // Session exists - go to Dashboard
            startActivity(Intent(this, Dashboard::class.java))
        } else {
            // No session - go to Login
            startActivity(Intent(this, Login::class.java))
        }
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