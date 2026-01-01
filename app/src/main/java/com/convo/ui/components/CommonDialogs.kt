package com.convo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.convo.ui.theme.AccentError
import com.convo.ui.theme.AccentPrimary
import com.convo.ui.theme.BgSecondary
import com.convo.ui.theme.BgTertiary
import com.convo.ui.theme.BorderColor
import com.convo.ui.theme.TextMuted
import com.convo.ui.theme.TextPrimary
import com.convo.ui.theme.TextSecondary
import kotlinx.coroutines.delay

private const val RESEND_TIMER_SECONDS = 300L // 5 minutes

@Composable
fun ExitConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BgSecondary,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = "Exit App",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Text(
                text = "Are you sure you want to exit?",
                color = TextSecondary,
                fontSize = 16.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentError,
                    contentColor = TextPrimary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("OK", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TextPrimary
                ),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                    brush = SolidColor(BorderColor)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Cancel", fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

@Composable
fun OtpVerificationDialog(
    phoneNumber: String,
    otp: String,
    onOtpChange: (String) -> Unit,
    isLoading: Boolean,
    isSendingOtp: Boolean,
    errorMessage: String?,
    resendTimerKey: Long,
    onVerify: () -> Unit,
    onResendOtp: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var remainingSeconds by remember(resendTimerKey) { mutableLongStateOf(RESEND_TIMER_SECONDS) }
    val canResend = remainingSeconds <= 0

    // Countdown timer
    LaunchedEffect(resendTimerKey) {
        remainingSeconds = RESEND_TIMER_SECONDS
        while (remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds--
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Format time as MM:SS
    fun formatTime(seconds: Long): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return "%02d:%02d".format(mins, secs)
    }

    AlertDialog(
        onDismissRequest = { /* Non-cancelable */ },
        containerColor = BgSecondary,
        shape = RoundedCornerShape(16.dp),
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        ),
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Verify OTP",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Enter the 6-digit code sent to",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = phoneNumber,
                    color = AccentPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // OTP Input boxes
                BasicTextField(
                    value = otp,
                    onValueChange = { newValue ->
                        if (newValue.length <= 6 && newValue.all { it.isDigit() }) {
                            onOtpChange(newValue)
                        }
                    },
                    enabled = !isLoading && !isSendingOtp,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.focusRequester(focusRequester),
                    textStyle = TextStyle(color = TextPrimary),
                    cursorBrush = SolidColor(AccentPrimary),
                    decorationBox = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(6) { index ->
                                val char = otp.getOrNull(index)?.toString() ?: ""
                                val isFocused = otp.length == index
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp)
                                        .background(BgTertiary, RoundedCornerShape(8.dp))
                                        .border(
                                            width = 2.dp,
                                            color = when {
                                                errorMessage != null -> AccentError
                                                isFocused -> AccentPrimary
                                                else -> BorderColor
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = char,
                                        color = TextPrimary,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                )

                // Error message
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage,
                        color = AccentError,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Resend OTP with timer
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSendingOtp) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = AccentPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sending...",
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                    } else if (canResend) {
                        Text(
                            text = "Didn't receive code? ",
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                        TextButton(
                            onClick = onResendOtp,
                            enabled = !isLoading
                        ) {
                            Text(
                                text = "Resend",
                                color = AccentPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        Text(
                            text = "Resend OTP in ",
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                        Text(
                            text = formatTime(remainingSeconds),
                            color = AccentPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onVerify,
                enabled = otp.length == 6 && !isLoading && !isSendingOtp,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentPrimary,
                    contentColor = TextPrimary,
                    disabledContainerColor = AccentPrimary.copy(alpha = 0.5f),
                    disabledContentColor = TextPrimary.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = TextPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Verifying...", fontWeight = FontWeight.SemiBold)
                } else {
                    Text("Verify", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    )
}

