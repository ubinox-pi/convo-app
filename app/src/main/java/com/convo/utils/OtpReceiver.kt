package com.convo.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.Telephony
import androidx.core.content.ContextCompat

/**
 * BroadcastReceiver for auto-reading OTP from SMS.
 * Listens for SMS from the specified sender number and extracts 6-digit OTP.
 */
class OtpReceiver(
    private val onOtpReceived: (String) -> Unit
) : BroadcastReceiver() {

    companion object {
        private const val OTP_SENDER_NUMBER = "9801112671"
        private val OTP_PATTERN = Regex("\\b(\\d{6})\\b")

        fun register(context: Context, onOtpReceived: (String) -> Unit): OtpReceiver {
            val receiver = OtpReceiver(onOtpReceived)
            val intentFilter = IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)
            intentFilter.priority = IntentFilter.SYSTEM_HIGH_PRIORITY
            ContextCompat.registerReceiver(
                context,
                receiver,
                intentFilter,
                ContextCompat.RECEIVER_EXPORTED
            )
            return receiver
        }

        fun unregister(context: Context, receiver: OtpReceiver) {
            try {
                context.unregisterReceiver(receiver)
            } catch (e: IllegalArgumentException) {
                // Receiver not registered, ignore
            }
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)

        for (message in messages) {
            val sender = message.displayOriginatingAddress ?: continue
            val body = message.messageBody ?: continue

            // Check if SMS is from the OTP sender
            if (sender.contains(OTP_SENDER_NUMBER) || sender.endsWith(OTP_SENDER_NUMBER)) {
                // Extract 6-digit OTP from message
                val matchResult = OTP_PATTERN.find(body)
                matchResult?.let {
                    val otp = it.groupValues[1]
                    onOtpReceived(otp)
                    return
                }
            }
        }
    }
}
