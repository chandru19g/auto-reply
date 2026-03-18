package com.autoreply.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsManager
import android.util.Log
import com.autoreply.app.data.AppPreferences
import com.autoreply.app.data.SessionLogPreferences
import com.autoreply.app.data.WhitelistPreferences
import com.autoreply.app.domain.Mode

/**
 * Listens for incoming SMS. When a mode is active and SMS reply message is set,
 * sends an automatic reply to the sender.
 *
 * Deduplication: uses the PDU timestamp from the message to uniquely identify it.
 * Some devices fire SMS_RECEIVED twice for the same message; this prevents a
 * double reply for that case.
 */
class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val firstMessage = messages.first()
        val sender = firstMessage.originatingAddress?.trim() ?: return
        if (sender.isEmpty()) return

        // Skip business/company/short-code senders — they are not real people
        if (MessageFilter.isBusinessSmsSender(sender)) {
            Log.d(TAG, "Skipping auto-reply to business/short-code sender: $sender")
            return
        }

        // Whitelist: whitelisted contacts always get through
        if (WhitelistPreferences(context).isNumberWhitelisted(sender)) {
            Log.d(TAG, "Whitelisted SMS sender $sender — skipping auto-reply")
            return
        }

        // Use PDU timestamp as a stable unique ID for this specific message
        val msgTimestamp = firstMessage.timestampMillis

        val prefs = AppPreferences(context)
        val appState = prefs.getAppStateSnapshot()
        if (appState.activeMode == Mode.OFF) return

        // Session log: record who contacted you during an active mode
        SessionLogPreferences(context).recordSms(sender, appState.activeMode)

        val replyText = AppPreferences.withAutomatedNote(
            appState.getSmsMessageForMode(appState.activeMode)
        )
        if (replyText.isBlank()) return

        // Dedup: skip if we already replied to this exact message (same PDU timestamp)
        if (DedupHelper.alreadyRepliedToSms(context, sender, msgTimestamp)) {
            Log.d(TAG, "Duplicate SMS broadcast detected for $sender @ $msgTimestamp — skipping")
            return
        }

        // Production rate limit (COOLDOWN_MS = 0 during testing)
        if (!RateLimitHelper.canReplyToSms(context, sender)) return

        try {
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            smsManager?.sendTextMessage(sender, null, replyText, null, null)
                ?: Log.e(TAG, "SmsManager unavailable")

            DedupHelper.recordSmsReply(context, sender, msgTimestamp)
            RateLimitHelper.recordSmsReply(context, sender)
            Log.d(TAG, "SMS reply sent to $sender")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send SMS reply: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "SmsReceiver"
    }
}
